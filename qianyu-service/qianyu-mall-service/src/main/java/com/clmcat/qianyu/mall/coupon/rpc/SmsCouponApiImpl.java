package com.clmcat.qianyu.mall.coupon.rpc;

import com.clmcat.qianyu.mall.api.coupon.SmsCouponApi;
import com.clmcat.qianyu.mall.api.coupon.model.dto.CouponDto;
import com.clmcat.qianyu.mall.api.coupon.model.dto.CouponPageDTO;
import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.coupon.mapper.SmsCouponMapper;
import com.clmcat.qianyu.mall.coupon.mapper.SmsUserCouponLogMapper;
import com.clmcat.qianyu.mall.coupon.mapper.SmsUserCouponMapper;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsUserCoupon;
import com.clmcat.qianyu.mall.coupon.model.entity.SmsUserCouponLog;
import com.clmcat.qianyu.mall.coupon.model.entity.status.CouponStatus;
import com.clmcat.qianyu.mall.coupon.support.CouponSupport;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 优惠券 RPC 实现（模板 CRUD + CAS 核销原语 + 可领券查询）。
 */
@Slf4j
@DubboService
@Service
public class SmsCouponApiImpl implements SmsCouponApi {

    @Resource
    private SmsCouponMapper couponMapper;

    @Resource
    private SmsUserCouponMapper userCouponMapper;

    @Resource
    private SmsUserCouponLogMapper logMapper;

    // ==================== 模板 CRUD ====================

    @Override
    public CouponDto getCouponById(Long id) {
        SmsCoupon c = couponMapper.selectOneById(id);
        return toDto(c);
    }

    @Override
    public PageResultDTO<CouponDto> pageCoupons(CouponPageDTO q) {
        int pageNum = q != null && q.getPageNum() != null && q.getPageNum() > 0 ? q.getPageNum() : 1;
        int pageSize = q != null && q.getPageSize() != null && q.getPageSize() > 0 ? q.getPageSize() : 10;

        QueryWrapper qw = QueryWrapper.create().where("deleted = 0");
        if (q != null) {
            if (q.getMerchantId() != null) qw.and("merchant_id = ?", q.getMerchantId());
            if (q.getStatus() != null) qw.and("status = ?", q.getStatus());
            if (q.getName() != null && !q.getName().isBlank()) qw.and("name LIKE ?", "%" + q.getName() + "%");
        }
        qw.orderBy("create_time DESC");

        Page<SmsCoupon> page = couponMapper.paginate(Page.of(pageNum, pageSize), qw);
        List<CouponDto> records = new ArrayList<>();
        if (page != null && page.getRecords() != null) {
            for (SmsCoupon c : page.getRecords()) records.add(toDto(c));
        }
        return PageResultDTO.<CouponDto>builder()
                .records(records)
                .total(page != null ? page.getTotalRow() : 0)
                .pageNum(pageNum)
                .pageSize(pageSize)
                .build();
    }

    @Override
    public Long createCoupon(CouponDto dto) {
        SmsCoupon c = new SmsCoupon();
        long now = System.currentTimeMillis();
        c.setId(CouponSupport.COUPON_ID_SNOWFLAKE.nextId());
        c.setMerchantId(dto.getMerchantId() != null ? dto.getMerchantId() : 0L);
        c.setName(dto.getName());
        c.setType(dto.getType());
        c.setScopeType(dto.getScopeType() != null ? dto.getScopeType() : 1);
        c.setScopeValue(dto.getScopeValue());
        c.setThreshold(dto.getThreshold());
        c.setDiscountAmount(dto.getDiscountAmount());
        c.setDiscountRate(dto.getDiscountRate());
        c.setTotalCount(dto.getTotalCount());
        c.setRemainCount(dto.getTotalCount()); // 初始剩余=发放总量
        c.setPerLimit(dto.getPerLimit() != null ? dto.getPerLimit() : 1);
        c.setStartTime(dto.getStartTime());
        c.setEndTime(dto.getEndTime());
        c.setStatus(dto.getStatus() != null ? dto.getStatus() : CouponStatus.COUPON_ENABLED);
        c.setCreateTime(now);
        c.setUpdateTime(now);
        c.setDeleted(0);
        couponMapper.insertSelective(c);
        return c.getId();
    }

