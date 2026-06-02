package com.clmcat.qianyu.mall.inv.rpc;

import com.clmcat.qianyu.mall.api.inv.InvStockLogApi;
import com.clmcat.qianyu.mall.inv.mapper.InvStockLogMapper;
import com.clmcat.qianyu.mall.inv.model.entity.InvStockLog;
import com.clmcat.qianyu.mall.inv.support.InvSupport;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@DubboService
@Service
public class InvStockLogApiImpl implements InvStockLogApi {

    @Resource
    private InvStockLogMapper stockLogMapper;

    @Override
    public void addLog(Long skuId, Integer type, Integer quantity, Long orderId, String reason) {
        InvStockLog log = new InvStockLog();
        log.setId(InvSupport.INV_ID_SNOWFLAKE.nextId());
        log.setSkuId(skuId);
        log.setOrderId(orderId);
        log.setType(type);
        log.setQuantity(quantity);
        log.setRemark(reason);
        log.setCreateTime(System.currentTimeMillis());
        stockLogMapper.insertSelective(log);
    }

    // ==================== Internal methods for ViewBiz ====================

    public java.util.List<InvStockLog> selectListByQuery(com.mybatisflex.core.query.QueryWrapper qw) {
        return stockLogMapper.selectListByQuery(qw);
    }

    public void insertLog(InvStockLog log) {
        stockLogMapper.insert(log);
    }

    public com.mybatisflex.core.paginate.Page<InvStockLog> paginate(
            com.mybatisflex.core.paginate.Page<InvStockLog> page, com.mybatisflex.core.query.QueryWrapper qw) {
        return stockLogMapper.paginate(page, qw);
    }
}
