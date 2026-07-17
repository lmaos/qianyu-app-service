package com.clmcat.qianyu.mall.stat.rpc;

import com.clmcat.qianyu.mall.api.stat.StatApi;
import com.clmcat.qianyu.mall.api.stat.model.vo.*;
import com.clmcat.qianyu.mall.mch.mapper.MerchantMapper;
import com.clmcat.qianyu.mall.mch.model.entity.Merchant;
import com.clmcat.qianyu.mall.oms.mapper.OmsOrderMapper;
import com.clmcat.qianyu.mall.oms.mapper.OmsAfterSaleMapper;
import com.clmcat.qianyu.mall.oms.model.entity.OmsOrder;
import com.clmcat.qianyu.mall.oms.model.entity.OmsAfterSale;
import com.clmcat.qianyu.mall.pay.mapper.PayPaymentMapper;
import com.clmcat.qianyu.mall.pay.model.entity.PayPayment;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.coupon.mapper.SmsCouponMapper;
import com.clmcat.qianyu.mall.coupon.mapper.SmsUserCouponMapper;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsUserCoupon;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * 统计聚合 RPC 实现。注入各域 Mapper，用 QueryWrapper selectCountByQuery / selectObjectByQueryAs 做实时聚合。
 */
@Slf4j
@DubboService
@Service
public class StatApiImpl implements StatApi {

    @Resource
    private OmsOrderMapper orderMapper;
    @Resource
    private PayPaymentMapper paymentMapper;
    @Resource
    private PmsSpuMapper spuMapper;
    @Resource
    private MerchantMapper merchantMapper;
    @Resource
    private OmsAfterSaleMapper aftersaleMapper;
    @Resource
    private SmsCouponMapper couponMapper;
    @Resource
    private SmsUserCouponMapper userCouponMapper;

    // ==================== KPI 概览 ====================

    @Override
    public StatOverviewVO overview() {
        long now = System.currentTimeMillis();
        long todayStart = todayStartMs();
        long todayEnd = todayStart + 86400000L;

        StatOverviewVO vo = new StatOverviewVO();

        // 今日 GMV: SUM(pay_amount) WHERE status>=20 AND pay_time∈今日
        vo.setTodayGMV(sumPayAmount(todayStart, todayEnd));

        // 今日订单数
        vo.setTodayOrders(orderMapper.selectCountByQuery(
                QueryWrapper.create().where("deleted = 0")
                        .and("status >= ?", OmsOrder.STATUS_PENDING_SHIP)
                        .and("create_time >= ?", todayStart)
                        .and("create_time < ?", todayEnd)));

        // 今日新增商户
        vo.setTodayNewMerchants(merchantMapper.selectCountByQuery(
                QueryWrapper.create().where("deleted = 0")
                        .and("create_time >= ?", todayStart)
                        .and("create_time < ?", todayEnd)));

        // 待处理事项: 待审商户 + 待审提现(status=0) + 待审SPU(status=4)
        long pendingMerchants = merchantMapper.selectCountByQuery(
                QueryWrapper.create().where("deleted = 0").and("audit_status = 0"));
        long pendingSpu = spuMapper.selectCountByQuery(
                QueryWrapper.create().where("deleted = 0").and("status = 4"));
        vo.setPendingItems(pendingMerchants + pendingSpu);

        // 平台累计 GMV
        vo.setTotalGMV(sumPayAmount(0, now));

        // 平台累计用户（暂用商户数近似；精确需调 user-service RPC）
        vo.setTotalUsers(merchantMapper.selectCountByQuery(
                QueryWrapper.create().where("deleted = 0")));

        return vo;
    }

    // ==================== GMV 趋势 ====================

    @Override
    public List<StatDailyVO> gmvTrend(int days) {
        if (days <= 0) days = 30;
        if (days > 90) days = 90;
        List<StatDailyVO> result = new ArrayList<>();
        long todayStart = todayStartMs();
        for (int i = days - 1; i >= 0; i--) {
            long dayStart = todayStart - (long) i * 86400000L;
            long dayEnd = dayStart + 86400000L;
            StatDailyVO dto = new StatDailyVO();
            dto.setDate(fmtDate(dayStart));
            BigDecimal gmv = sumPayAmount(dayStart, dayEnd);
            dto.setGmv(gmv != null ? gmv : BigDecimal.ZERO);
            long count = orderMapper.selectCountByQuery(
                    QueryWrapper.create().where("deleted = 0")
                            .and("status >= ?", OmsOrder.STATUS_PENDING_SHIP)
                            .and("create_time >= ?", dayStart)
                            .and("create_time < ?", dayEnd));
            dto.setOrderCount(count);
            dto.setAvgPrice(count > 0 ? gmv.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);
            result.add(dto);
        }
        return result;
    }