    @Override
    public void updateCoupon(CouponDto dto) {
        SmsCoupon existing = couponMapper.selectOneById(dto.getId());
        CouponStatus.COUPON_NOT_FOUND.assertThrowResEx(existing == null);
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getType() != null) existing.setType(dto.getType());
        if (dto.getScopeType() != null) existing.setScopeType(dto.getScopeType());
        if (dto.getScopeValue() != null) existing.setScopeValue(dto.getScopeValue());
        if (dto.getThreshold() != null) existing.setThreshold(dto.getThreshold());
        if (dto.getDiscountAmount() != null) existing.setDiscountAmount(dto.getDiscountAmount());
        if (dto.getDiscountRate() != null) existing.setDiscountRate(dto.getDiscountRate());
        if (dto.getTotalCount() != null) existing.setTotalCount(dto.getTotalCount());
        if (dto.getPerLimit() != null) existing.setPerLimit(dto.getPerLimit());
        if (dto.getStartTime() != null) existing.setStartTime(dto.getStartTime());
        if (dto.getEndTime() != null) existing.setEndTime(dto.getEndTime());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        existing.setUpdateTime(System.currentTimeMillis());
        couponMapper.update(existing);
    }

    @Override
    public void deleteCoupon(Long id) {
        SmsCoupon c = couponMapper.selectOneById(id);
        CouponStatus.COUPON_NOT_FOUND.assertThrowResEx(c == null);
        c.setDeleted(1);
        c.setUpdateTime(System.currentTimeMillis());
        couponMapper.update(c);
    }

    @Override
    public void setCouponStatus(Long id, int status) {
        SmsCoupon c = couponMapper.selectOneById(id);
        CouponStatus.COUPON_NOT_FOUND.assertThrowResEx(c == null);
        c.setStatus(status);
        c.setUpdateTime(System.currentTimeMillis());
        couponMapper.update(c);
    }

    // ==================== 可领券查询 ====================

    @Override
    public List<CouponDto> listClaimable(Long merchantId) {
        long now = System.currentTimeMillis();
        QueryWrapper qw = QueryWrapper.create()
                .where("deleted = 0").and("status = 1")
                .and("remain_count > 0")
                .and("start_time <= ?", now)
                .and("end_time >= ?", now);
        if (merchantId != null && merchantId > 0) {
            // 商家领券中心：平台券(merchant_id=0) + 本商家券
            qw.and("(merchant_id = 0 OR merchant_id = ?)", merchantId);
        } else {
            qw.and("merchant_id = 0"); // 仅平台券
        }
        qw.orderBy("create_time DESC");
        List<SmsCoupon> list = couponMapper.selectListByQuery(qw);
        List<CouponDto> result = new ArrayList<>();
        if (list != null) {
            for (SmsCoupon c : list) result.add(toDto(c));
        }
        return result;
    }

    // ==================== P3 核销原语（CAS）====================

    @Override
    public boolean lockAndApplyCoupon(Long userCouponId, Long orderId) {
        long now = System.currentTimeMillis();
        // CAS: status 1→2 + set order_id/use_time（WHERE 未过期）
        SmsUserCoupon update = new SmsUserCoupon();
        update.setStatus(CouponStatus.UC_USED);
        update.setOrderId(orderId);
        update.setUseTime(now);
        update.setUpdateTime(now);
        int rows = userCouponMapper.updateByQuery(update,
                QueryWrapper.create().where("id = ?", userCouponId)
                        .and("status = ?", CouponStatus.UC_UNUSED)
                        .and("expire_time > ?", now));
        if (rows > 0) {
            writeLog(userCouponId, orderId, CouponStatus.ACTION_USE, "下单核销");
            log.info("[coupon] 核销 userCouponId={} orderId={}", userCouponId, orderId);
        }
        return rows > 0;
    }

    @Override
    public boolean rollbackCoupon(Long userCouponId) {
        // CAS: status 2→1 + clear order_id
        SmsUserCoupon uc = userCouponMapper.selectOneById(userCouponId);
        if (uc == null || uc.getStatus() != CouponStatus.UC_USED) return false;
        Long orderId = uc.getOrderId();
        SmsUserCoupon update = new SmsUserCoupon();
        update.setStatus(CouponStatus.UC_UNUSED);
        update.setOrderId(null);
        update.setUseTime(null);
        update.setUpdateTime(System.currentTimeMillis());
        int rows = userCouponMapper.updateByQuery(update,
                QueryWrapper.create().where("id = ?", userCouponId)
                        .and("status = ?", CouponStatus.UC_USED));
        if (rows > 0) {
            writeLog(userCouponId, orderId, CouponStatus.ACTION_ROLLBACK, "订单取消/退款回滚");
            log.info("[coupon] 回滚 userCouponId={} orderId={}", userCouponId, orderId);
        }
        return rows > 0;
    }

    // ==================== helpers ====================

    private void writeLog(Long userCouponId, Long orderId, int action, String remark) {
        SmsUserCoupon uc = userCouponMapper.selectOneById(userCouponId);
        if (uc == null) return;
        SmsUserCouponLog logEntry = new SmsUserCouponLog();
        logEntry.setId(CouponSupport.COUPON_LOG_ID_SNOWFLAKE.nextId());
        logEntry.setUserCouponId(userCouponId);
        logEntry.setUserId(uc.getUserId());
        logEntry.setCouponId(uc.getCouponId());
        logEntry.setAction(action);
        logEntry.setOrderId(orderId);
        logEntry.setRemark(remark);
        logEntry.setCreateTime(System.currentTimeMillis());
        logMapper.insertSelective(logEntry);
    }

    private CouponDto toDto(SmsCoupon c) {
        if (c == null) return null;
        CouponDto d = new CouponDto();
        d.setId(c.getId());
        d.setMerchantId(c.getMerchantId());
        d.setName(c.getName());
        d.setType(c.getType());
        d.setScopeType(c.getScopeType());
        d.setScopeValue(c.getScopeValue());
        d.setThreshold(c.getThreshold());
        d.setDiscountAmount(c.getDiscountAmount());
        d.setDiscountRate(c.getDiscountRate());
        d.setTotalCount(c.getTotalCount());
        d.setRemainCount(c.getRemainCount());
        d.setPerLimit(c.getPerLimit());
        d.setStartTime(c.getStartTime());
        d.setEndTime(c.getEndTime());
        d.setStatus(c.getStatus());
        d.setCreateTime(c.getCreateTime());
        return d;
    }
}
