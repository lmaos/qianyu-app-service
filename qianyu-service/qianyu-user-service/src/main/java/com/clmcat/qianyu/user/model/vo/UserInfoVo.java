package com.clmcat.qianyu.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户基础信息返回对象")
public class UserInfoVo {

    /**
     * 登录的用户编号, 不为空
     */
    @Schema(description = "用户外显编号")
    private String userNo;
    /**
     * 登录时使用的用户ID，某些时候不存在
     */
    @Schema(description = "用户ID")
    private Long userId;
    /**
     * 昵称
     */
    @Schema(description = "昵称")
    private String nickname;
    /**
     * 头像
     */
    @Schema(description = "头像地址")
    private String avatar;
    /**
     * 个人简介
     */
    @Schema(description = "个人简介")
    private String bio;
    /**
     * 性别
     */
    @Schema(description = "性别：0未知，1女性，2男性")
    private Integer gender;
    /**
     * 生日
     */
    @Schema(description = "生日")
    private LocalDate birthday;
    /**
     * 年龄
     */
    @Schema(description = "年龄")
    private Integer age;
    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;
    /**
     * 手机认证时间
     */
    @Schema(description = "手机号认证通过时间戳")
    private Long phoneVerifiedTime;
    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;
    /**
     * 国家
     */
    @Schema(description = "国家/地区代码")
    private String country;
    /**
     * 省份
     */
    @Schema(description = "省份/州")
    private String province;
    /**
     * 城市
     */
    @Schema(description = "城市")
    private String city;
    /**
     * 最近登录时间
     */
    @Schema(description = "最后登录时间戳")
    private Long lastLoginTime;
    /**
     * 状态
     */
    @Schema(description = "账号状态")
    private Integer status;
    /**
     * 冻结结束时间
     */
    @Schema(description = "冻结结束时间戳")
    private Long freezeEndTime;
    /**
     * 创建时间
     */
    @Schema(description = "创建时间戳")
    private Long createTime;
    /**
     * 更新时间
     */
    @Schema(description = "更新时间戳")
    private Long updateTime;
}
