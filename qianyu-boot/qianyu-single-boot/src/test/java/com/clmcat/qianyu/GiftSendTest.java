package com.clmcat.qianyu;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.framework.webmvc.error.ApiException;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftSendRequest;
import com.clmcat.qianyu.gift.api.gift.model.dto.GiftSendResult;
import com.clmcat.qianyu.gift.gift.mapper.GiftBlindboxDropMapper;
import com.clmcat.qianyu.gift.gift.mapper.GiftConfigMapper;
import com.clmcat.qianyu.gift.gift.model.entity.GiftBlindboxDrop;
import com.clmcat.qianyu.gift.gift.model.entity.GiftConfig;
import com.clmcat.qianyu.gift.gift.service.GiftSendServiceBiz;
import com.clmcat.qianyu.payment.api.wallet.WalletApi;
import com.clmcat.qianyu.payment.api.wallet.model.dto.WalletDto;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.function.Executable;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 送礼方法级单元测试。
 * <p>
 * 直接调用 {@link GiftSendServiceBiz}，不经过 HTTP 层。
 * 测试前需确保：
 * <ul>
 *   <li>MySQL / Redis / Nacos 已启动</li>
 *   <li>gift_config / gift_blindbox_drop / gift_send_record 表已创建</li>
 *   <li>user_wallet / transaction_record 表已创建（支付模块）</li>
 * </ul>
 *
 * @author ark-home
 * @date 2026-08-07
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GiftSendTest {

    @Resource
    private GiftSendServiceBiz giftSendServiceBiz;

    @Resource
    private GiftConfigMapper giftConfigMapper;

    @Resource
    private GiftBlindboxDropMapper giftBlindboxDropMapper;

    @Resource
    private WalletApi walletApi;

    private static final long TEST_SENDER = System.currentTimeMillis();
    private static final long TEST_RECEIVER = TEST_SENDER + 1;

    // 测试礼物 ID（雪花）
    private static long normalGiftId;
    private static long blindboxGiftId;
    private static long openedGiftId;

    private static final long GIFT_PRICE = 10000L; // 100.00

    private String genKey(String tag) {
        // 保持 idempotent key 简短：uuid前8位 + tag，避免跟子key拼接过长超VARCHAR(64)
        return "gk_" + tag + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * 1. 初始化测试礼物数据。
     */
    @Test
    @Order(1)
    void setupGiftData() {
        long now = System.currentTimeMillis();

        // 普通礼物
        normalGiftId = now;
        GiftConfig normalGift = GiftConfig.builder()
                .id(normalGiftId)
                .name("测试玫瑰")
                .icon("https://cdn.test/rose.png")
                .animationUrl("https://cdn.test/rose.mp4")
                .price(GIFT_PRICE)
                .giftType(GiftConfig.TYPE_NORMAL)
                .category(GiftConfig.CATEGORY_NORMAL)
                .shelfScenes("live_room,voice_room,private_chat")
                .sortOrder(100)
                .status(GiftConfig.STATUS_ENABLED)
                .commissionRate(5000)
                .createTime(now)
                .updateTime(now)
                .build();
        giftConfigMapper.customInsert(normalGift);

        // 盲盒开出的普通礼物
        openedGiftId = now + 1;
        GiftConfig openedGift = GiftConfig.builder()
                .id(openedGiftId)
                .name("金色玫瑰")
                .icon("https://cdn.test/gold_rose.png")
                .animationUrl("https://cdn.test/gold_rose.mp4")
                .price(50000L) // 500.00，比盲盒贵
                .giftType(GiftConfig.TYPE_NORMAL)
                .category(GiftConfig.CATEGORY_LUXURY)
                .shelfScenes("live_room,voice_room,private_chat")
                .sortOrder(200)
                .status(GiftConfig.STATUS_ENABLED)
                .commissionRate(5000)
                .createTime(now)
                .updateTime(now)
                .build();
        giftConfigMapper.customInsert(openedGift);

        // 盲盒礼物
        blindboxGiftId = now + 2;
        GiftConfig blindbox = GiftConfig.builder()
                .id(blindboxGiftId)
                .name("神秘盲盒")
                .icon("https://cdn.test/blindbox.png")
                .animationUrl("https://cdn.test/blindbox.mp4")
                .price(GIFT_PRICE)
                .giftType(GiftConfig.TYPE_BLINDBOX)
                .category(GiftConfig.CATEGORY_SPECIAL)
                .shelfScenes("live_room")
                .sortOrder(50)
                .status(GiftConfig.STATUS_ENABLED)
                .commissionRate(5000)
                .extraConfig("{\"pool_id\":1}")
                .createTime(now)
                .updateTime(now)
                .build();
        giftConfigMapper.customInsert(blindbox);

        // 盲盒掉落配置
        GiftBlindboxDrop drop = GiftBlindboxDrop.builder()
                .id(now + 10)
                .blindboxGiftId(blindboxGiftId)
                .dropGiftId(openedGiftId)
                .weight(100)
                .status(GiftBlindboxDrop.STATUS_ENABLED)
                .createTime(now)
                .build();
        giftBlindboxDropMapper.insert(drop);

        // 给测试用户充值
        walletApi.credit(TEST_SENDER, 200000L, "test", "setup_gift", genKey("setup"));
        walletApi.credit(TEST_RECEIVER, 10000L, "test", "setup_gift_receiver", genKey("setup_recv"));

        System.out.println("✅ 测试数据初始化完成: normalGift=" + normalGiftId
                + ", blindboxGift=" + blindboxGiftId + ", openedGift=" + openedGiftId);
    }

    /**
     * 2. 正常送礼：扣款 + 结算成功。
     */
    @Test
    @Order(2)
    void sendNormalGift() {
        WalletDto senderBefore = walletApi.getWallet(TEST_SENDER);

        GiftSendRequest req = GiftSendRequest.builder()
                .senderUserId(TEST_SENDER)
                .receiverUserId(TEST_RECEIVER)
                .giftId(normalGiftId)
                .quantity(1)
                .sceneType("live_room")
                .roomId(1001L)
                .payType(1)
                .idempotentKey(genKey("normal1"))
                .build();

        GiftSendResult result = giftSendServiceBiz.sendGift(req);

        assertNotNull(result);
        assertEquals(normalGiftId, (long) result.getGiftId());
        assertEquals("测试玫瑰", result.getGiftName());
        assertNull(result.getActualGiftId()); // 普通礼物无开出
        assertEquals(GIFT_PRICE, (long) result.getTotalAmount());
        // 分佣50% → 结算金额 = 10000 * 5000/10000 = 5000
        assertEquals((Long) 5000L, result.getSettleAmount());

        // 验证余额已扣减
        WalletDto senderAfter = walletApi.getWallet(TEST_SENDER);
        assertEquals((Long) (senderBefore.getBalance() - GIFT_PRICE), senderAfter.getBalance());

        // 验证结算账户已增加
        // (结算账户通过 SettlementApi 查询，这里只验证钱包侧)
        System.out.println("✅ 普通送礼成功: recordId=" + result.getRecordId()
                + ", totalAmount=" + result.getTotalAmount()
                + ", settleAmount=" + result.getSettleAmount());
    }

    /**
     * 3. 盲盒送礼：开出实际礼物，按实际礼物价值结算。
     */
    @Test
    @Order(3)
    void sendBlindboxGift() {
        WalletDto senderBefore = walletApi.getWallet(TEST_SENDER);

        GiftSendRequest req = GiftSendRequest.builder()
                .senderUserId(TEST_SENDER)
                .receiverUserId(TEST_RECEIVER)
                .giftId(blindboxGiftId)
                .quantity(1)
                .sceneType("live_room")
                .roomId(1001L)
                .payType(1)
                .idempotentKey(genKey("blindbox1"))
                .build();

        GiftSendResult result = giftSendServiceBiz.sendGift(req);

        assertNotNull(result);
        assertEquals(blindboxGiftId, (long) result.getGiftId());
        assertEquals("神秘盲盒", result.getGiftName());
        // 盲盒应开出金色玫瑰
        assertNotNull(result.getActualGiftId());
        assertEquals(openedGiftId, (long) result.getActualGiftId());
        assertEquals("金色玫瑰", result.getActualGiftName());
        assertEquals(GIFT_PRICE, (long) result.getTotalAmount()); // 扣的是盲盒价格
        // 结算按金色玫瑰价值：50000 * 5000/10000 = 25000
        assertEquals((Long) 25000L, result.getSettleAmount());

        // 验证余额扣了盲盒价格
        WalletDto senderAfter = walletApi.getWallet(TEST_SENDER);
        assertEquals((Long) (senderBefore.getBalance() - GIFT_PRICE), senderAfter.getBalance());

        System.out.println("✅ 盲盒送礼成功: gift=" + result.getGiftName()
                + ", actualGift=" + result.getActualGiftName()
                + ", settleAmount=" + result.getSettleAmount());
    }

    /**
     * 4. 幂等键重复：同一 key 第二次调用返回已有结果，不重复扣款。
     */
    @Test
    @Order(4)
    void idempotentKeyDuplicate() {
        WalletDto senderBefore = walletApi.getWallet(TEST_SENDER);
        String key = genKey("idempotent1");

        GiftSendRequest req = GiftSendRequest.builder()
                .senderUserId(TEST_SENDER)
                .receiverUserId(TEST_RECEIVER)
                .giftId(normalGiftId)
                .quantity(1)
                .sceneType("live_room")
                .idempotentKey(key)
                .build();

        // 第一次发送
        GiftSendResult r1 = giftSendServiceBiz.sendGift(req);
        Long afterFirst = walletApi.getWallet(TEST_SENDER).getBalance();

        // 同一幂等键再次发送
        GiftSendResult r2 = giftSendServiceBiz.sendGift(req);

        // 应返回相同结果
        assertEquals(r1.getRecordId(), r2.getRecordId(), "幂等键重复应返回同一记录");
        assertEquals(r1.getTotalAmount(), r2.getTotalAmount());
        assertEquals(r1.getSettleAmount(), r2.getSettleAmount());

        // 余额不应再变化
        WalletDto afterSecond = walletApi.getWallet(TEST_SENDER);
        assertEquals(afterFirst, afterSecond.getBalance(), "幂等重放不应再扣款");

        System.out.println("✅ 幂等键重复正确处理: recordId=" + r1.getRecordId());
    }

    /**
     * 5. 余额不足时送礼应失败。
     */
    @Test
    @Order(5)
    void sendFailWhenInsufficientBalance() {
        long hugeAmountGiftId = System.currentTimeMillis() + 100;
        long now = System.currentTimeMillis();

        // 插入一个超贵礼物
        GiftConfig expensiveGift = GiftConfig.builder()
                .id(hugeAmountGiftId)
                .name("超贵礼物")
                .icon("")
                .animationUrl("")
                .price(99999999L)
                .giftType(GiftConfig.TYPE_NORMAL)
                .category(GiftConfig.CATEGORY_LUXURY)
                .shelfScenes("live_room")
                .sortOrder(0)
                .status(GiftConfig.STATUS_ENABLED)
                .commissionRate(5000)
                .createTime(now)
                .updateTime(now)
                .build();
        giftConfigMapper.customInsert(expensiveGift);

        WalletDto before = walletApi.getWallet(TEST_SENDER);

        GiftSendRequest req = GiftSendRequest.builder()
                .senderUserId(TEST_SENDER)
                .receiverUserId(TEST_RECEIVER)
                .giftId(hugeAmountGiftId)
                .quantity(1)
                .sceneType("live_room")
                .idempotentKey(genKey("insufficient1"))
                .build();

        assertThrowsResException(
                () -> giftSendServiceBiz.sendGift(req),
                ResponseStatus.R_ACCOUNT_LESS_MONEY
        );

        // 余额未变
        WalletDto after = walletApi.getWallet(TEST_SENDER);
        assertEquals(before.getBalance(), after.getBalance());

        System.out.println("✅ 余额不足正确拒绝");
    }

    /**
     * 6. 不存在的礼物应失败。
     */
    @Test
    @Order(6)
    void sendFailWhenGiftNotFound() {
        GiftSendRequest req = GiftSendRequest.builder()
                .senderUserId(TEST_SENDER)
                .receiverUserId(TEST_RECEIVER)
                .giftId(99999999L)
                .quantity(1)
                .sceneType("live_room")
                .idempotentKey(genKey("notfound1"))
                .build();

        try {
            giftSendServiceBiz.sendGift(req);
            fail("应抛出异常");
        } catch (ApiException e) {
            // GiftStatus.GIFT_NOT_FOUND → state = "GIFT_NOT_FOUND"
            assertTrue(e.getState().contains("GIFT") || e.getState().contains("ERROR"),
                    "预期礼物相关异常，实际: " + e.getState());
        }

        System.out.println("✅ 礼物不存在正确拒绝");
    }

    /**
     * 7. 参数校验：非法参数应抛异常。
     */
    @Test
    @Order(7)
    void paramValidation() {
        // senderUserId <= 0
        assertThrowsResException(
                () -> giftSendServiceBiz.sendGift(GiftSendRequest.builder()
                        .senderUserId(0L).receiverUserId(TEST_RECEIVER)
                        .giftId(normalGiftId).sceneType("live_room").idempotentKey(genKey("val1")).build()),
                ResponseStatus.P_VALUE_ERROR
        );

        // receiverUserId <= 0
        assertThrowsResException(
                () -> giftSendServiceBiz.sendGift(GiftSendRequest.builder()
                        .senderUserId(TEST_SENDER).receiverUserId(0L)
                        .giftId(normalGiftId).sceneType("live_room").idempotentKey(genKey("val2")).build()),
                ResponseStatus.P_VALUE_ERROR
        );

        // 不能给自己送礼
        assertThrowsResException(
                () -> giftSendServiceBiz.sendGift(GiftSendRequest.builder()
                        .senderUserId(TEST_SENDER).receiverUserId(TEST_SENDER)
                        .giftId(normalGiftId).sceneType("live_room").idempotentKey(genKey("val3")).build()),
                ResponseStatus.P_VALUE_ERROR
        );

        // sceneType 为空
        assertThrowsResException(
                () -> giftSendServiceBiz.sendGift(GiftSendRequest.builder()
                        .senderUserId(TEST_SENDER).receiverUserId(TEST_RECEIVER)
                        .giftId(normalGiftId).sceneType("").idempotentKey(genKey("val4")).build()),
                ResponseStatus.P_VALUE_ERROR
        );

        // idempotentKey 为空
        assertThrowsResException(
                () -> giftSendServiceBiz.sendGift(GiftSendRequest.builder()
                        .senderUserId(TEST_SENDER).receiverUserId(TEST_RECEIVER)
                        .giftId(normalGiftId).sceneType("live_room").idempotentKey("").build()),
                ResponseStatus.P_VALUE_ERROR
        );

        System.out.println("✅ 参数校验全部通过");
    }

    /**
     * 8. 连续多次送礼：验证多次扣款正确性。
     */
    @Test
    @Order(8)
    void multipleSends() {
        WalletDto before = walletApi.getWallet(TEST_SENDER);

        // 给 TEST_SENDER 再充点钱确保够用
        walletApi.credit(TEST_SENDER, 100000L, "test", "multi_credit", genKey("multi_credit"));

        long totalSpent = 0;
        for (int i = 0; i < 5; i++) {
            GiftSendRequest req = GiftSendRequest.builder()
                    .senderUserId(TEST_SENDER)
                    .receiverUserId(TEST_RECEIVER)
                    .giftId(normalGiftId)
                    .quantity(1)
                    .sceneType("live_room")
                    .idempotentKey(genKey("multi" + i))
                    .build();
            GiftSendResult result = giftSendServiceBiz.sendGift(req);
            assertNotNull(result);
            assertEquals(GIFT_PRICE, (long) result.getTotalAmount());
            totalSpent += GIFT_PRICE;
        }

        WalletDto after = walletApi.getWallet(TEST_SENDER);
        long expectedBalance = before.getBalance() + 100000L - totalSpent;
        assertEquals((Long) expectedBalance, after.getBalance());

        System.out.println("✅ 连续送礼: spent=" + totalSpent
                + ", balance=" + after.getBalance());
    }

    // ---- 辅助方法 ----

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
