package com.clmcat.qianyu.mall.oms.service;

import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleAuditDTO;
import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleCreateDTO;
import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleQueryDTO;
import com.clmcat.qianyu.mall.oms.model.vo.AfterSaleCreateVO;
import com.clmcat.qianyu.mall.oms.model.vo.AfterSaleDetailVO;
import com.clmcat.qianyu.mall.oms.model.vo.AfterSaleSimpleVO;
import com.mybatisflex.core.paginate.Page;

public interface OmsAfterSaleViewServiceBiz {

    AfterSaleCreateVO applyAfterSale(Long userId, AfterSaleCreateDTO dto);

    Page<AfterSaleSimpleVO> afterSaleList(Long userId, AfterSaleQueryDTO dto);

    AfterSaleDetailVO afterSaleDetail(Long userId, Long aftersaleId);

    void cancelAfterSale(Long userId, Long aftersaleId);

    void auditAfterSale(Long merchantId, AfterSaleAuditDTO dto);

}