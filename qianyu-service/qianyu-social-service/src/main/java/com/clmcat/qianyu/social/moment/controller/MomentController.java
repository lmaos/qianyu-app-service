package com.clmcat.qianyu.social.moment.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.social.moment.model.dto.MomentAuthorQueryDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentIdDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentIdsDto;
import com.clmcat.qianyu.social.moment.model.dto.MomentPublishDto;
import com.clmcat.qianyu.social.moment.model.vo.MomentAuthorPageVo;
import com.clmcat.qianyu.social.moment.model.vo.MomentVo;
import com.clmcat.qianyu.social.moment.service.MomentServiceViewBiz;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@ApiController
@RequestMapping("/api/social/moment")
@LoginVerify
public class MomentController {
    @Resource
    private MomentServiceViewBiz momentServiceViewBiz;

    @RequestMapping("test")
    public String test(@Token long userId) { // @Token 可以从Token获取当前的登录用户ID。
        return "hello world， userId: " + userId;
    }

    /**
     * 发布动态。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 发布参数，包含内容、定位、国家、状态等
     * @return 发布后的动态 VO
     */
    @RequestMapping("/publish")
    public MomentVo publish(@Token long userId, @Params MomentPublishDto dto) {
        return momentServiceViewBiz.publish(userId, dto);
    }

    /**
     * 查询单条动态详情。
     *
     * @param userId 当前查看者ID，来自 Token；用于区分本人查看与他人查看的缓存策略
     * @param dto 查询参数，必须提供 momentId
     * @return 动态详情 VO
     */
    @RequestMapping("/get")
    public MomentVo get(@Token long userId, @Params MomentIdDto dto) {
        return momentServiceViewBiz.getMoment(userId, dto);
    }

    /**
     * 批量查询动态。
     *
     * @param dto 查询参数，可通过 JSON 的 momentIds 或逗号分隔字符串传入多个 momentId
     * @return 动态列表
     */
    @RequestMapping("/list")
    public List<MomentVo> list(@Params MomentIdsDto dto) {
        return momentServiceViewBiz.getMomentList(dto);
    }

    /**
     * 按作者查询动态列表，使用 momentId 倒序游标分页。
     *
     * @param userId 当前查看者ID，来自 Token；本人查看自己的列表时不走缓存
     * @param dto 查询参数，包含 authorId、momentId 游标、limit
     * @return 作者动态分页结果
     */
    @RequestMapping("/author/list")
    public MomentAuthorPageVo authorList(@Token long userId, @Params MomentAuthorQueryDto dto) {
        return momentServiceViewBiz.getMomentPageByAuthor(userId, dto);
    }

    /**
     * 删除动态。
     *
     * @param userId 当前登录用户ID，来自 Token，只允许作者本人删除
     * @param dto 删除参数，必须提供 momentId
     * @return 删除是否成功
     */
    @RequestMapping("/delete")
    public boolean delete(@Token long userId, @Params MomentIdDto dto) {
        return momentServiceViewBiz.deleteMoment(userId, dto);
    }

}
