package com.clmcat.qianyu.mall.coupon.service.impl;

import com.clmcat.qianyu.mall.coupon.mapper.SmsCouponMapper;
import com.clmcat.qianyu.mall.coupon.mapper.SmsUserCouponLogMapper;
import com.clmcat.qianyu.mall.coupon.mapper.SmsUserCouponMapper;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsUserCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsUserCouponLog;
import com.clmcat.qianyu.mall.coupon.model.entity.status.CouponStatus;
import com.clmcat.qianyu.mall.coupon.model.vo.CouponCardVO;
import com.clmcat.qianyu.mall.coupon.model.vo.UsableCouponVO;
import com.clmcat.qianyu.mall.coupon.model.vo.UserCouponVO;
import com.clmcat.qianyu.mall.coupon.service.CouponViewServiceBiz;
import com.clmcat.qianyu.mall.coupon.support.CouponSupport;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
public class CouponViewServiceBizImpl implements CouponViewServiceBiz {

    @Resource
    private SmsCouponMapper couponMapper;
    @Resource
    private SmsUserCouponMapper userCouponMapper;
    @Resource
    private SmsUserCouponLogMapper logMapper;

    @Override
    public List<CouponCardVO> claimableCoupons(long userId, Long merchantId) {
        long now = System.currentTimeMillis();
        QueryWrapper qw = QueryWrapper.create()
                .where("deleted = 0").and("status = 1")
                .and("remain_count > 0")
                .and("start_time <= ?", now).and("end_time >= ?", now);
        if (merchantId != null && merchantId > 0) {
            qw.and("(merchant_id = 0 OR merchant_id = ?)", merchantId);
        } else {
            qw.and("merchant_id = 0");
        }
        qw.orderBy("create_time DESC");
        List<SmsCoupon> coupons = couponMapper.selectListByQuery(qw);
        List<CouponCardVO> result = new ArrayList<>();
        if (coupons == null) return result;
        for (SmsCoupon c : coupons) {
            int claimed = countUserClaims(userId, c.getId());
            result.add(CouponCardVO.builder()
                    .id(c.getId()).name(c.getName()).type(c.getType())
                    .typeText(typeText(c.getType()))
                    .threshold(c.getThreshold() != null ? c.getThreshold().toPlainString() : "0")
                    .discountText(discountText(c))
                    .remainCount(c.getRemainCount()).perLimit(c.getPerLimit())
                    .endTime(c.getEndTime()).claimed(claimed)
                    .build());
        }
        return result;
    }

    @Override
    public Long claim(long userId, Long couponId) {
        SmsCoupon coupon = couponMapper.selectOneById(couponId);
        CouponStatus.COUPON_NOT_FOUND.assertThrowResEx(coupon == null || coupon.getDeleted() == 1);
        long now = System.currentTimeMillis();
        CouponStatus.COUPON_NOT_USABLE.assertThrowResEx(coupon.getStatus() != CouponStatus.COUPON_ENABLED);
        CouponStatus.COUPON_NOT_USABLE.assertThrowResEx(now < coupon.getStartTime() || now > coupon.getEndTime());

        // 限领校验
        int claimed = countUserClaims(userId, couponId);
        int perLimit = coupon.getPerLimit() != null ? coupon.getPerLimit() : 1;
        CouponStatus.COUPON_LIMIT_EXCEED.assertThrowResEx(claimed >= perLimit);

        // CAS 扣库存：remain_count - 1 WHERE remain_count > 0
        SmsCoupon update = new SmsCoupon();
        update.setRemainCount(coupon.getRemainCount() - 1);
        update.setUpdateTime(now);
        int rows = couponMapper.updateByQuery(update,
                QueryWrapper.create().where("id = ?", couponId)
                        .and("remain_count > 0").and("deleted = 0"));
        CouponStatus.COUPON_SOLD_OUT.assertThrowResEx(rows == 0);

        // 插入用户券
        SmsUserCoupon uc = new SmsUserCoupon();
        uc.setId(CouponSupport.USER_COUPON_ID_SNOWFLAKE.nextId());
        uc.setCouponId(couponId);
        uc.setUserId(userId);
        uc.setSourceType(1); // 主动领取
        uc.setStatus(CouponStatus.UC_UNUSED);
        uc.setExpireTime(coupon.getEndTime());
        uc.setCreateTime(now);
        uc.setUpdateTime(now);
        uc.setDeleted(0);
        userCouponMapper.insertSelective(uc);

        // 记录领取日志
        writeLog(uc.getId(), userId, couponId, CouponStatus.ACTION_CLAIM, null, "主动领取");
        log.info("[coupon] 用户{}领取券{} userCouponId={}", userId, couponId, uc.getId());
        return uc.getId();
    }

