package com.clmcat.qianyu.mall.api.mch;

import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantPageQueryDTO;

import java.util.List;

public interface MerchantApi {

    MerchantDto getByUserId(Long userId);

    MerchantDto getById(Long merchantId);

    /** 平台商户分页查询（运营端跨店视角）。 */
    List<MerchantDto> pageMerchants(MerchantPageQueryDTO query);

    /** 更新商户状态（0禁用/1正常/2冻结）。运营端状态管控。 */
    void updateMerchantStatus(Long merchantId, Integer status);

    /**
     * 平台审核商家入驻（迁移自 settleAudit，纠正忽略 userId 越权 bug）。
     * approved=true → auditStatus=1(通过) + status=1(启用) + 同步 store.status=1；
     * approved=false → auditStatus=2(拒绝) + auditRemark=rejectReason。
     */
    void auditMerchant(Long merchantId, Boolean approved, String rejectReason);
}
