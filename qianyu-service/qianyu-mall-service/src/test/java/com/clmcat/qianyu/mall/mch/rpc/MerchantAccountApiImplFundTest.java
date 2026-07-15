package com.clmcat.qianyu.mall.mch.rpc;

import com.clmcat.qianyu.mall.mch.mapper.MerchantAccountMapper;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount;
import com.mybatisflex.core.query.QueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 资金三原语（freezeForApply / settleForApprove / refundForReject）双 CAS 正负样本单测。
 * <p>合并门禁 BG-01：覆盖「余额/frozen 不足、version 冲突、CAS affected=0、账户不存在、非法入参」全部返回 false 且不动账，
 * 以及 happy 路径的金额方向性 + version+1。纯 Mockito（@ExtendWith(MockitoExtension.class)），mock {@link MerchantAccountMapper}。
 * <p>{@code @Transactional} 在纯单测里惰性（无 Spring 代理），方法直接执行——这里验证分支与 CAS 语义，真实回滚由 BG-02/BG-06 验证。
 */
@DisplayName("资金三原语 MerchantAccountApiImpl（freeze / settle / refund 双 CAS）")
@ExtendWith(MockitoExtension.class)
class MerchantAccountApiImplFundTest {

    private static final Long MERCHANT_ID = 1001L;

    @Mock
    private MerchantAccountMapper accountMapper;

    @InjectMocks
    private MerchantAccountApiImpl accountApi;

    // ==================== fixtures ====================

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    /** 构造账户 fixture（balance/frozen/version 必填，其余默认）。 */
    private static MerchantAccount account(String balance, String frozen, Long version) {
        MerchantAccount a = new MerchantAccount();
        a.setMerchantId(MERCHANT_ID);
        a.setBalance(balance == null ? null : bd(balance));
        a.setFrozenAmount(frozen == null ? null : bd(frozen));
        a.setVersion(version);
        return a;
    }

    private void stubRead(MerchantAccount a) {
        when(accountMapper.selectByMerchantId(MERCHANT_ID)).thenReturn(a);
    }

    /** 捕获 updateByQuery 收到的 update 实体并断言其 balance/frozen/version。 */
    private MerchantAccount captureUpdate() {
        ArgumentCaptor<MerchantAccount> cap = ArgumentCaptor.forClass(MerchantAccount.class);
        verify(accountMapper).updateByQuery(cap.capture(), any(QueryWrapper.class));
        return cap.getValue();
    }

    // ==================== freezeForApply（balance → frozen） ====================

    @Nested
    @DisplayName("freezeForApply：冻结提现金额（balance-=amount, frozen+=amount, version+1）")
    class FreezeForApply {

        @Test
        @DisplayName("[正] 余额充足 → true，balance↓/frozen↑/version+1")
        void balanceSufficient_returnsTrue_andMovesBalanceToFrozen() {
            stubRead(account("100", "10", 5L));
            when(accountMapper.updateByQuery(any(MerchantAccount.class), any(QueryWrapper.class))).thenReturn(1);

            boolean ok = accountApi.freezeForApply(MERCHANT_ID, bd("50"), 5L);

            assertThat(ok).isTrue();
            MerchantAccount u = captureUpdate();
            assertThat(u.getBalance()).isEqualByComparingTo("50");
            assertThat(u.getFrozenAmount()).isEqualByComparingTo("60");
            assertThat(u.getVersion()).isEqualTo(6L);
        }

