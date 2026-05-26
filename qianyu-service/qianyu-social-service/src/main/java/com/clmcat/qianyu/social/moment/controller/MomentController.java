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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 动态作品接口。
 * <p>
 * 当前类使用 {@link ApiController} 暴露动态发布、详情、批量查询、作者分页列表和删除能力。
 * <p>
 * OpenAPI 页面地址（默认本地启动）：
 * http://localhost:8080/swagger-ui/index.html
 * <p>
 * OpenAPI JSON 地址（HTTP API 分组）：
 * http://localhost:8080/v3/api-docs/http-api
 */
@Tag(name = "动态作品接口", description = "提供动态发布、详情查询、批量查询、作者分页查询和删除能力。")
@ApiController
@RequestMapping("/api/social/moment")
@LoginVerify
public class MomentController {
    @Resource
    private MomentServiceViewBiz momentServiceViewBiz;

    /**
     * 发布动态。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto 发布参数，包含内容、定位、国家、状态等
     * @return 发布后的动态 VO
     */
    @Operation(summary = "发布动态", description = "参数说明：userId 为当前登录用户ID；dto 包含 content、latitude、longitude、country、status。")
    @PostMapping("/publish")
    public MomentVo publish(@Parameter(hidden = true) @Token long userId, @Params(description = "动态发布参数") MomentPublishDto dto) {
        return momentServiceViewBiz.publish(userId, dto);
    }

    /**
     * 查询单条动态详情。
     *
     * @param userId 当前查看者ID，来自 Token；用于区分本人查看与他人查看的缓存策略
     * @param dto 查询参数，必须提供 momentId
     * @return 动态详情 VO
     */
    @Operation(summary = "查询动态详情", description = "参数说明：userId 为当前查看者ID，用于缓存策略；dto.momentId 为要查询的动态ID。")
    @GetMapping("/get")
    public MomentVo get(@Parameter(hidden = true) @Token long userId, @ParameterObject @Params MomentIdDto dto) {
        return momentServiceViewBiz.getMoment(userId, dto);
    }

    /**
     * 批量查询动态。
     *
     * @param dto 查询参数，可通过 JSON 的 momentIds 或逗号分隔字符串传入多个 momentId
     * @return 动态列表
     */
    @Operation(summary = "批量查询动态", description = "参数说明：dto.momentIds 适合 JSON 数组传参；dto.momentIdsText 兼容 query/form 的逗号分隔字符串。")
    @GetMapping("/list")
    public List<MomentVo> list(@ParameterObject @Params MomentIdsDto dto) {
        return momentServiceViewBiz.getMomentList(dto);
    }

    /**
     * 按作者查询动态列表，使用 momentId 倒序游标分页。
     *
     * @param userId 当前查看者ID，来自 Token；本人查看自己的列表时不走缓存
     * @param dto 查询参数，包含 authorId、momentId 游标、limit
     * @return 作者动态分页结果
     */
    @Operation(summary = "按作者分页查询动态", description = "参数说明：userId 为当前查看者ID；dto.authorId 为作者ID；dto.momentId 为倒序游标；dto.limit 为分页大小。")
    @GetMapping("/author/list")
    public MomentAuthorPageVo authorList(@Parameter(hidden = true) @Token long userId, @ParameterObject @Params MomentAuthorQueryDto dto) {
        return momentServiceViewBiz.getMomentPageByAuthor(userId, dto);
    }

    /**
     * 删除动态。
     *
     * @param userId 当前登录用户ID，来自 Token，只允许作者本人删除
     * @param dto 删除参数，必须提供 momentId
     * @return 删除是否成功
     */
    @Operation(summary = "删除动态", description = "参数说明：userId 为当前登录用户ID，仅作者本人可删除；dto.momentId 为待删除动态ID。")
    @PostMapping("/delete")
    public boolean delete(@Parameter(hidden = true) @Token long userId, @Params(description = "动态删除参数") MomentIdDto dto) {
        return momentServiceViewBiz.deleteMoment(userId, dto);
    }

}
