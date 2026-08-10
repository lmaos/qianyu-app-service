package com.clmcat.qianyu;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.payment.api.wallet.model.dto.TransactionDto;
import com.clmcat.qianyu.payment.api.wallet.model.dto.TransactionListDto;
import com.clmcat.qianyu.payment.api.wallet.model.dto.WalletDto;
import com.clmcat.qianyu.payment.wallet.service.WalletServiceBiz;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.function.Executable;
import com.clmcat.framework.webmvc.error.ApiException;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 虚拟货币钱包方法级单元测试。
 * <p>
 * 直接调用 {@link WalletServiceBiz} 方法，不经过 HTTP 层。
 * 测试前需确保：
 * <ul>
 *   <li>MySQL / Redis / Nacos 已启动</li>
 *   <li>user_wallet / transaction_record 表已创建</li>
 * </ul>
 *
 * @author ark-home
 * @date 2026-08-03
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WalletTest {

    @Resource
    private WalletServiceBiz walletServiceBiz;

    // 每次测试运行使用不同的 userId，避免上次运行残留数据影响断言
    private static final long TEST_USER_A = System.currentTimeMillis();
    private static final long TEST_USER_B = TEST_USER_A + 1;
    private static final long AMOUNT_100 = 10000L; // 100.00 虚拟币

    private String genKey(String tag) {
        return "test_" + tag + "_" + UUID.randomUUID();
    }

    /**
     * 1. 收入：首次操作自动创建钱包，并增加余额。
     */
    @Test
    @Order(1)
    void credit() {
        TransactionDto t = walletServiceBiz.credit(TEST_USER_A, AMOUNT_100,
                "test", "order_001", genKey("credit1"));

        assertNotNull(t);
        assertEquals((Long) AMOUNT_100, t.getAmount());
        assertEquals(TransactionDto.TYPE_INCOME, (Integer) t.getTransType());
        assertEquals(TransactionDto.STATUS_SUCCESS, (Integer) t.getStatus());
        assertEquals((Long) AMOUNT_100, t.getBalanceAfter());
        assertEquals((Long) 0L, t.getBalanceBefore());
        assertNotNull(t.getTransNo());

        System.out.println("✅ 收入成功: transNo=" + t.getTransNo()
                + ", balance: " + t.getBalanceBefore() + " → " + t.getBalanceAfter());
    }

    /**
     * 2. 查询余额：确认收入后的余额正确。
     */
    @Test
    @Order(2)
    void getWallet() {
        WalletDto wallet = walletServiceBiz.getWallet(TEST_USER_A);

        assertNotNull(wallet, "钱包不应为空");
        assertEquals(TEST_USER_A, (long) wallet.getUserId());
        assertEquals(AMOUNT_100, (long) wallet.getBalance());
        assertEquals(AMOUNT_100, (long) wallet.getTotalIncome());
        assertEquals(0L, (long) wallet.getTotalExpense());

        System.out.println("✅ 查询余额: balance=" + wallet.getBalance()
                + ", income=" + wallet.getTotalIncome()
                + ", expense=" + wallet.getTotalExpense());
    }

    /**
     * 3. 支出：扣除余额。
     */
    @Test
    @Order(3)
    void deduct() {
        long amount = 3000L; // 30.00
        TransactionDto t = walletServiceBiz.deduct(TEST_USER_A, amount,
                "test", "order_002", genKey("deduct1"));

        assertNotNull(t);
        assertEquals((Long) amount, t.getAmount());
        assertEquals(TransactionDto.TYPE_EXPENSE, (Integer) t.getTransType());
        assertEquals(TransactionDto.STATUS_SUCCESS, (Integer) t.getStatus());
        assertEquals((Long) (AMOUNT_100 - amount), t.getBalanceAfter());
        assertEquals(AMOUNT_100, (long) t.getBalanceBefore());

        // 验证余额已扣减
        WalletDto wallet = walletServiceBiz.getWallet(TEST_USER_A);
        assertEquals((Long) (AMOUNT_100 - amount), wallet.getBalance());
        assertEquals((Long) amount, wallet.getTotalExpense());

        System.out.println("✅ 支出成功: balance: " + t.getBalanceBefore() + " → " + t.getBalanceAfter());
    }

    /**
     * 4. 余额不足时支出应失败。
     */
    @Test
    @Order(4)
    void deductFailWhenInsufficient() {
        long hugeAmount = 99999999L;

        assertThrowsResException(
                () -> walletServiceBiz.deduct(TEST_USER_A, hugeAmount,
                        "test", "order_fail", genKey("deduct_fail1")),
                ResponseStatus.R_ACCOUNT_LESS_MONEY
        );

        // 验证余额未变
        WalletDto wallet = walletServiceBiz.getWallet(TEST_USER_A);
        assertEquals((Long) (AMOUNT_100 - 3000L), wallet.getBalance());

        System.out.println("✅ 余额不足正确拒绝");
    }

    /**
     * 5. 不存在的用户支出应失败（无钱包 = 余额 0）。
     */
    @Test
    @Order(5)
    void deductFailWhenNoWallet() {
        assertThrowsResException(
                () -> walletServiceBiz.deduct(TEST_USER_B, 100L,
                        "test", "order_no_wallet", genKey("deduct_nowallet")),
                ResponseStatus.R_ACCOUNT_LESS_MONEY
        );

        // 确认钱包仍未创建（支出失败不创建钱包）
        WalletDto wallet = walletServiceBiz.getWallet(TEST_USER_B);
        assertNull(wallet);

        System.out.println("✅ 无钱包用户支出正确拒绝");
    }

    /**
     * 6. 幂等键重复：同一 key 第二次调用返回已有结果，不重复加钱。
     */
    @Test
    @Order(6)
    void idempotentKeyDuplicate() {
        String key = genKey("idempotent1");

        TransactionDto t1 = walletServiceBiz.credit(TEST_USER_A, 5000L,
                "test", "idem_001", key);
        Long balanceAfter1 = t1.getBalanceAfter();

        // 同一幂等键再次调用，应返回相同结果，余额不变
        TransactionDto t2 = walletServiceBiz.credit(TEST_USER_A, 5000L,
                "test", "idem_001", key);

        assertEquals(t1.getTransNo(), t2.getTransNo(), "幂等键重复应返回同一流水");
        assertEquals(balanceAfter1, t2.getBalanceAfter(), "余额不应变化");

        // 验证实际余额只加了一次
        WalletDto wallet = walletServiceBiz.getWallet(TEST_USER_A);
        assertEquals(balanceAfter1, wallet.getBalance());

        System.out.println("✅ 幂等键重复正确处理: transNo=" + t1.getTransNo());
    }

    /**
     * 7. 多用户独立钱包互不影响。
     */
    @Test
    @Order(7)
    void multiUser() {
        // 给 TEST_USER_B 充值
        TransactionDto t = walletServiceBiz.credit(TEST_USER_B, 20000L,
                "test", "multi_001", genKey("multi1"));
        assertEquals((Long) 20000L, t.getBalanceAfter());

        // TEST_USER_A 余额不受影响
        WalletDto walletA = walletServiceBiz.getWallet(TEST_USER_A);
        assertNotNull(walletA);
        // A 的余额应该是：初始10000 - 支出3000 + 幂等测试5000 = 12000
        assertEquals((Long) (AMOUNT_100 - 3000L + 5000L), walletA.getBalance());

        // 验证 B 的余额
        WalletDto walletB = walletServiceBiz.getWallet(TEST_USER_B);
        assertEquals((Long) 20000L, walletB.getBalance());

        System.out.println("✅ 多用户钱包隔离: A=" + walletA.getBalance()
                + ", B=" + walletB.getBalance());
    }

    /**
     * 8. 交易流水查询：验证分页和游标。
     */
    @Test
    @Order(8)
    void getTransactions() {
        // 给 TEST_USER_A 再操作几笔
        walletServiceBiz.credit(TEST_USER_A, 1000L, "test", "tx_001", genKey("tx1"));
        walletServiceBiz.deduct(TEST_USER_A, 500L, "test", "tx_002", genKey("tx2"));
        walletServiceBiz.credit(TEST_USER_A, 2000L, "test", "tx_003", genKey("tx3"));

        // 查询第一页
        TransactionListDto page1 = walletServiceBiz.getTransactions(TEST_USER_A, 0L, 5);
        assertNotNull(page1);
        assertFalse(page1.getTransactions().isEmpty());
        assertTrue(page1.getTransactions().size() <= 5);

        // 验证 ID 倒序：第一条 id > 最后一条 id
        if (page1.getTransactions().size() >= 2) {
            long firstId = page1.getTransactions().get(0).getId();
            long lastId = page1.getTransactions().get(page1.getTransactions().size() - 1).getId();
            assertTrue(firstId > lastId, "应倒序排列: firstId=" + firstId + " > lastId=" + lastId);
        }

        // 验证游标
        if (page1.getHasMore() != null && page1.getHasMore()) {
            assertTrue(page1.getCursor() > 0);
            System.out.println("✅ 分页查询: page1=" + page1.getTransactions().size()
                    + ", cursor=" + page1.getCursor() + ", hasMore=true");
        } else {
            System.out.println("✅ 单页查询: count=" + page1.getTransactions().size()
                    + ", hasMore=false");
        }

        // 无流水用户返回空
        TransactionListDto empty = walletServiceBiz.getTransactions(999999L, 0L, 10);
        assertNotNull(empty);
        assertTrue(empty.getTransactions().isEmpty());
        assertFalse(empty.getHasMore());

        System.out.println("✅ 空流水列表正常");
    }

    /**
     * 9. 退款：退回一笔支出，余额恢复。
     */
    @Test
    @Order(9)
    void refund() {
        // 确保有余额（隔离运行时无前置 credit）
        walletServiceBiz.credit(TEST_USER_A, 5000L, "test", "refund_setup", genKey("refund_setup"));

        // 先做一笔支出
        TransactionDto deductTxn = walletServiceBiz.deduct(TEST_USER_A, 2000L,
                "test", "refund_test", genKey("refund_deduct"));
        long balanceAfterDeduct = deductTxn.getBalanceAfter();

        // 退款
        String refundKey = genKey("refund_txn");
        TransactionDto refundTxn = walletServiceBiz.refund(TEST_USER_A, deductTxn.getId(), refundKey);

        assertNotNull(refundTxn);
        assertEquals(TransactionDto.TYPE_INCOME, (Integer) refundTxn.getTransType());
        assertEquals(deductTxn.getAmount(), refundTxn.getAmount());
        assertEquals((Long) balanceAfterDeduct, refundTxn.getBalanceBefore());
        assertEquals((Long) (balanceAfterDeduct + deductTxn.getAmount()), refundTxn.getBalanceAfter());

        // 验证余额恢复
        WalletDto wallet = walletServiceBiz.getWallet(TEST_USER_A);
        assertEquals(refundTxn.getBalanceAfter(), wallet.getBalance());

        // 退款幂等：用同一 key 再次退款应返回相同结果
        TransactionDto refundAgain = walletServiceBiz.refund(TEST_USER_A, deductTxn.getId(), refundKey);
        assertEquals(refundTxn.getTransNo(), refundAgain.getTransNo(), "幂等退款应返回同一流水");

        System.out.println("✅ 退款成功: refund_before=" + refundTxn.getBalanceBefore()
                + " → refund_after=" + refundTxn.getBalanceAfter());
    }

    /**
     * 10. 连续支出：验证原子 SQL 在多次快速操作下的正确性。
     */
    @Test
    @Order(10)
    void rapidDeductSequence() {
        long initBalance = walletServiceBiz.getWallet(TEST_USER_A).getBalance();

        // 连续小额支出
        long[] amounts = {100L, 200L, 300L, 400L, 500L};
        long totalDeduct = 0;
        for (int i = 0; i < amounts.length; i++) {
            walletServiceBiz.deduct(TEST_USER_A, amounts[i],
                    "test", "rapid_" + i, genKey("rapid" + i));
            totalDeduct += amounts[i];
        }

        WalletDto wallet = walletServiceBiz.getWallet(TEST_USER_A);
        assertEquals((Long) (initBalance - totalDeduct), wallet.getBalance());

        System.out.println("✅ 连续支出: init=" + initBalance
                + ", deducted=" + totalDeduct
                + ", final=" + wallet.getBalance());
    }

    /**
     * 11. 参数校验：非法参数应抛异常。
     */
    @Test
    @Order(11)
    void paramValidation() {
        // 金额 <= 0
        assertThrowsResException(
                () -> walletServiceBiz.credit(TEST_USER_A, 0L,
                        "test", "p1", genKey("val1")),
                ResponseStatus.P_VALUE_ERROR
        );
        assertThrowsResException(
                () -> walletServiceBiz.deduct(TEST_USER_A, -100L,
                        "test", "p2", genKey("val2")),
                ResponseStatus.P_VALUE_ERROR
        );

        // userId <= 0
        assertThrowsResException(
                () -> walletServiceBiz.getWallet(0L),
                ResponseStatus.P_VALUE_ERROR
        );

        // bizType 为空
        assertThrowsResException(
                () -> walletServiceBiz.credit(TEST_USER_A, 100L,
                        "", "p3", genKey("val3")),
                ResponseStatus.P_VALUE_ERROR
        );

        // idempotentKey 为空
        assertThrowsResException(
                () -> walletServiceBiz.credit(TEST_USER_A, 100L,
                        "test", "p4", ""),
                ResponseStatus.P_VALUE_ERROR
        );

        System.out.println("✅ 参数校验全部通过");
    }

    // ---- 辅助方法 ----

    /**
     * 断言执行器抛出目标 ResponseStatus 对应的 ApiException。
     */
    private void assertThrowsResException(Executable executable, ResponseStatus expectedStatus) {
        try {
            executable.execute();
            fail("应抛出异常，预期 status=" + expectedStatus.getStatus() + " (" + expectedStatus.getState() + ")");
        } catch (Throwable e) {
            if (e instanceof ApiException apiEx) {
                assertEquals(expectedStatus.getState(), apiEx.getState(),
                        "预期 " + expectedStatus.getState() + "(" + expectedStatus.getStatus() + ")"
                                + "，实际 " + apiEx.getState() + " - " + apiEx.getMessage());
            } else {
                fail("预期 ApiException，实际: " + e.getClass().getName() + ": " + e.getMessage());
            }
        }
    }
}
