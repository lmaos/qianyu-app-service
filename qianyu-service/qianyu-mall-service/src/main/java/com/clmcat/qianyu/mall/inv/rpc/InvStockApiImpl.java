package com.clmcat.qianyu.mall.inv.rpc;

import com.clmcat.qianyu.mall.api.inv.InvStockApi;
import com.clmcat.qianyu.mall.api.inv.model.dto.InvStockDto;
import com.clmcat.qianyu.mall.inv.mapper.InvStockMapper;
import com.clmcat.qianyu.mall.inv.model.entity.InvStock;
import com.clmcat.qianyu.mall.inv.model.entity.status.InvStatus;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@DubboService
@Service
public class InvStockApiImpl implements InvStockApi {

    @Resource
    private InvStockMapper stockMapper;

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
    public boolean lockStock(String orderSn, List<InvStockDto.StockLockItem> items) {
        if (items == null || items.isEmpty()) return false;
        for (InvStockDto.StockLockItem item : items) {
            QueryWrapper qw = QueryWrapper.create().eq(InvStock::getSkuId, item.getSkuId());
            InvStock stock = stockMapper.selectOneByQuery(qw);
            InvStatus.INV_SKU_NOT_FOUND.assertThrowResEx(stock == null);
            InvStatus.INV_STOCK_NOT_ENOUGH.assertThrowResEx(stock.getAvailableStock() < item.getQuantity());
            stock.setAvailableStock(stock.getAvailableStock() - item.getQuantity());
            stock.setLockedStock(stock.getLockedStock() + item.getQuantity());
            stock.setUpdateTime(System.currentTimeMillis());
            stockMapper.update(stock);
        }
        return true;
    }

    @Override
    public boolean confirmStock(Long orderId) {
        return true;
    }

    @Override
    public boolean releaseStock(String orderSn, List<InvStockDto.StockLockItem> items) {
        if (items == null || items.isEmpty()) return true;
        for (InvStockDto.StockLockItem item : items) {
            QueryWrapper qw = QueryWrapper.create().eq(InvStock::getSkuId, item.getSkuId());
            InvStock stock = stockMapper.selectOneByQuery(qw);
            if (stock != null) {
                stock.setLockedStock(Math.max(0, stock.getLockedStock() - item.getQuantity()));
                stock.setAvailableStock(stock.getAvailableStock() + item.getQuantity());
                stock.setUpdateTime(System.currentTimeMillis());
                stockMapper.update(stock);
            }
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