    @Override
    public Page<UserCouponVO> myCoupons(long userId, int status, int pageNum, int pageSize) {
        QueryWrapper qw = QueryWrapper.create().where("user_id = ?", userId).and("deleted = 0");
        if (status > 0) qw.and("status = ?", status);
        qw.orderBy("create_time DESC");

        Page<SmsUserCoupon> page = userCouponMapper.paginate(Page.of(pageNum, pageSize), qw);
        List<UserCouponVO> voList = new ArrayList<>();
        if (page == null || page.getRecords() == null) return new Page<>(pageNum, pageSize);

        // 批量取 coupon 详情
        Set<Long> couponIds = new HashSet<>();
        for (SmsUserCoupon uc : page.getRecords()) couponIds.add(uc.getCouponId());
        Map<Long, SmsCoupon> couponMap = batchGetCoupons(couponIds);

        for (SmsUserCoupon uc : page.getRecords()) {
            SmsCoupon c = couponMap.get(uc.getCouponId());
            voList.add(UserCouponVO.builder()
                    .id(uc.getId()).couponId(uc.getCouponId())
                    .name(c != null ? c.getName() : "已删除券")
                    .type(c != null ? c.getType() : null)
                    .typeText(c != null ? typeText(c.getType()) : "")
                    .threshold(c != null && c.getThreshold() != null ? c.getThreshold().toPlainString() : "0")
                    .discountText(c != null ? discountText(c) : "")
                    .status(uc.getStatus()).statusText(ucStatusText(uc.getStatus()))
                    .expireTime(uc.getExpireTime()).createTime(uc.getCreateTime())
                    .build());
        }
        Page<UserCouponVO> result = new Page<>(pageNum, pageSize);
        result.setRecords(voList);
        result.setTotalRow(page.getTotalRow());
        return result;
    }

    @Override
    public List<UsableCouponVO> usableForOrder(long userId, BigDecimal orderAmount) {
        long now = System.currentTimeMillis();
        QueryWrapper qw = QueryWrapper.create()
                .where("user_id = ?", userId).and("deleted = 0")
                .and("status = 1").and("expire_time > ?", now);
        List<SmsUserCoupon> ucs = userCouponMapper.selectListByQuery(qw);
        if (ucs == null || ucs.isEmpty()) return new ArrayList<>();

        Set<Long> couponIds = new HashSet<>();
        for (SmsUserCoupon uc : ucs) couponIds.add(uc.getCouponId());
        Map<Long, SmsCoupon> couponMap = batchGetCoupons(couponIds);

        List<UsableCouponVO> result = new ArrayList<>();
        for (SmsUserCoupon uc : ucs) {
            SmsCoupon c = couponMap.get(uc.getCouponId());
            if (c == null) continue;
            BigDecimal threshold = c.getThreshold() != null ? c.getThreshold() : BigDecimal.ZERO;
            boolean usable = orderAmount.compareTo(threshold) >= 0;
            BigDecimal discount = calcDiscount(c, orderAmount);
            result.add(UsableCouponVO.builder()
                    .userCouponId(uc.getId()).couponId(uc.getCouponId())
                    .name(c.getName()).type(c.getType()).typeText(typeText(c.getType()))
                    .threshold(threshold.toPlainString()).discountText(discountText(c))
                    .discountAmount(discount.toPlainString())
                    .usable(usable).reason(usable ? "" : "未满" + threshold.toPlainString() + "元")
                    .build());
        }
        // 可用的排前面
        result.sort((a, b) -> Boolean.compare(!a.isUsable(), !b.isUsable()));
        return result;
    }

