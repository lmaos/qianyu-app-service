package com.clmcat.qianyu.mall.inv.service;

import com.clmcat.qianyu.mall.inv.model.dto.*;
import com.clmcat.qianyu.mall.inv.model.vo.StockAdjustResultVO;
import com.clmcat.qianyu.mall.inv.model.vo.StockInfoVO;
import com.clmcat.qianyu.mall.inv.model.vo.StockLockResultVO;
import com.clmcat.qianyu.mall.inv.model.vo.StockPageItemVO;
import com.mybatisflex.core.paginate.Page;
import java.util.List;

public interface InvStockViewServiceBiz {

    StockLockResultVO lockStock(StockLockDTO dto);

    void confirmStock(StockConfirmDTO dto);

    void releaseStock(StockReleaseDTO dto);

    List<StockInfoVO> batchQuery(StockBatchQueryDTO dto);

    StockAdjustResultVO adjustStock(long userId, StockAdjustDTO dto);

    Page<StockPageItemVO> stockPage(long userId, StockPageQueryDTO dto);

}