        @Test
        @DisplayName("[负] 余额不足 → false，不触 update")
        void balanceInsufficient_returnsFalse_noUpdate() {
            stubRead(account("30", "10", 5L));

            assertThat(accountApi.freezeForApply(MERCHANT_ID, bd("50"), 5L)).isFalse();

            verify(accountMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[负] version 陈旧 → false，不触 update")
        void staleVersion_returnsFalse_noUpdate() {
            stubRead(account("100", "10", 6L)); // 库里已是 6，传入旧 5

            assertThat(accountApi.freezeForApply(MERCHANT_ID, bd("50"), 5L)).isFalse();

            verify(accountMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[负] CAS affected=0（并发冲突）→ false")
        void concurrentCasReturnsZero_returnsFalse() {
            stubRead(account("100", "10", 5L));
            when(accountMapper.updateByQuery(any(MerchantAccount.class), any(QueryWrapper.class))).thenReturn(0);

            assertThat(accountApi.freezeForApply(MERCHANT_ID, bd("50"), 5L)).isFalse();

            verify(accountMapper).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[负] 账户不存在 → false")
        void accountNotFound_returnsFalse() {
            stubRead(null);

            assertThat(accountApi.freezeForApply(MERCHANT_ID, bd("50"), 5L)).isFalse();

            verify(accountMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[负] 非法入参（merchantId/amount/version 缺失或非正）→ false，不查不改")
        void invalidArgs_returnsFalse() {
            assertThat(accountApi.freezeForApply(null, bd("50"), 5L)).isFalse();
            assertThat(accountApi.freezeForApply(MERCHANT_ID, null, 5L)).isFalse();
            assertThat(accountApi.freezeForApply(MERCHANT_ID, bd("0"), 5L)).isFalse();
            assertThat(accountApi.freezeForApply(MERCHANT_ID, bd("-1"), 5L)).isFalse();
            assertThat(accountApi.freezeForApply(MERCHANT_ID, bd("50"), null)).isFalse();

            verify(accountMapper, never()).selectByMerchantId(any());
            verify(accountMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }
    }

    // ==================== settleForApprove（frozen → totalWithdraw） ====================

    @Nested
    @DisplayName("settleForApprove：结算提现（frozen-=amount, totalWithdraw+=amount, version+1）")
    class SettleForApprove {

        @Test
        @DisplayName("[正] frozen 充足 → true，frozen↓/totalWithdraw↑/version+1")
        void frozenSufficient_returnsTrue_decrementsFrozenIncrementsTotalWithdraw() {
            MerchantAccount a = account(null, "50", 5L);
            a.setTotalWithdraw(bd("200"));
            stubRead(a);
            when(accountMapper.updateByQuery(any(MerchantAccount.class), any(QueryWrapper.class))).thenReturn(1);

            boolean ok = accountApi.settleForApprove(MERCHANT_ID, bd("50"), 5L);

            assertThat(ok).isTrue();
            MerchantAccount u = captureUpdate();
            assertThat(u.getFrozenAmount()).isEqualByComparingTo("0");
            assertThat(u.getTotalWithdraw()).isEqualByComparingTo("250");
            assertThat(u.getVersion()).isEqualTo(6L);
        }

        @Test
        @DisplayName("[负] frozen 不足 → false，不触 update")
        void frozenInsufficient_returnsFalse_noUpdate() {
            stubRead(account("100", "10", 5L));

            assertThat(accountApi.settleForApprove(MERCHANT_ID, bd("50"), 5L)).isFalse();

            verify(accountMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[负] version 陈旧 → false，不触 update")
        void staleVersion_returnsFalse_noUpdate() {
            stubRead(account("100", "50", 6L));

            assertThat(accountApi.settleForApprove(MERCHANT_ID, bd("50"), 5L)).isFalse();

            verify(accountMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[负] 账户不存在 / 非法入参 → false")
        void notFoundAndInvalidArgs_returnFalse() {
            stubRead(null);
            assertThat(accountApi.settleForApprove(MERCHANT_ID, bd("50"), 5L)).isFalse();

            assertThat(accountApi.settleForApprove(MERCHANT_ID, bd("50"), null)).isFalse();
            assertThat(accountApi.settleForApprove(MERCHANT_ID, bd("0"), 5L)).isFalse();

            verify(accountMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }
    }

    // ==================== refundForReject（frozen → balance） ====================

    @Nested
    @DisplayName("refundForReject：退还冻结（frozen-=amount, balance+=amount, version+1）")
    class RefundForReject {

        @Test
        @DisplayName("[正] frozen 充足 → true，balance↑/frozen↓/version+1（断言方向性，frozen 不为负）")
        void frozenSufficient_returnsTrue_movesFrozenToBalance() {
            stubRead(account("80", "20", 5L));
            when(accountMapper.updateByQuery(any(MerchantAccount.class), any(QueryWrapper.class))).thenReturn(1);

            boolean ok = accountApi.refundForReject(MERCHANT_ID, bd("20"), 5L);

            assertThat(ok).isTrue();
            MerchantAccount u = captureUpdate();
            assertThat(u.getBalance()).isEqualByComparingTo("100");
            assertThat(u.getFrozenAmount()).isEqualByComparingTo("0");
            assertThat(u.getVersion()).isEqualTo(6L);
        }

        @Test
        @DisplayName("[负] frozen 不足 → false，不触 update（不会回滚成负数）")
        void frozenInsufficient_returnsFalse_noUpdate() {
            stubRead(account("80", "10", 5L));

            assertThat(accountApi.refundForReject(MERCHANT_ID, bd("50"), 5L)).isFalse();

            verify(accountMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[负] version 陈旧 → false，不触 update")
        void staleVersion_returnsFalse_noUpdate() {
            stubRead(account("80", "20", 6L));

            assertThat(accountApi.refundForReject(MERCHANT_ID, bd("20"), 5L)).isFalse();

            verify(accountMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[负] 账户不存在 / 非法入参 → false")
        void notFoundAndInvalidArgs_returnFalse() {
            stubRead(null);
            assertThat(accountApi.refundForReject(MERCHANT_ID, bd("20"), 5L)).isFalse();

            assertThat(accountApi.refundForReject(MERCHANT_ID, bd("20"), null)).isFalse();
            assertThat(accountApi.refundForReject(MERCHANT_ID, bd("0"), 5L)).isFalse();

            verify(accountMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }
    }
}