    // ==================== helpers ====================

    private int countUserClaims(long userId, Long couponId) {
        List<SmsUserCoupon> list = userCouponMapper.selectListByQuery(
                QueryWrapper.create().where("user_id = ?", userId)
                        .and("coupon_id = ?", couponId).and("deleted = 0"));
        return list != null ? list.size() : 0;
    }

    private Map<Long, SmsCoupon> batchGetCoupons(Set<Long> ids) {
        Map<Long, SmsCoupon> map = new HashMap<>();
        if (ids == null || ids.isEmpty()) return map;
        for (Long id : ids) {
            SmsCoupon c = couponMapper.selectOneById(id);
            if (c != null) map.put(id, c);
        }
        return map;
    }

    private BigDecimal calcDiscount(SmsCoupon c, BigDecimal orderAmount) {
        if (c.getType() == null) return BigDecimal.ZERO;
        BigDecimal threshold = c.getThreshold() != null ? c.getThreshold() : BigDecimal.ZERO;
        if (orderAmount.compareTo(threshold) < 0) return BigDecimal.ZERO;
        return switch (c.getType()) {
            case 1 -> { // 满减
                BigDecimal amt = c.getDiscountAmount() != null ? c.getDiscountAmount() : BigDecimal.ZERO;
                yield amt.min(orderAmount);
            }
            case 2 -> { // 折扣
                BigDecimal rate = c.getDiscountRate() != null ? c.getDiscountRate() : BigDecimal.TEN;
                yield orderAmount.multiply(BigDecimal.ONE.subtract(rate.divide(BigDecimal.TEN, 4, RoundingMode.HALF_UP)))
                        .setScale(2, RoundingMode.HALF_UP);
            }
            case 3 -> BigDecimal.ZERO; // 免运费（freight 未知，试算返回 0）
            default -> BigDecimal.ZERO;
        };
    }

    private void writeLog(Long userCouponId, Long userId, Long couponId, int action, Long orderId, String remark) {
        SmsUserCouponLog entry = new SmsUserCouponLog();
        entry.setId(CouponSupport.COUPON_LOG_ID_SNOWFLAKE.nextId());
        entry.setUserCouponId(userCouponId);
        entry.setUserId(userId);
        entry.setCouponId(couponId);
        entry.setAction(action);
        entry.setOrderId(orderId);
        entry.setRemark(remark);
        entry.setCreateTime(System.currentTimeMillis());
        logMapper.insertSelective(entry);
    }

    private String typeText(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 1 -> "满减";
            case 2 -> "折扣";
            case 3 -> "免运费";
            default -> "";
        };
    }

    private String ucStatusText(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 1 -> "未使用";
            case 2 -> "已使用";
            case 3 -> "已过期";
            default -> "";
        };
    }

    private String discountText(SmsCoupon c) {
        if (c == null || c.getType() == null) return "";
        String threshold = c.getThreshold() != null ? c.getThreshold().toPlainString() : "0";
        return switch (c.getType()) {
            case 1 -> "减" + (c.getDiscountAmount() != null ? c.getDiscountAmount().toPlainString() : "0") + "元";
            case 2 -> "打" + (c.getDiscountRate() != null ? c.getDiscountRate().toPlainString() : "0") + "折";
            case 3 -> "免运费";
            default -> "";
        };
    }
}