    // ==================== 订单状态分布 ====================

    @Override
    public List<StatDistVO> orderStatusDist() {
        int[] statuses = {10, 20, 30, 40, 50, 60};
        String[] labels = {"待付款", "待发货", "待收货", "已完成", "已取消", "已关闭"};
        List<StatDistVO> result = new ArrayList<>();
        for (int i = 0; i < statuses.length; i++) {
            long count = orderMapper.selectCountByQuery(
                    QueryWrapper.create().where("deleted = 0").and("status = ?", statuses[i]));
            StatDistVO d = new StatDistVO();
            d.setKey(String.valueOf(statuses[i]));
            d.setLabel(labels[i]);
            d.setCount(count);
            result.add(d);
        }
        return result;
    }

    // ==================== 支付渠道分布 ====================

    @Override
    public List<StatDistVO> payChannelDist(int days) {
        if (days <= 0) days = 7;
        long startTime = todayStartMs() - (long) (days - 1) * 86400000L;
        int[] channels = {1, 2, 3};
        String[] labels = {"微信", "支付宝", "余额"};
        List<StatDistVO> result = new ArrayList<>();
        for (int i = 0; i < channels.length; i++) {
            QueryWrapper qw = QueryWrapper.create()
                    .where("deleted = 0")
                    .and("pay_status = ?", 20) // 成功
                    .and("pay_channel = ?", channels[i])
                    .and("pay_time >= ?", startTime)
                    .select("SUM(amount)");
            BigDecimal amount = paymentMapper.selectObjectByQueryAs(qw, BigDecimal.class);
            long count = paymentMapper.selectCountByQuery(
                    QueryWrapper.create().where("deleted = 0")
                            .and("pay_status = ?", 20)
                            .and("pay_channel = ?", channels[i])
                            .and("pay_time >= ?", startTime));
            StatDistVO d = new StatDistVO();
            d.setKey(String.valueOf(channels[i]));
            d.setLabel(labels[i]);
            d.setCount(count);
            d.setAmount(amount != null ? amount : BigDecimal.ZERO);
            result.add(d);
        }
        return result;
    }

    // ==================== 商户 GMV Top N ====================

    @Override
    public List<StatRankVO> topMerchants(int days, int limit) {
        if (days <= 0) days = 30;
        if (limit <= 0 || limit > 50) limit = 10;
        long startTime = todayStartMs() - (long) (days - 1) * 86400000L;

        // 先聚合 oms_order 按 merchant_id SUM(pay_amount)
        QueryWrapper qw = QueryWrapper.create()
                .select("merchant_id, SUM(pay_amount) as total_amount, COUNT(*) as order_count")
                .where("deleted = 0").and("status >= ?", OmsOrder.STATUS_PENDING_SHIP)
                .and("pay_time >= ?", startTime)
                .groupBy("merchant_id")
                .orderBy("total_amount DESC")
                .limit(limit);
        List<OmsOrder> aggList = orderMapper.selectListByQuery(qw);
        if (aggList == null || aggList.isEmpty()) return new ArrayList<>();

        // 批量取商户名
        Set<Long> mids = new HashSet<>();
        for (OmsOrder o : aggList) mids.add(o.getMerchantId());
        Map<Long, String> nameMap = new HashMap<>();
        for (Long mid : mids) {
            Merchant m = merchantMapper.selectOneById(mid);
            if (m != null) nameMap.put(mid, m.getName());
        }

        List<StatRankVO> result = new ArrayList<>();
        for (OmsOrder o : aggList) {
            StatRankVO r = new StatRankVO();
            r.setId(String.valueOf(o.getMerchantId()));
            r.setName(nameMap.getOrDefault(o.getMerchantId(), "商户" + o.getMerchantId()));
            r.setAmount(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO);
            result.add(r);
        }
        return result;
    }

    // ==================== 商品销量 Top N ====================

    @Override
    public List<StatRankVO> topProducts(int limit) {
        if (limit <= 0 || limit > 50) limit = 10;
        List<PmsSpu> spus = spuMapper.selectListByQuery(
                QueryWrapper.create().where("deleted = 0")
                        .orderBy("sales DESC")
                        .limit(limit));
        if (spus == null) return new ArrayList<>();
        List<StatRankVO> result = new ArrayList<>();
        for (PmsSpu s : spus) {
            StatRankVO r = new StatRankVO();
            r.setId(String.valueOf(s.getId()));
            r.setName(s.getName());
            r.setImage(s.getMainImage());
            r.setSales(s.getSales() != null ? s.getSales() : 0);
            result.add(r);
        }
        return result;
    }

