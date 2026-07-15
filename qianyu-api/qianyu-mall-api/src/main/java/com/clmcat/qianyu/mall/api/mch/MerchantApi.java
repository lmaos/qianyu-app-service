package com.clmcat.qianyu.mall.api.mch;

import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantPageQueryDTO;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;

public interface MerchantApi {

    MerchantDto getByUserId(Long userId);

    MerchantDto getById(Long merchantId);

    /**
     * 商户身份强校验门禁（B 端商家操作统一入口）：要求当前用户是「已审核通过且生效」的商户。
     * <p>语义：{@code audit_status==1 && status==1} 才返回；否则抛错——
     * <ul>
     *   <li>非商户（无 merchant 记录）→ {@code MCH_NOT_MERCHANT}(407009)</li>
     *   <li>冻结（status=2）→ {@code MCH_MERCHANT_FROZEN}(407002)</li>
     *   <li>待审(audit=0)/被拒(audit=2)/禁用(status=0) → {@code MCH_MERCHANT_NOT_APPROVED}(407003)</li>
     * </ul>
     * <p>供商品上架(pms)、库存(inv)、商家订单(oms)、物流(log)、商家评价(rev) 等 B 端操作统一调用，
     * 收紧「成为商户后才能经营」语义（待审/冻结/禁用一律不可操作）。
     * @return 已生效商户 DTO（非 null）
     */
    MerchantDto requireActiveMerchant(Long userId);

    /**
     * 平台商户分页查询（运营端跨店视角）。
     * @return 分页结果（含当前页 records + total/页码），不再丢 total
     */
    PageResultDTO<MerchantDto> pageMerchants(MerchantPageQueryDTO query);

    /** 更新商户状态（0禁用/1正常/2冻结）。运营端状态管控。 */
    void updateMerchantStatus(Long merchantId, Integer status);

    /**
     * 平台审核商家入驻（迁移自 settleAudit，纠正忽略 userId 越权 bug）。
     * approved=true → auditStatus=1(通过) + status=1(启用) + 同步 store.status=1；
     * approved=false → auditStatus=2(拒绝) + auditRemark=rejectReason。
     */
    void auditMerchant(Long merchantId, Boolean approved, String rejectReason);
}
