package com.clmcat.qianyu.social.follow.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.social.follow.model.dto.FollowListQueryDto;
import com.clmcat.qianyu.social.follow.model.dto.FollowTargetDto;
import com.clmcat.qianyu.social.follow.model.vo.FollowCountVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowPageVo;
import com.clmcat.qianyu.social.follow.model.vo.FollowRelationVo;
import com.clmcat.qianyu.social.follow.service.FollowViewServiceBiz;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;

@ApiController
@RequestMapping("/api/social/follow")
public class FollowController {
    @Resource
    FollowViewServiceBiz followViewServiceBiz;

    /**
     * 当前登录用户关注目标用户。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 目标用户参数，userId 表示要关注的人
     * @return 是否处理成功
     */
    @RequestMapping("/follow")
    @LoginVerify
    public boolean follow(@Token long userId, @Params FollowTargetDto dto) {
        return followViewServiceBiz.follow(userId, dto);
    }

    /**
     * 当前登录用户取消关注目标用户。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 目标用户参数，userId 表示要取消关注的人
     * @return 是否处理成功
     */
    @RequestMapping("/cancel")
    @LoginVerify
    public boolean cancel(@Token long userId, @Params FollowTargetDto dto) {
        return followViewServiceBiz.cancelFollow(userId, dto);
    }

    /**
     * 查看当前登录用户与目标用户之间的关注关系。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 目标用户参数，userId 表示对方用户
     * @return 关系 VO
     */
    @RequestMapping("/relation")
    @LoginVerify
    public FollowRelationVo relation(@Token long userId, @Params FollowTargetDto dto) {
        return followViewServiceBiz.getRelation(userId, dto);
    }

    /**
     * 查询用户的关注列表。
     *
     * @param dto 查询参数，userId 为被查询用户，nextId 为游标，limit 为分页大小
     * @return 关注列表分页结果
     */
    @RequestMapping("/followee/list")
    public FollowPageVo followList(@Params FollowListQueryDto dto) {
        return followViewServiceBiz.getFollowList(dto);
    }

    /**
     * 查询用户的粉丝列表。
     *
     * @param dto 查询参数，userId 为被查询用户，nextId 为游标，limit 为分页大小
     * @return 粉丝列表分页结果
     */
    @RequestMapping("/follower/list")
    public FollowPageVo followerList(@Params FollowListQueryDto dto) {
        return followViewServiceBiz.getFollowerList(dto);
    }

    /**
     * 查询用户的关注数和粉丝数。
     *
     * @param dto 查询参数，userId 为被查询用户
     * @return 数量 VO
     */
    @RequestMapping("/count")
    public FollowCountVo count(@Params FollowTargetDto dto) {
        return followViewServiceBiz.getFollowCount(dto);
    }
}
