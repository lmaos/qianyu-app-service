package com.clmcat.qianyu.mall.oms.service;

import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleAuditDTO;
import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleCreateDTO;
import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleQueryDTO;
import com.clmcat.qianyu.mall.oms.model.dto.AfterSaleReturnShipDTO;
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

    Page<AfterSaleSimpleVO> merchantAfterSaleList(Long merchantId, AfterSaleQueryDTO dto);

    /** C 端：买家填退货物流（type=2 退货退款，20 商家同意 → 40 用户已发货）。 */
    void aftersaleReturnShip(Long userId, AfterSaleReturnShipDTO dto);

    /** B 端：商家确认收到退货（type=2，40 用户已发货 → 50 已完成 + 触发退款）。 */
    void aftersaleConfirmReturn(Long merchantId, Long aftersaleId);

    /** B 端：商家填写寄回物流（type=3/4，55 商家已收货 → 70 商家已寄出）。 */
    void aftersaleSendBack(Long merchantId, AfterSaleReturnShipDTO dto);

    /** C 端：买家确认收到换货/维修品（type=3/4，70 商家已寄出 → 50 已完成）。 */
    void aftersaleConfirmReplacement(Long userId, Long aftersaleId);

}