    // ==================== Phase 2: 售后/优惠券/分类统计 ====================

    @Override
    public List<StatDailyVO> aftersaleTrend(int days) {
        if (days <= 0) days = 30;
        if (days > 90) days = 90;
        List<StatDailyVO> result = new ArrayList<>();
        long todayStart = todayStartMs();
        for (int i = days - 1; i >= 0; i--) {
            long dayStart = todayStart - (long) i * 86400000L;
            long dayEnd = dayStart + 86400000L;
            StatDailyVO dto = new StatDailyVO();
            dto.setDate(fmtDate(dayStart));
            long count = aftersaleMapper.selectCountByQuery(
                    QueryWrapper.create().where("deleted = 0")
                            .and("create_time >= ?", dayStart).and("create_time < ?", dayEnd));
            dto.setOrderCount(count);
            dto.setGmv(BigDecimal.ZERO); // 售后无 GMV，复用 orderCount 字段表示售后量
            result.add(dto);
        }
        return result;
    }

    @Override
    public List<StatDistVO> aftersaleTypeDist() {
        int[] types = {1, 2, 3, 4};
        String[] labels = {"仅退款", "退货退款", "换货", "维修"};
        return buildDist(aftersaleMapper, "type", types, labels);
    }

    @Override
    public List<StatDistVO> aftersaleStatusDist() {
        int[] statuses = {10, 20, 30, 40, 50, 55, 60, 70};
        String[] labels = {"待审核", "商家同意", "拒绝", "用户已发货", "已完成", "商家已收货", "已取消", "商家已寄出"};
        return buildDist(aftersaleMapper, "status", statuses, labels);
    }

    @Override
    public List<StatRankVO> couponStats() {
        List<SmsCoupon> coupons = couponMapper.selectListByQuery(
                QueryWrapper.create().where("deleted = 0").orderBy("create_time DESC").limit(20));
        if (coupons == null) return new ArrayList<>();
        List<StatRankVO> result = new ArrayList<>();
        for (SmsCoupon c : coupons) {
            long claimed = userCouponMapper.selectCountByQuery(
                    QueryWrapper.create().where("deleted = 0").and("coupon_id = ?", c.getId()));
            long used = userCouponMapper.selectCountByQuery(
                    QueryWrapper.create().where("deleted = 0").and("coupon_id = ?", c.getId()).and("status = 2"));
            StatRankVO r = new StatRankVO();
            r.setId(String.valueOf(c.getId()));
            r.setName(c.getName());
            r.setSales(claimed);    // 复用 sales 字段表示领取量
            r.setAmount(BigDecimal.valueOf(used)); // 复用 amount 表示核销量
            result.add(r);
        }
        return result;
    }

    @Override
    public List<StatDistVO> categorySalesDist() {
        // 用 pms_spu.sales 按 category_id 聚合（简化：取一级分类名需 JOIN，此处先按 category_id 分组）
        QueryWrapper qw = QueryWrapper.create()
                .select("category_id, SUM(sales) as total_sales")
                .where("deleted = 0").and("status = 1")
                .groupBy("category_id")
                .orderBy("total_sales DESC")
                .limit(10);
        List<PmsSpu> aggList = spuMapper.selectListByQuery(qw);
        if (aggList == null || aggList.isEmpty()) return new ArrayList<>();
        List<StatDistVO> result = new ArrayList<>();
        for (PmsSpu s : aggList) {
            StatDistVO d = new StatDistVO();
            d.setKey(String.valueOf(s.getCategoryId()));
            d.setLabel("分类" + s.getCategoryId());
            d.setCount(s.getSales() != null ? s.getSales() : 0);
            result.add(d);
        }
        return result;
    }

    // ==================== Phase 3: 商家仪表盘增强 ====================

