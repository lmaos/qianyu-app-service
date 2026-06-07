package com.clmcat.qianyu.user.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.user.model.dto.UserIdDto;
import com.clmcat.qianyu.user.model.dto.UserIdsDto;
import com.clmcat.qianyu.user.model.dto.UserInfoUpdateDto;
import com.clmcat.qianyu.user.model.dto.UserNoSearchDto;
import com.clmcat.qianyu.user.model.vo.UserInfoVo;
import com.clmcat.qianyu.user.service.UserViewServiceBiz;
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
 * 用户基础接口。
 * <p>
 * 当前类使用 {@link ApiController} 暴露 HTTP API，统一走 clmcat-webmvc 的响应包装链路。
 * <p>
 * OpenAPI 页面地址（默认本地启动）：
 * http://localhost:8080/swagger-ui/index.html
 * <p>
 * OpenAPI JSON 地址（HTTP API 分组）：
 * http://localhost:8080/v3/api-docs/http-api
 */
@Tag(name = "用户基础接口", description = "用户基础登录态接口，示例中包含 token 自动解析后的用户ID读取。")
@ApiController
@RequestMapping("/api/user")
@LoginVerify
public class UserController {

    @Resource
    UserViewServiceBiz userViewServiceBiz;

    /**
     * 读取当前登录用户ID。
     *
     * @param userId 当前登录用户ID，由 Token 自动解析注入
     * @return user:{userId} 文本
     */
    @Operation(
            summary = "读取当前登录用户ID",
            description = "用于验证 @Token 注入是否生效。参数说明：userId 由登录 token 自动解析，不需要前端手工传参。"
    )
    @GetMapping("/value")
    public String value(@Parameter(hidden = true) @Token long userId) {
        return "user:" + userId;
    }

    /**
     * 查询单个用户基础信息。
     *
     * @param dto 查询参数，targetId 表示要查询的目标用户ID
     * @return 用户信息；查不到返回 null
     */
    @Operation(
            summary = "查询单个用户信息",
            description = "参数说明：userId 由登录 token 自动解析；dto.targetId 为要查询的目标用户ID。查询自己时返回完整资料且不走缓存；查询他人时返回公开资料，并使用 1-2 分钟本地缓存。"
    )
    @GetMapping("/user_info/get")
    public UserInfoVo getUserInfo(@Parameter(hidden = true) @Token long userId,
                                  @ParameterObject @Params UserIdDto dto) {
        return userViewServiceBiz.getUserInfo(userId, dto);
    }

    /**
     * 批量查询多个用户基础信息。
     *
     * @param dto 查询参数，支持 targetIds 数组或逗号分隔字符串
     * @return 用户信息列表
     */
    @Operation(
            summary = "批量查询用户信息",
            description = "参数说明：userId 由登录 token 自动解析；dto.targetIds 适合 JSON 数组；dto.targetIdsText 兼容 query/form 的逗号分隔字符串。查询自己时返回完整资料且不走缓存；查询他人时返回公开资料，并使用 1-2 分钟本地缓存。"
    )
    @GetMapping("/user_info/list")
    public List<UserInfoVo> getUserInfoList(@Parameter(hidden = true) @Token long userId,
                                            @ParameterObject @Params UserIdsDto dto) {
        return userViewServiceBiz.getUserInfoList(userId, dto);
    }

    /**
     * 查询当前登录用户自己的基础信息。
     *
     * @param userId 当前登录用户ID，由 Token 自动解析注入
     * @return 当前登录用户的基础信息
     */
    @Operation(
            summary = "查询当前登录用户信息",
            description = "参数说明：userId 由登录 token 自动解析。当前接口仅用于查询当前登录用户自己的 user_info 信息。"
    )
    @GetMapping("/user_info/self")
    public UserInfoVo getSelfUserInfo(@Parameter(hidden = true) @Token long userId) {
        return userViewServiceBiz.getSelfUserInfo(userId);
    }

    /**
     * 按 userNo 精确搜索用户（添加好友场景）。
     *
     * @param userId 当前登录用户ID，由 Token 自动解析注入
     * @param dto 查询参数，userNo 为用户外显 ID（全局唯一）
     * @return 用户信息；查不到返回 null
     */
    @Operation(
            summary = "按 userNo 搜索用户",
            description = "参数说明：userId 由登录 token 自动解析；dto.userNo 为用户外显ID（用于添加好友等场景的精确搜索）。userNo 全局唯一，未命中返回 null。返回他人资料时使用 1-2 分钟本地缓存。"
    )
    @GetMapping("/user_info/search")
    public UserInfoVo getUserInfoByUserNo(@Parameter(hidden = true) @Token long userId,
                                         @ParameterObject @Params UserNoSearchDto dto) {
        return userViewServiceBiz.getUserInfoByUserNo(userId, dto);
    }

    /**
     * 修改当前登录用户自己的基础信息。
     *
     * @param userId 当前登录用户ID，由 Token 自动解析注入
     * @param dto 聚合修改参数
     * @return 修改后的当前登录用户基础信息
     */
    @Operation(
            summary = "修改当前登录用户信息",
            description = "参数说明：userId 由登录 token 自动解析；dto 为当前登录用户自己的聚合资料修改参数，当前支持 nickname、avatar、bio、gender、birthday、country、province、city。"
    )
    @PostMapping("/user_info/update")
    public UserInfoVo updateSelfUserInfo(@Parameter(hidden = true) @Token long userId,
                                         @Params(description = "当前登录用户个人资料聚合修改参数") UserInfoUpdateDto dto) {
        return userViewServiceBiz.updateSelfUserInfo(userId, dto);
    }


}
