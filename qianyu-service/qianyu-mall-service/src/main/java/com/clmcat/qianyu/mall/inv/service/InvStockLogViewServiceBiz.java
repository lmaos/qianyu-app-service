package com.clmcat.qianyu.mall.inv.service;

import com.clmcat.qianyu.mall.inv.model.dto.StockLogQueryDTO;
import com.clmcat.qianyu.mall.inv.model.vo.StockLogItemVO;
import com.mybatisflex.core.paginate.Page;

public interface InvStockLogViewServiceBiz {

    Page<StockLogItemVO> queryLog(long userId, StockLogQueryDTO dto);

}