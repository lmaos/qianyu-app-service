package com.clmcat.qianyu.mall.inv.service.impl;

import com.clmcat.qianyu.mall.inv.rpc.InvStockLogApiImpl;
import com.clmcat.qianyu.mall.inv.rpc.InvStockApiImpl;
import com.clmcat.qianyu.mall.inv.model.dto.*;
import com.clmcat.qianyu.mall.inv.model.entity.InvStock;
import com.clmcat.qianyu.mall.inv.model.entity.InvStockLog;
import com.clmcat.qianyu.mall.inv.model.entity.status.InvStatus;
import com.clmcat.qianyu.mall.inv.model.vo.StockAdjustResultVO;
import com.clmcat.qianyu.mall.inv.model.vo.StockInfoVO;
import com.clmcat.qianyu.mall.inv.model.vo.StockLockFailItemVO;
import com.clmcat.qianyu.mall.inv.model.vo.StockLockResultVO;
import com.clmcat.qianyu.mall.inv.model.vo.StockPageItemVO;
import com.clmcat.qianyu.mall.inv.support.InvSupport;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.clmcat.qianyu.mall.inv.service.InvStockViewServiceBiz;

@Service
public class InvStockViewServiceBizImpl implements InvStockViewServiceBiz {

    @Resource
    private InvStockApiImpl stockServiceBiz;

    @Resource
    private InvStockLogApiImpl stockLogServiceBiz;

    /**
     * 锁定库存
     */
    public StockLockResultVO lockStock(StockLockDTO dto) {
        List<StockLockItem> items = dto.getItems();
        if (items == null || items.isEmpty()) {
            return StockLockResultVO.builder().success(false).failItems(new ArrayList<>()).build();
        }
        List<StockLockFailItemVO> failItems = new ArrayList<>();
        for (StockLockItem item : dto.getItems()) {
            InvStock stock = stockServiceBiz.selectOneBySkuId(item.getSkuId());
            InvStatus.INV_SKU_NOT_FOUND.assertThrowResEx(stock == null);
            InvStatus.INV_STOCK_NOT_ENOUGH.assertThrowResEx(stock.getAvailableStock() < item.getQuantity());
            int rows = stockServiceBiz.lockStockInternal(item.getSkuId(), item.getQuantity(),
                    stock.getVersion(), System.currentTimeMillis());
            if (rows == 0) {
                failItems.add(StockLockFailItemVO.builder()
                        .skuId(item.getSkuId())
                        .available(stock.getAvailableStock())
                        .requested(item.getQuantity())
                        .build());
            }
        }
        return StockLockResultVO.builder()
                .success(failItems.isEmpty())
                .failItems(failItems)
                .build();
    }

    /**
     * 确认库存（支付成功）- 通过订单ID查找库存日志中的锁定记录并确认
     */
    public void confirmStock(StockConfirmDTO dto) {
        // Find locked stock logs by orderId and confirm them
        List<InvStockLog> logs = stockLogServiceBiz.selectListByQuery(
                QueryWrapper.create().eq(InvStockLog::getOrderId, dto.getOrderId()).eq(InvStockLog::getType, 2));
        for (InvStockLog logEntry : logs) {
            InvStock stock = stockServiceBiz.selectOneBySkuId(logEntry.getSkuId());
            if (stock == null) continue;
            stockServiceBiz.confirmStockInternal(logEntry.getSkuId(), Math.abs(logEntry.getQuantity()),
                    stock.getVersion(), System.currentTimeMillis());
        }
    }

