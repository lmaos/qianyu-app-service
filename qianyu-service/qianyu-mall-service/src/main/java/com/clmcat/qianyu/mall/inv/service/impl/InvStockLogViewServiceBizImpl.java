package com.clmcat.qianyu.mall.inv.service.impl;

import com.clmcat.qianyu.mall.inv.rpc.InvStockLogApiImpl;
import com.clmcat.qianyu.mall.inv.model.dto.StockLogQueryDTO;
import com.clmcat.qianyu.mall.inv.model.entity.InvStockLog;
import com.clmcat.qianyu.mall.inv.model.vo.StockLogItemVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import com.clmcat.qianyu.mall.inv.service.InvStockLogViewServiceBiz;

@Service
public class InvStockLogViewServiceBizImpl implements InvStockLogViewServiceBiz {

    @Resource
    private InvStockLogApiImpl stockLogServiceBiz;

    /**
     * 查询库存日志
     */
    public Page<StockLogItemVO> queryLog(long userId, StockLogQueryDTO dto) {
        int pageNum = dto != null && dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        QueryWrapper qw = QueryWrapper.create();
        if (dto != null && dto.getSkuId() != null) {
            qw.where("sku_id = ?", dto.getSkuId());
        }
        qw.orderBy("create_time DESC");

        Page<InvStockLog> logPage = stockLogServiceBiz.paginate(new Page<>(pageNum, pageSize), qw);
        if (logPage == null || logPage.getRecords() == null) {
            return new Page<>(pageNum, pageSize);
        }

        List<StockLogItemVO> voList = new ArrayList<>();
        for (InvStockLog log : logPage.getRecords()) {
            voList.add(StockLogItemVO.builder()
                    .id(log.getId())
                    .skuId(log.getSkuId())
                    .type(log.getType())
                    .quantity(log.getQuantity())
                    .beforeStock(log.getBeforeStock())
                    .afterStock(log.getAfterStock())
                    .reason(log.getRemark())
                    .createTime(String.valueOf(log.getCreateTime()))
                    .build());
        }

        Page<StockLogItemVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotalRow(logPage.getTotalRow());
        return result;
    }
}
