package com.clmcat.qianyu.mall.mch.rpc;

import com.clmcat.framework.webmvc.error.ApiResultException;
import com.clmcat.qianyu.mall.api.msg.MsgApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.mch.mapper.MerchantMapper;
import com.clmcat.qianyu.mall.mch.mapper.MerchantStoreMapper;
import com.clmcat.qianyu.mall.mch.model.entity.Merchant;
import com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.when;

/**
 * 商户身份守卫 {@link MerchantApiImpl#requireActiveMerchant(Long)} 单测（P0）。
 * <p>验证「成为商户后才能经营」语义：仅 audit_status==1 && status==1 放行；
 * 非商户/待审/被拒/冻结/禁用 均被拦（407009/407003/407002）。
 */
@DisplayName("商户身份守卫 MerchantApiImpl.requireActiveMerchant")
@ExtendWith(MockitoExtension.class)
class MerchantApiImplGuardTest {

    private static final Long USER_ID = 5274664548958211L;

    @Mock private MerchantMapper merchantMapper;
    @Mock private MerchantStoreMapper merchantStoreMapper;
    @Mock private MsgApi msgApi;

    @InjectMocks private MerchantApiImpl merchantApi;

    private static Merchant merchant(Integer auditStatus, Integer status) {
        Merchant m = new Merchant();
        m.setId(1001L);
        m.setUserId(USER_ID);
        m.setAuditStatus(auditStatus);
        m.setStatus(status);
        return m;
    }

    private void stub(Merchant m) {
        when(merchantMapper.selectByUserId(USER_ID)).thenReturn(m);
    }

    private void assertBlocked(ThrowingCallable action, MchStatus expected) {
        ApiResultException ex = catchThrowableOfType(action, ApiResultException.class);
        assertThat(ex).as("期望抛出 %s", expected.getState()).isNotNull();
        assertThat(ex.getState()).isEqualTo(expected.getState());
    }

    @Test
    @DisplayName("[正] audit=1 & status=1 → 放行，返回商户 DTO")
    void activeMerchant_returnsDto() {
        stub(merchant(1, 1));
        MerchantDto dto = merchantApi.requireActiveMerchant(USER_ID);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1001L);
    }

    @Test
    @DisplayName("[负] 非商户（无记录）→ MCH_NOT_MERCHANT(407009)")
    void notMerchant_throwsNotFound() {
        stub(null);
        assertBlocked(() -> merchantApi.requireActiveMerchant(USER_ID), MchStatus.MCH_NOT_MERCHANT);
    }

    @Test
    @DisplayName("[负] 待审 audit=0 → MCH_MERCHANT_NOT_APPROVED(407003)")
    void pending_throwsNotApproved() {
        stub(merchant(0, 0));
        assertBlocked(() -> merchantApi.requireActiveMerchant(USER_ID), MchStatus.MCH_MERCHANT_NOT_APPROVED);
    }

    @Test
    @DisplayName("[负] 被拒 audit=2 → MCH_MERCHANT_NOT_APPROVED(407003)")
    void rejected_throwsNotApproved() {
        stub(merchant(2, 0));
        assertBlocked(() -> merchantApi.requireActiveMerchant(USER_ID), MchStatus.MCH_MERCHANT_NOT_APPROVED);
    }

    @Test
    @DisplayName("[负] 冻结 audit=1 & status=2 → MCH_MERCHANT_FROZEN(407002)")
    void frozen_throwsFrozen() {
        stub(merchant(1, 2));
        assertBlocked(() -> merchantApi.requireActiveMerchant(USER_ID), MchStatus.MCH_MERCHANT_FROZEN);
    }

    @Test
    @DisplayName("[负] 禁用 audit=1 & status=0 → MCH_MERCHANT_NOT_APPROVED(407003)")
    void disabled_throwsNotApproved() {
        stub(merchant(1, 0));
        assertBlocked(() -> merchantApi.requireActiveMerchant(USER_ID), MchStatus.MCH_MERCHANT_NOT_APPROVED);
    }
}