    @Override
    public StatOverviewVO merchantDashboard(long merchantId) {
        long now = System.currentTimeMillis();
        long todayStart = todayStartMs();
        long todayEnd = todayStart + 86400000L;
        StatOverviewVO vo = new StatOverviewVO();

        // 今日订单
        vo.setTodayOrders(orderMapper.selectCountByQuery(
                QueryWrapper.create().where("deleted = 0").and("merchant_id = ?", merchantId)
                        .and("status >= ?", OmsOrder.STATUS_PENDING_SHIP)
                        .and("create_time >= ?", todayStart).and("create_time < ?", todayEnd)));

        // 今日销售额
        vo.setTodaySales(sumPayAmountByMerchant(merchantId, todayStart, todayEnd));

        // 在售商品
        vo.setOnShelfGoods(spuMapper.selectCountByQuery(
                QueryWrapper.create().where("deleted = 0").and("merchant_id = ?", merchantId).and("status = 1")));

        // 店铺评分（avg_score 均值）
        QueryWrapper scoreQw = QueryWrapper.create()
                .select("AVG(avg_score)").where("deleted = 0").and("merchant_id = ?", merchantId).and("status = 1");
        BigDecimal score = spuMapper.selectObjectByQueryAs(scoreQw, BigDecimal.class);
        vo.setShopScore(score != null ? score : BigDecimal.ZERO);

        // 待发货 + 待处理售后
        vo.setPendingShip(orderMapper.selectCountByQuery(
                QueryWrapper.create().where("deleted = 0").and("merchant_id = ?", merchantId).and("status = 20")));
        vo.setPendingAftersale(aftersaleMapper.selectCountByQuery(
                QueryWrapper.create().where("deleted = 0").and("merchant_id = ?", merchantId).and("status = 10")));

        // 近 7 天销售趋势
        List<StatDailyVO> sales7d = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            long dayStart = todayStart - (long) i * 86400000L;
            long dayEnd = dayStart + 86400000L;
            StatDailyVO d = new StatDailyVO();
            d.setDate(fmtDate(dayStart));
            d.setGmv(sumPayAmountByMerchant(merchantId, dayStart, dayEnd));
            sales7d.add(d);
        }
        vo.setSales7d(sales7d);

        // 自有商品 Top 5
        List<PmsSpu> top5 = spuMapper.selectListByQuery(
                QueryWrapper.create().where("deleted = 0").and("merchant_id = ?", merchantId)
                        .orderBy("sales DESC").limit(5));
        List<StatRankVO> topProducts = new ArrayList<>();
        if (top5 != null) {
            for (PmsSpu s : top5) {
                StatRankVO r = new StatRankVO();
                r.setId(String.valueOf(s.getId()));
                r.setName(s.getName());
                r.setImage(s.getMainImage());
                r.setSales(s.getSales() != null ? s.getSales() : 0);
                topProducts.add(r);
            }
        }
        vo.setTopProducts(topProducts);

        return vo;
    }

    // ==================== helpers ====================

    /** 通用分布查询（按某字段分组 COUNT） */
    private List<StatDistVO> buildDist(com.mybatisflex.core.BaseMapper<?> mapper, String column, int[] values, String[] labels) {
        List<StatDistVO> result = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            long count = mapper.selectCountByQuery(
                    QueryWrapper.create().where("deleted = 0").and(column + " = ?", values[i]));
            StatDistVO d = new StatDistVO();
            d.setKey(String.valueOf(values[i]));
            d.setLabel(labels[i]);
            d.setCount(count);
            result.add(d);
        }
        return result;
    }

    /** 商家维度 SUM(pay_amount) */
    private BigDecimal sumPayAmountByMerchant(long merchantId, long start, long end) {
        QueryWrapper qw = QueryWrapper.create()
                .where("deleted = 0").and("status >= ?", OmsOrder.STATUS_PENDING_SHIP)
                .and("merchant_id = ?", merchantId)
                .and("pay_time >= ?", start).and("pay_time < ?", end)
                .select("SUM(pay_amount)");
        BigDecimal result = orderMapper.selectObjectByQueryAs(qw, BigDecimal.class);
        return result != null ? result : BigDecimal.ZERO;
    }

    /** SUM(pay_amount) WHERE status>=20 AND pay_time∈[start,end) */
    private BigDecimal sumPayAmount(long start, long end) {
        QueryWrapper qw = QueryWrapper.create()
                .where("deleted = 0")
                .and("status >= ?", OmsOrder.STATUS_PENDING_SHIP)
                .and("pay_time >= ?", start)
                .and("pay_time < ?", end)
                .select("SUM(pay_amount)");
        BigDecimal result = orderMapper.selectObjectByQueryAs(qw, BigDecimal.class);
        return result != null ? result : BigDecimal.ZERO;
    }

    private long todayStartMs() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        return today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private String fmtDate(long ms) {
        LocalDate d = LocalDate.ofEpochDay(ms / 86400000L);
        return d.toString(); // yyyy-MM-dd
    }
}
