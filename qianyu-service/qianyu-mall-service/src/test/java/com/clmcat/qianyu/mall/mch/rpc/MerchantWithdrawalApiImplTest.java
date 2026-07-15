package com.clmcat.qianyu.mall.mch.rpc;

import com.clmcat.framework.webmvc.error.ApiResultException;
import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.mch.mapper.FundOpLogMapper;
import com.clmcat.qianyu.mall.mch.mapper.MerchantWithdrawalMapper;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantWithdrawal;
import com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus;
import com.mybatisflex.core.query.QueryWrapper;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 提现审批闭环（approve / reject / markTransferred）双 CAS 串行单测。
 * <p>合并门禁 BG-01：approve 仅 0→1 且 withdrawal CAS 与 account CAS 任一失败抛 {@link MchStatus#MCH_WITHDRAWAL_CAS_FAIL}；
 * reject 允许 0/1/2→4，1/2→4 先 refund 后 CAS、0→4 不动账户；markTransferred 仅 2→3/5，transferNo 去重，不动账户。
 * <p>纯 Mockito：{@code accountApi} 字段是具体类型 {@link MerchantAccountApiImpl}，mock 该具体类；
 * {@code merchantApi} 仅 pageByPlatform 用，approve/reject/markTransferred 不触，留空 mock 满足字段注入。
 * <p>{@code @Transactional} 在纯单测里惰性（无 Spring 代理），方法直接执行——验证分支/异常状态码/调用顺序，
 * 真实事务回滚由 BG-02（同事务 op_log）/BG-06（运行时冒烟）验证。
 */
@DisplayName("提现审批 MerchantWithdrawalApiImpl（approve / reject / markTransferred 双 CAS）")
@ExtendWith(MockitoExtension.class)
class MerchantWithdrawalApiImplTest {

    private static final Long WITHDRAWAL_ID = 8001L;
    private static final Long MERCHANT_ID = 1001L;
    private static final BigDecimal AMOUNT = new BigDecimal("50");
    private static final Long VERSION = 5L;
    private static final String TRANSFER_NO = "TN20260714001";

    @Mock
    private MerchantWithdrawalMapper withdrawalMapper;

    @Mock
    private MerchantAccountApiImpl accountApi;

    @Mock
    private MerchantApi merchantApi;

    @Mock
    private FundOpLogMapper fundOpLogMapper;

    @InjectMocks
    private MerchantWithdrawalApiImpl withdrawalApi;

    // ==================== fixtures ====================

    private static MerchantWithdrawal withdrawal(Integer status) {
        MerchantWithdrawal w = new MerchantWithdrawal();
        w.setId(WITHDRAWAL_ID);
        w.setMerchantId(MERCHANT_ID);
        w.setAmount(AMOUNT);
        w.setStatus(status);
        return w;
    }

    private static MerchantAccount account(Long version) {
        MerchantAccount a = new MerchantAccount();
        a.setMerchantId(MERCHANT_ID);
        a.setVersion(version);
        return a;
    }

    /** 断言 action 抛出指定状态的 ApiResultException。 */
    private static void assertFundEx(ThrowingCallable action, MchStatus expected) {
        ApiResultException ex = catchThrowableOfType(action, ApiResultException.class);
        assertThat(ex).as("期望抛出 %s", expected.getState()).isNotNull();
        assertThat(ex.getState()).isEqualTo(expected.getState());
    }

    private void stubWithdrawalCasSuccess() {
        when(withdrawalMapper.updateByQuery(any(MerchantWithdrawal.class), any(QueryWrapper.class))).thenReturn(1);
    }

    // ==================== approve（0→1 + settleForApprove） ====================

    @Nested
    @DisplayName("approve：审核通过 0→1 + 账户结算")
    class Approve {

        @Test
        @DisplayName("[正] 0→1 happy：提现单 CAS 成功后调 settleForApprove")
        void happyPath_status0to1_callsSettle() {
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(0));
            stubWithdrawalCasSuccess();
            when(accountApi.selectAccountByMerchantId(MERCHANT_ID)).thenReturn(account(VERSION));
            when(accountApi.settleForApprove(MERCHANT_ID, AMOUNT, VERSION)).thenReturn(true);

            withdrawalApi.approve(WITHDRAWAL_ID);

            verify(accountApi).settleForApprove(MERCHANT_ID, AMOUNT, VERSION);
            verify(fundOpLogMapper).insert(any()); // BG-02：happy 路径同事务落资金 op_log
        }

        @Test
        @DisplayName("[BG-02] op_log insert 抛错 → 异常传播（真实 @Transactional 下回滚已落账资金 CAS）")
        void opLogInsertFailure_propagates_toRollback() {
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(0));
            stubWithdrawalCasSuccess();
            when(accountApi.selectAccountByMerchantId(MERCHANT_ID)).thenReturn(account(VERSION));
            when(accountApi.settleForApprove(MERCHANT_ID, AMOUNT, VERSION)).thenReturn(true);
            doThrow(new RuntimeException("audit db down")).when(fundOpLogMapper).insert(any());

            // 单测无真实事务管理器，但异常传播即回滚机制：writeFundOpLog 不吞错，approve 抛出 → 同事务资金 CAS 回滚
            assertThatThrownBy(() -> withdrawalApi.approve(WITHDRAWAL_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("audit db down");
        }

        @Test
        @DisplayName("[负] 提现单 CAS 失败（affected=0）→ CAS_FAIL，账户原语不执行")
        void withdrawalCasFail_throwsCasFail_accountUntouched() {
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(0));
            when(withdrawalMapper.updateByQuery(any(MerchantWithdrawal.class), any(QueryWrapper.class))).thenReturn(0);

            assertFundEx(() -> withdrawalApi.approve(WITHDRAWAL_ID), MchStatus.MCH_WITHDRAWAL_CAS_FAIL);

            verify(accountApi, never()).selectAccountByMerchantId(any());
            verify(accountApi, never()).settleForApprove(any(), any(), any());
            verify(fundOpLogMapper, never()).insert(any()); // BG-02：CAS 失败不落 op_log
        }

        @Test
        @DisplayName("[负] 账户 settle CAS 失败 → CAS_FAIL")
        void settleCasFail_throwsCasFail() {
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(0));
            stubWithdrawalCasSuccess();
            when(accountApi.selectAccountByMerchantId(MERCHANT_ID)).thenReturn(account(VERSION));
            when(accountApi.settleForApprove(MERCHANT_ID, AMOUNT, VERSION)).thenReturn(false);

            assertFundEx(() -> withdrawalApi.approve(WITHDRAWAL_ID), MchStatus.MCH_WITHDRAWAL_CAS_FAIL);

            verify(accountApi).settleForApprove(MERCHANT_ID, AMOUNT, VERSION);
        }

        @Test
        @DisplayName("[负] 状态非 0 → STATUS_INVALID，不触 CAS")
        void wrongStatus_throwsStatusInvalid() {
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(2));

            assertFundEx(() -> withdrawalApi.approve(WITHDRAWAL_ID), MchStatus.MCH_WITHDRAWAL_STATUS_INVALID);

            verify(withdrawalMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[负] 提现单不存在 → NOT_FOUND")
        void notFound_throwsNotFound() {
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(null);

            assertFundEx(() -> withdrawalApi.approve(WITHDRAWAL_ID), MchStatus.MCH_WITHDRAWAL_NOT_FOUND);

            verify(withdrawalMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }
    }

    // ==================== reject（0/1/2 → 4） ====================

    @Nested
    @DisplayName("reject：拒绝 0/1/2 → 4（1/2 退款冻结，0 仅改状态）")
    class Reject {

        @Test
        @DisplayName("[正] 1→4：先 refundForReject 后提现单 CAS（顺序）")
        void status1to4_refundsFrozenBeforeWithdrawalCas() {
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(1));
            when(accountApi.selectAccountByMerchantId(MERCHANT_ID)).thenReturn(account(VERSION));
            when(accountApi.refundForReject(MERCHANT_ID, AMOUNT, VERSION)).thenReturn(true);
            stubWithdrawalCasSuccess();

            withdrawalApi.reject(WITHDRAWAL_ID, "不合格");

            InOrder inOrder = inOrder(accountApi, withdrawalMapper);
            inOrder.verify(accountApi).refundForReject(MERCHANT_ID, AMOUNT, VERSION);
            inOrder.verify(withdrawalMapper).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[正] 0→4：不退款，账户原语不调用")
        void status0to4_skipsAccount() {
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(0));
            stubWithdrawalCasSuccess();

            withdrawalApi.reject(WITHDRAWAL_ID, "取消");

            verify(accountApi, never()).selectAccountByMerchantId(any());
            verify(accountApi, never()).refundForReject(any(), any(), any());
            verify(withdrawalMapper).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[正] 2→4：退款冻结")
        void status2to4_refunds() {
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(2));
            when(accountApi.selectAccountByMerchantId(MERCHANT_ID)).thenReturn(account(VERSION));
            when(accountApi.refundForReject(MERCHANT_ID, AMOUNT, VERSION)).thenReturn(true);
            stubWithdrawalCasSuccess();

            withdrawalApi.reject(WITHDRAWAL_ID, "打款失败");

            verify(accountApi).refundForReject(MERCHANT_ID, AMOUNT, VERSION);
        }

        @Test
        @DisplayName("[负] refund 失败 → CAS_FAIL，提现单 CAS 不触达（顺序前置）")
        void refundFail_throwsCasFail_beforeWithdrawalCas() {
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(1));
            when(accountApi.selectAccountByMerchantId(MERCHANT_ID)).thenReturn(account(VERSION));
            when(accountApi.refundForReject(MERCHANT_ID, AMOUNT, VERSION)).thenReturn(false);

            assertFundEx(() -> withdrawalApi.reject(WITHDRAWAL_ID, "不合格"), MchStatus.MCH_WITHDRAWAL_CAS_FAIL);

            verify(withdrawalMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[负] 终态（3）→ STATUS_INVALID")
        void terminalStatus_throwsStatusInvalid() {
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(3));

            assertFundEx(() -> withdrawalApi.reject(WITHDRAWAL_ID, "x"), MchStatus.MCH_WITHDRAWAL_STATUS_INVALID);

            verify(withdrawalMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }
    }

    // ==================== markTransferred（2 → 3/5，不动账户） ====================

    @Nested
    @DisplayName("markTransferred：标记打款 2→3(成功)/5(失败)，transferNo 去重")
    class MarkTransferred {

        @Test
        @DisplayName("[正] success=true → 2→3，transferNo/transferTime 落库")
        void success_status2to3() {
            when(withdrawalMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(2));
            java.util.concurrent.atomic.AtomicReference<MerchantWithdrawal> captured = captureWithdrawalUpdate(1);

            withdrawalApi.markTransferred(WITHDRAWAL_ID, TRANSFER_NO, Boolean.TRUE);

            assertThat(captured.get().getStatus()).isEqualTo(3);
            assertThat(captured.get().getTransferNo()).isEqualTo(TRANSFER_NO);
            assertThat(captured.get().getTransferTime()).isNotNull();
            verify(accountApi, never()).selectAccountByMerchantId(any());
        }

        @Test
        @DisplayName("[正] success=false → 2→5，不设 transferTime")
        void fail_status2to5() {
            when(withdrawalMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(2));
            java.util.concurrent.atomic.AtomicReference<MerchantWithdrawal> captured = captureWithdrawalUpdate(1);

            withdrawalApi.markTransferred(WITHDRAWAL_ID, TRANSFER_NO, Boolean.FALSE);

            assertThat(captured.get().getStatus()).isEqualTo(5);
            assertThat(captured.get().getTransferTime()).isNull();
        }

        @Test
        @DisplayName("[负] transferNo 重复 → TRANSFER_NO_DUPLICATE，不读单不改单")
        void dupTransferNo_throwsDuplicate() {
            when(withdrawalMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(1L);

            assertFundEx(() -> withdrawalApi.markTransferred(WITHDRAWAL_ID, TRANSFER_NO, Boolean.TRUE),
                    MchStatus.MCH_WITHDRAWAL_TRANSFER_NO_DUPLICATE);

            verify(withdrawalMapper, never()).selectOneById(any());
            verify(withdrawalMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }

        @Test
        @DisplayName("[负] 状态非 2 → STATUS_INVALID")
        void wrongStatus_throwsStatusInvalid() {
            when(withdrawalMapper.selectCountByQuery(any(QueryWrapper.class))).thenReturn(0L);
            when(withdrawalMapper.selectOneById(WITHDRAWAL_ID)).thenReturn(withdrawal(1));

            assertFundEx(() -> withdrawalApi.markTransferred(WITHDRAWAL_ID, TRANSFER_NO, Boolean.TRUE),
                    MchStatus.MCH_WITHDRAWAL_STATUS_INVALID);

            verify(withdrawalMapper, never()).updateByQuery(any(), any(QueryWrapper.class));
        }
    }

    /** 捕获 markTransferred/approve/reject 的 withdrawal updateByQuery 实体（stub 返回 affected）。 */
    @SuppressWarnings("SameParameterValue")
    private java.util.concurrent.atomic.AtomicReference<MerchantWithdrawal> captureWithdrawalUpdate(int affected) {
        java.util.concurrent.atomic.AtomicReference<MerchantWithdrawal> ref = new java.util.concurrent.atomic.AtomicReference<>();
        when(withdrawalMapper.updateByQuery(any(MerchantWithdrawal.class), any(QueryWrapper.class))).thenAnswer(inv -> {
            ref.set(inv.getArgument(0));
            return affected;
        });
        return ref;
    }
}
