package com.clmcat.qianyu.mall.coupon.scheduled;

import com.clmcat.qianyu.mall.coupon.mapper.SmsUserCouponLogMapper;
import com.clmcat.qianyu.mall.coupon.mapper.SmsUserCouponMapper;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsUserCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsUserCouponLog;
import com.clmcat.qianyu.mall.coupon.model.entity.status.CouponStatus;
import com.clmcat.qianyu.mall.coupon.support.CouponSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时扫描过期用户券：status 1→3 + 写 EXPIRE log。
 * 每 5 分钟执行一次。
 */
@Slf4j
@Component
public class CouponExpireTask {

    @Resource
    private SmsUserCouponMapper userCouponMapper;

    @Resource
    private SmsUserCouponLogMapper logMapper;

    @Scheduled(fixedDelay = 300000) // 5 分钟
    public void expireCoupons() {
        long now = System.currentTimeMillis();
        List<SmsUserCoupon> expired = userCouponMapper.selectListByQuery(
                QueryWrapper.create()
                        .where("status = ?", CouponStatus.UC_UNUSED)
                        .and("expire_time < ?", now)
                        .and("deleted = 0")
                        .limit(200));
        if (expired == null || expired.isEmpty()) return;

        int count = 0;
        for (SmsUserCoupon uc : expired) {
            // CAS: status 1→3（防并发）
            SmsUserCoupon update = new SmsUserCoupon();
            update.setStatus(CouponStatus.UC_EXPIRED);
            update.setUpdateTime(now);
            int rows = userCouponMapper.updateByQuery(update,
                    QueryWrapper.create().where("id = ?", uc.getId())
                            .and("status = ?", CouponStatus.UC_UNUSED));
            if (rows > 0) {
                writeExpireLog(uc, now);
                count++;
            }
        }
        if (count > 0) {
            log.info("[coupon-expire] 过期 {} 张用户券", count);
        }
    }

    private void writeExpireLog(SmsUserCoupon uc, long now) {
        SmsUserCouponLog entry = new SmsUserCouponLog();
        entry.setId(CouponSupport.COUPON_LOG_ID_SNOWFLAKE.nextId());
        entry.setUserCouponId(uc.getId());
        entry.setUserId(uc.getUserId());
        entry.setCouponId(uc.getCouponId());
        entry.setAction(CouponStatus.ACTION_EXPIRE);
        entry.setRemark("系统自动过期");
        entry.setCreateTime(now);
        logMapper.insertSelective(entry);
    }
}
