package com.clmcat.qianyu.mall.inv.rpc;

import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.inv.model.dto.InvStockDto;
import com.clmcat.qianyu.mall.api.oms.OmsOrderApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderItemDto;
import com.clmcat.qianyu.mall.inv.mapper.InvStockMapper;
import com.clmcat.qianyu.mall.inv.model.entity.InvStock;
import com.clmcat.qianyu.mall.inv.model.entity.InvStockLog;
import com.clmcat.qianyu.mall.inv.model.entity.status.InvStatus;
import com.clmcat.qianyu.mall.inv.support.InvSupport;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@DubboService
@Service
public class InvStockApiImpl implements InvStockApi {

    @Resource
    private InvStockMapper stockMapper;

    @DubboReference
    private OmsOrderApi omsOrderApi;

    @Resource
    private InvStockLogApiImpl stockLogServiceBiz;

    @Override
    public InvStockDto getBySkuId(Long skuId) {
        QueryWrapper qw = QueryWrapper.create().eq(InvStock::getSkuId, skuId);
        InvStock stock = stockMapper.selectOneByQuery(qw);
        return toDto(stock);
    }

    @Override
    public List<InvStockDto> batchQuery(List<Long> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) return new ArrayList<>();
        QueryWrapper qw = QueryWrapper.create().in(InvStock::getSkuId, skuIds);
        List<InvStock> stocks = stockMapper.selectListByQuery(qw);
        List<InvStockDto> dtos = new ArrayList<>();
        for (InvStock s : stocks) {
            InvStockDto dto = toDto(s);
            if (dto != null) dtos.add(dto);
        }
        return dtos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lockStock(String orderSn, List<InvStockDto.StockLockItem> items) {
        if (items == null || items.isEmpty()) return false;
        long now = System.currentTimeMillis();
        for (InvStockDto.StockLockItem item : items) {
            QueryWrapper qw = QueryWrapper.create().eq(InvStock::getSkuId, item.getSkuId());
            InvStock stock = stockMapper.selectOneByQuery(qw);
            InvStatus.INV_SKU_NOT_FOUND.assertThrowResEx(stock == null);
            // S2: 真 CAS（WHERE available_stock>=? AND version=?），防并发超卖
            int rows = stockMapper.lockStock(item.getSkuId(), item.getQuantity(), stock.getVersion(), now);
            if (rows == 0) {
                // CAS 失败：重查区分库存不足 vs 并发冲突
                InvStock fresh = stockMapper.selectOneByQuery(qw);
                int avail = fresh != null ? fresh.getAvailableStock() : 0;
                if (avail < item.getQuantity()) {
                    InvStatus.INV_STOCK_NOT_ENOUGH.assertThrowResEx(true);
                }
                InvStatus.INV_OPTIMISTIC_LOCK_FAIL.assertThrowResEx(true);
            }
            // S12: 写库存日志（type=2 下单锁定）
            writeLog(item.getSkuId(), 2, item.getQuantity(), null, orderSn,
                    stock.getAvailableStock(), stock.getAvailableStock() - item.getQuantity());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmStock(Long orderId) {
        if (orderId == null) return false;
        // S4: 支付成功后 locked_stock → 实扣（locked_stock 减，总库存减）。跨域取订单明细走 OmsOrderApi 契约。
        List<OmsOrderItemDto> items = omsOrderApi.findOrderItemsByOrderId(orderId);
        if (items == null || items.isEmpty()) return false;
        long now = System.currentTimeMillis();
        for (OmsOrderItemDto item : items) {
            QueryWrapper qw = QueryWrapper.create().eq(InvStock::getSkuId, item.getSkuId());
            InvStock stock = stockMapper.selectOneByQuery(qw);
            if (stock == null) {
                log.warn("confirmStock SKU 库存记录不存在 skuId={} orderId={}", item.getSkuId(), orderId);
                continue;
            }
            int rows = stockMapper.confirmStock(item.getSkuId(), item.getQuantity(), stock.getVersion(), now);
            if (rows == 0) {
                // CAS 失败（locked 不足或版本冲突）：钱已收，不阻断支付成功，仅告警记悬挂待对账
                log.warn("confirmStock CAS 失败 skuId={} qty={} version={} orderId={}", item.getSkuId(), item.getQuantity(), stock.getVersion(), orderId);
            }
            // S12: 写库存日志（type=4 支付确认）
            writeLog(item.getSkuId(), 4, item.getQuantity(), orderId, "支付确认",
                    stock.getAvailableStock(), stock.getAvailableStock());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean releaseStock(String orderSn, List<InvStockDto.StockLockItem> items) {
        if (items == null || items.isEmpty()) return true;
        long now = System.currentTimeMillis();
        for (InvStockDto.StockLockItem item : items) {
            QueryWrapper qw = QueryWrapper.create().eq(InvStock::getSkuId, item.getSkuId());
            InvStock stock = stockMapper.selectOneByQuery(qw);
            if (stock == null) continue; // SKU 库存记录不存在，无锁可释，跳过
            // S2: 真 CAS（WHERE locked_stock>=? AND version=?）
            int rows = stockMapper.releaseStock(item.getSkuId(), item.getQuantity(), stock.getVersion(), now);
            if (rows == 0) {
                // 补偿操作：CAS 失败（locked 不足或版本冲突）仅告警不抛，避免阻断取消流程
                log.warn("releaseStock CAS 失败 skuId={} qty={} version={}", item.getSkuId(), item.getQuantity(), stock.getVersion());
            }
            // S12: 写库存日志（type=3 取消释放）
            writeLog(item.getSkuId(), 3, item.getQuantity(), null, orderSn,
                    stock.getAvailableStock(), stock.getAvailableStock() + item.getQuantity());
        }
        return true;
    }

    @Override
    public int adjustStock(Long skuId, int adjustType, int quantity, String reason) {
        if (skuId == null || skuId <= 0) return -1;
        QueryWrapper qw = QueryWrapper.create().eq(InvStock::getSkuId, skuId);
        InvStock stock = stockMapper.selectOneByQuery(qw);

        if (stock == null) {
            // Auto-create stock record on first adjustment (e.g., during SPU creation)
            stock = new InvStock();
            stock.setId(com.clmcat.qianyu.mall.inv.support.InvSupport.INV_ID_SNOWFLAKE.nextId());
            stock.setSkuId(skuId);
            stock.setAvailableStock(0);
            stock.setLockedStock(0);
            stock.setSafetyStock(0);
            stock.setVersion(0L);
            stock.setCreateTime(System.currentTimeMillis());
            stock.setUpdateTime(System.currentTimeMillis());
            stockMapper.insert(stock);
        }

        int delta;
        if (adjustType == 1) {
            delta = quantity;
        } else {
            delta = -quantity;
            if (stock.getAvailableStock() + delta < 0) return -1;
        }

        int rows = stockMapper.adjustStock(stock.getId(), delta,
                stock.getVersion(), System.currentTimeMillis());
        if (rows == 0) return -1;

        return stock.getAvailableStock() + delta;
    }

    // ==================== Internal methods for ViewBiz ====================

    public void insertStock(InvStock stock) {
        stockMapper.insert(stock);
    }

    public InvStock selectOneBySkuId(Long skuId) {
        QueryWrapper qw = QueryWrapper.create().eq(InvStock::getSkuId, skuId);
        return stockMapper.selectOneByQuery(qw);
    }

    public int lockStockInternal(Long skuId, int quantity, Long version, long updateTime) {
        return stockMapper.lockStock(skuId, quantity, version, updateTime);
    }

    public int confirmStockInternal(Long skuId, int quantity, Long version, long updateTime) {
        return stockMapper.confirmStock(skuId, quantity, version, updateTime);
    }

    public int releaseStockInternal(Long skuId, int quantity, Long version, long updateTime) {
        return stockMapper.releaseStock(skuId, quantity, version, updateTime);
    }

    public int adjustStockInternal(Long id, int delta, Long version, long updateTime) {
        return stockMapper.adjustStock(id, delta, version, updateTime);
    }

    public Page<InvStock> paginate(Page<InvStock> page, QueryWrapper qw) {
        return stockMapper.paginate(page, qw);
    }

    /**
     * S12: 写库存变更日志（type: 1=商家调整 2=下单锁定 3=取消释放 4=支付确认 5=售后释放）。
     * 日志失败不阻断主流程（仅 warn）。
     */
    private void writeLog(Long skuId, int type, int quantity, Long orderId, String remark, int beforeStock, int afterStock) {
        try {
            InvStockLog logEntry = new InvStockLog();
            logEntry.setId(InvSupport.STOCK_LOG_ID_SNOWFLAKE.nextId());
            logEntry.setSkuId(skuId);
            logEntry.setOrderId(orderId);
            logEntry.setType(type);
            logEntry.setQuantity(quantity);
            logEntry.setBeforeStock(beforeStock);
            logEntry.setAfterStock(afterStock);
            logEntry.setRemark(remark);
            logEntry.setArchived(0);
            logEntry.setCreateTime(System.currentTimeMillis());
            stockLogServiceBiz.insertLog(logEntry);
        } catch (Exception e) {
            log.warn("库存日志写入失败 skuId={} type={} error={}", skuId, type, e.getMessage());
        }
    }

    private InvStockDto toDto(InvStock stock) {
        if (stock == null) return null;
        InvStockDto dto = new InvStockDto();
        dto.setId(stock.getId());
        dto.setSkuId(stock.getSkuId());
        dto.setAvailableStock(stock.getAvailableStock());
        dto.setLockedStock(stock.getLockedStock());
        dto.setTotalStock(stock.getAvailableStock() + stock.getLockedStock());
        return dto;
    }
}