    /**
     * 释放库存（取消/退款）
     */
    public void releaseStock(StockReleaseDTO dto) {
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            for (StockReleaseItem item : dto.getItems()) {
                InvStock stock = stockServiceBiz.selectOneBySkuId(item.getSkuId());
                if (stock == null) continue;
                stockServiceBiz.releaseStockInternal(item.getSkuId(), item.getQuantity(),
                        stock.getVersion(), System.currentTimeMillis());
            }
        }
    }

    /**
     * 批量查询库存
     */
    public List<StockInfoVO> batchQuery(StockBatchQueryDTO dto) {
        InvStatus.INV_BATCH_QUERY_LIMIT_EXCEED.assertThrowResEx(dto.getSkuIds().size() > 100);
        List<StockInfoVO> result = new ArrayList<>();
        for (Long skuId : dto.getSkuIds()) {
            InvStock stock = stockServiceBiz.selectOneBySkuId(skuId);
            if (stock != null) {
                result.add(StockInfoVO.builder()
                        .skuId(skuId)
                        .total(stock.getAvailableStock() + stock.getLockedStock())
                        .available(stock.getAvailableStock())
                        .locked(stock.getLockedStock())
                        .build());
            }
        }
        return result;
    }

    /**
     * 商家库存调整（乐观锁）
     */
    public StockAdjustResultVO adjustStock(long userId, StockAdjustDTO dto) {
        InvStock stock = stockServiceBiz.selectOneBySkuId(dto.getSkuId());
        if (stock == null) {
            // Auto-create stock record for new SKU
            stock = new InvStock();
            stock.setId(InvSupport.INV_ID_SNOWFLAKE.nextId());
            stock.setSkuId(dto.getSkuId());
            stock.setAvailableStock(0);
            stock.setLockedStock(0);
            stock.setSafetyStock(0);
            stock.setVersion(0L);
            stock.setCreateTime(System.currentTimeMillis());
            stock.setUpdateTime(System.currentTimeMillis());
            stockServiceBiz.insertStock(stock);
        }
        // TODO: 校验 SKU 归属商家 InvStatus.INV_SKU_NOT_BELONG_MERCHANT.assertThrowResEx(...)

        int beforeStock = stock.getAvailableStock();
        int delta;
        if (dto.getAdjustType() == 1) {
            delta = dto.getQuantity();
        } else {
            delta = -dto.getQuantity();
            InvStatus.INV_ADJUST_QUANTITY_INVALID.assertThrowResEx(beforeStock + delta < 0);
        }

        int rows = stockServiceBiz.adjustStockInternal(stock.getId(), delta,
                stock.getVersion(), System.currentTimeMillis());
        InvStatus.INV_OPTIMISTIC_LOCK_FAIL.assertThrowResEx(rows == 0);

        // 写入库存日志
        InvStockLog log = new InvStockLog();
        log.setId(InvSupport.STOCK_LOG_ID_SNOWFLAKE.nextId());
        log.setSkuId(dto.getSkuId());
        log.setType(1); // 商家调整
        log.setQuantity(delta);
        log.setBeforeStock(beforeStock);
        log.setAfterStock(beforeStock + delta);
        log.setRemark(dto.getReason());
        log.setArchived(0);
        log.setCreateTime(System.currentTimeMillis());
        stockLogServiceBiz.insertLog(log);

        return StockAdjustResultVO.builder()
                .beforeStock(beforeStock)
                .afterStock(beforeStock + delta)
                .build();
    }

    /**
     * B端商家库存分页查询
     */
    public Page<StockPageItemVO> stockPage(long userId, StockPageQueryDTO dto) {
        int pageNum = dto != null && dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        QueryWrapper qw = QueryWrapper.create();
        qw.orderBy("update_time DESC");

        Page<InvStock> stockPage = stockServiceBiz.paginate(Page.of(pageNum, pageSize), qw);
        if (stockPage == null || stockPage.getRecords() == null) {
            return new Page<>(pageNum, pageSize);
        }

        List<StockPageItemVO> voList = new ArrayList<>();
        for (InvStock stock : stockPage.getRecords()) {
            voList.add(StockPageItemVO.builder()
                    .id(stock.getId())
                    .skuId(stock.getSkuId())
                    .availableStock(stock.getAvailableStock())
                    .lockedStock(stock.getLockedStock())
                    .safetyStock(stock.getSafetyStock())
                    .build());
        }

        Page<StockPageItemVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotalRow(stockPage.getTotalRow());
        return result;
    }
}
