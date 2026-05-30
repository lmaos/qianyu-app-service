package com.clmcat.qianyu.mall.rev.service;

import com.clmcat.qianyu.mall.api.mch.MerchantApi;
import com.clmcat.qianyu.mall.api.mch.model.dto.MerchantDto;
import com.clmcat.qianyu.mall.api.oms.OmsOrderApi;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsOrderDto;
import com.clmcat.qianyu.mall.api.pms.PmsSpuApi;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuDto;
import com.clmcat.qianyu.mall.rev.model.dto.*;
import com.clmcat.qianyu.mall.rev.model.entity.RevReview;
import com.clmcat.qianyu.mall.rev.model.entity.status.RevStatus;
import com.clmcat.qianyu.mall.rev.model.vo.*;
import com.clmcat.qianyu.mall.rev.support.RevSupport;
import com.clmcat.qianyu.user.api.UserApi;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RevReviewViewServiceBiz {

    @Resource
    private RevReviewServiceBiz reviewServiceBiz;

    @Resource
    private RevReviewStatServiceBiz statServiceBiz;

    @DubboReference
    private PmsSpuApi pmsSpuApi;

    @DubboReference
    private OmsOrderApi omsOrderApi;

    @DubboReference
    private MerchantApi merchantApi;

    @DubboReference
    private UserApi userApi;

    /**
     * 提交评价
     * 1. 校验评分 1-5
     * 2. 校验图片数 <= 9
     * 3. 校验内容长度 <= 500
     * 4. 校验未重复评价
     * 5. 批量插入 rev_review（每个 item 一条）
     * 6. 异步更新 rev_review_stat
     */
    public ReviewSubmitResultVO submitReview(long userId, ReviewSubmitDTO dto) {
        RevStatus.REV_ORDER_NOT_COMPLETED.assertThrowResEx(dto == null || RevSupport.isNullOrNonPositive(dto.getOrderId()));
        RevStatus.REV_ORDER_NOT_COMPLETED.assertThrowResEx(dto.getItems() == null || dto.getItems().isEmpty());

        // Validate order exists and is completed (status=40)
        OmsOrderDto orderDto = omsOrderApi.findById(dto.getOrderId());
        RevStatus.REV_ORDER_NOT_COMPLETED.assertThrowResEx(orderDto == null);
        RevStatus.REV_ORDER_NOT_COMPLETED.assertThrowResEx(orderDto.getStatus() == null || orderDto.getStatus() != 40);

        List<Long> reviewIds = new ArrayList<>();

        for (ReviewItemDTO item : dto.getItems()) {
            // 校验评分 1-5
            RevStatus.REV_SCORE_INVALID.assertThrowResEx(
                    item.getScore() == null || item.getScore() < 1 || item.getScore() > 5);
            // 校验图片数 <= 9
            if (item.getImages() != null) {
                RevStatus.REV_IMAGE_LIMIT_EXCEED.assertThrowResEx(item.getImages().size() > 9);
            }
            // 校验内容长度 <= 500
            if (item.getContent() != null) {
                RevStatus.REV_CONTENT_TOO_LONG.assertThrowResEx(item.getContent().length() > 500);
            }
            // 校验未重复评价
            int existCount = reviewServiceBiz.countByOrderItemId(item.getOrderItemId());
            RevStatus.REV_ALREADY_REVIEWED.assertThrowResEx(existCount > 0);

            // 构建实体并插入
            RevReview review = RevSupport.newReview(
                    userId, dto.getOrderId(), 0L, // merchantId 需要 JOIN 获取，此处先填 0
                    item.getOrderItemId(), item.getSpuId(), item.getSkuId(), null,
                    item.getScore(), item.getContent(), item.getImages(),
                    item.getAnonymous()
            );
            reviewServiceBiz.insertSelective(review);
            reviewIds.add(review.getId());
        }

        // 异步更新统计（简化处理：直接同步调用）
        for (ReviewItemDTO item : dto.getItems()) {
            statServiceBiz.refreshStat(item.getSpuId());
        }

        return ReviewSubmitResultVO.builder().reviewIds(reviewIds).build();
    }

    /**
     * 商品评价列表
     * 1. 按 spuId 分页查询（仅 status=1 正常评价）
     * 2. 匿名时隐藏用户信息
     */
    public Page<ReviewItemVO> getReviewList(ReviewListQueryDTO dto) {
        RevStatus.REV_SCORE_INVALID.assertThrowResEx(dto == null || RevSupport.isNullOrNonPositive(dto.getSpuId()));

        int pageNum = dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        QueryWrapper qw = QueryWrapper.create()
                .where("spu_id = ?", dto.getSpuId())
                .and("status = 1")
                .and("deleted = 0");

        if (dto.getScore() != null) {
            if (dto.getScore() == 1) {
                qw.and("score <= 2");
            } else if (dto.getScore() == 2) {
                qw.and("score = 3");
            } else if (dto.getScore() == 3) {
                qw.and("score >= 4");
            } else if (dto.getScore() == 4) {
                qw.and("images IS NOT NULL AND JSON_LENGTH(images) > 0");
            }
        }

        String sortField = dto.getSortField();
        if ("score".equals(sortField)) {
            qw.orderBy("score DESC, create_time DESC");
        } else {
            qw.orderBy("create_time DESC");
        }

        Page<RevReview> result = reviewServiceBiz.paginate(new Page<>(pageNum, pageSize), qw);
        if (result == null || result.getRecords() == null) {
            return new Page<>(pageNum, pageSize);
        }

        List<ReviewItemVO> voList = new ArrayList<>();
        for (RevReview review : result.getRecords()) {
            voList.add(toReviewItemVO(review));
        }

        Page<ReviewItemVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(voList);
        voPage.setTotalRow(result.getTotalRow());
        return voPage;
    }

    /**
     * 我的评价列表
     * 1. 按 userId 分页查询 rev_review
     */
    public Page<MyReviewItemVO> getMyReviewList(long userId, MyReviewQueryDTO dto) {
        int pageNum = dto != null && dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        QueryWrapper qw = QueryWrapper.create()
                .eq(RevReview::getUserId, userId)
                .eq(RevReview::getDeleted, 0)
                .orderBy(RevReview::getCreateTime, false);

        Page<RevReview> page = new Page<>(pageNum, pageSize);
        Page<RevReview> result = reviewServiceBiz.paginate(page, qw);
        if (result == null || result.getRecords() == null) {
            return new Page<>(pageNum, pageSize);
        }

        List<MyReviewItemVO> voList = new ArrayList<>();
        for (RevReview review : result.getRecords()) {
            voList.add(toMyReviewItemVO(review));
        }

        Page<MyReviewItemVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(voList);
        voPage.setTotalRow(result.getTotalRow());
        return voPage;
    }

    /**
     * 商家评价列表
     * 1. 根据 userId 获取 merchantId
     * 2. 按 merchantId 分页查询
     * 3. 不限制 status，展示所有评价
     */
    public Page<ReviewItemVO> getMerchantReviewList(long userId, MerchantReviewQueryDTO dto) {
        // Resolve merchantId from userId via MCH module
        MerchantDto merchantDto = merchantApi.getByUserId(userId);
        long merchantId = merchantDto != null ? merchantDto.getId() : userId;

        int pageNum = dto != null && dto.getPageNum() != null && dto.getPageNum() > 0 ? dto.getPageNum() : 1;
        int pageSize = dto != null && dto.getPageSize() != null && dto.getPageSize() > 0 ? dto.getPageSize() : 10;

        QueryWrapper qw = QueryWrapper.create()
                .where("merchant_id = ?", merchantId)
                .and("deleted = 0");
        if (dto != null && dto.getSpuId() != null) {
            qw.and("spu_id = ?", dto.getSpuId());
        }
        if (dto != null && dto.getScore() != null) {
            qw.and("score = ?", dto.getScore());
        }
        if (dto != null && dto.getHasReply() != null) {
            if (dto.getHasReply()) {
                qw.and("reply_content IS NOT NULL");
            } else {
                qw.and("reply_content IS NULL");
            }
        }
        qw.orderBy("create_time DESC");

        Page<RevReview> result = reviewServiceBiz.paginate(new Page<>(pageNum, pageSize), qw);
        if (result == null || result.getRecords() == null) {
            return new Page<>(pageNum, pageSize);
        }

        List<ReviewItemVO> voList = new ArrayList<>();
        for (RevReview review : result.getRecords()) {
            voList.add(toReviewItemVO(review));
        }

        Page<ReviewItemVO> voPage = new Page<>(pageNum, pageSize);
        voPage.setRecords(voList);
        voPage.setTotalRow(result.getTotalRow());
        return voPage;
    }

    /**
     * 商家回复评价
     * 1. 校验评价存在
     * 2. 校验归属商家
     * 3. 校验未重复回复
     * 4. 校验回复长度 <= 200
     * 5. 更新 reply_content + reply_time
     */
    public void replyReview(long userId, ReviewReplyDTO dto) {
        RevStatus.REV_REVIEW_NOT_FOUND.assertThrowResEx(dto == null || RevSupport.isNullOrNonPositive(dto.getReviewId()));

        RevReview review = reviewServiceBiz.selectOneById(dto.getReviewId());
        RevStatus.REV_REVIEW_NOT_FOUND.assertThrowResEx(review == null);

        // Resolve merchantId from userId via MCH module
        MerchantDto merchantDto = merchantApi.getByUserId(userId);
        long merchantId = merchantDto != null ? merchantDto.getId() : userId;
        RevStatus.REV_REVIEW_NOT_BELONG_MERCHANT.assertThrowResEx(!review.getMerchantId().equals(merchantId));
        RevStatus.REV_ALREADY_REPLIED.assertThrowResEx(review.getReplyContent() != null);

        if (dto.getContent() != null) {
            RevStatus.REV_REPLY_CONTENT_TOO_LONG.assertThrowResEx(dto.getContent().length() > 200);
        }

        review.setReplyContent(dto.getContent());
        review.setReplyTime(System.currentTimeMillis());
        review.setUpdateTime(System.currentTimeMillis());
        reviewServiceBiz.updateReview(review);
    }

    /**
     * RevReview -> ReviewItemVO
     */
    private ReviewItemVO toReviewItemVO(RevReview review) {
        if (review == null) {
            return null;
        }

        boolean isAnonymous = review.getIsAnonymous() != null && review.getIsAnonymous() == 1;

        // 匿名时隐藏用户信息，非匿名时通过 UserApi RPC 获取
        Long showUserId = isAnonymous ? null : review.getUserId();
        String userNick = isAnonymous ? "匿名用户" : null;
        String userAvatar = isAnonymous ? null : null;
        if (!isAnonymous && review.getUserId() != null) {
            try {
                RpcUserInfoDto userInfo = userApi.getUserInfo(review.getUserId());
                if (userInfo != null) {
                    userNick = userInfo.getNickname();
                    userAvatar = userInfo.getAvatar();
                }
            } catch (Exception e) {
                // User RPC failure should not block review display
            }
        }

        // 商家回复
        MerchantReplyVO merchantReply = null;
        if (review.getReplyContent() != null) {
            merchantReply = MerchantReplyVO.builder()
                    .content(review.getReplyContent())
                    .createTime(RevSupport.formatTime(review.getReplyTime()))
                    .build();
        }

        // Lookup SPU name via PMS module
        String spuName = null;
        if (review.getSpuId() != null) {
            PmsSpuDto spuDto = pmsSpuApi.getById(review.getSpuId());
            if (spuDto != null) {
                spuName = spuDto.getName();
            }
        }

        return ReviewItemVO.builder()
                .id(review.getId())
                .userId(showUserId)
                .userNick(userNick)
                .userAvatar(userAvatar)
                .spuId(review.getSpuId())
                .spuName(spuName)
                .skuSpecs(review.getSkuName())
                .score(review.getScore())
                .content(review.getContent())
                .images(review.getImages())
                .merchantReply(merchantReply)
                .createTime(RevSupport.formatTime(review.getCreateTime()))
                .anonymous(isAnonymous)
                .build();
    }

    /**
     * RevReview -> MyReviewItemVO
     */
    private MyReviewItemVO toMyReviewItemVO(RevReview review) {
        if (review == null) {
            return null;
        }

        // 商家回复
        MerchantReplyVO merchantReply = null;
        if (review.getReplyContent() != null) {
            merchantReply = MerchantReplyVO.builder()
                    .content(review.getReplyContent())
                    .createTime(RevSupport.formatTime(review.getReplyTime()))
                    .build();
        }

        // Lookup orderSn via OMS module
        String orderSn = null;
        if (review.getOrderId() != null) {
            var orderDto = omsOrderApi.findById(review.getOrderId());
            if (orderDto != null) {
                orderSn = orderDto.getOrderNo();
            }
        }

        // Lookup SPU info via PMS module
        String spuName = null;
        String spuImage = null;
        if (review.getSpuId() != null) {
            PmsSpuDto spuDto = pmsSpuApi.getById(review.getSpuId());
            if (spuDto != null) {
                spuName = spuDto.getName();
                spuImage = spuDto.getMainImage();
            }
        }

        return MyReviewItemVO.builder()
                .id(review.getId())
                .orderId(review.getOrderId())
                .orderSn(orderSn)
                .spuId(review.getSpuId())
                .spuName(spuName)
                .spuImage(spuImage)
                .skuSpecs(review.getSkuName())
                .score(review.getScore())
                .content(review.getContent())
                .images(review.getImages())
                .merchantReply(merchantReply)
                .createTime(RevSupport.formatTime(review.getCreateTime()))
                .build();
    }
}
