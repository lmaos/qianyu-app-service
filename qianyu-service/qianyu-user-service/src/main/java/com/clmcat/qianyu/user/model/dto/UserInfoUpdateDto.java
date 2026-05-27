package com.clmcat.qianyu.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "当前登录用户个人资料聚合修改参数")
public class UserInfoUpdateDto {
    /**
     * 用户昵称
     */
    @Schema(description = "昵称，最长64个字符")
    private String nickname;
    /**
     * 用户头像
     */
    @Schema(description = "头像地址")
    private String avatar;
    /**
     * 个人简介
     */
    @Schema(description = "个人简介，最长1000个字符")
    private String bio;
    /**
     * 性别
     */
    @Schema(description = "性别：0未知，1女性，2男性")
    private Integer gender;
    /**
     * 生日
     */
    @Schema(description = "生日，格式 yyyy-MM-dd")
    private LocalDate birthday;
    /**
     * 国家/地区代码
     */
    @Schema(description = "国家/地区代码，例如 CN、US")
    private String country;
    /**
     * 省份/州
     */
    @Schema(description = "省份/州")
    private String province;
    /**
     * 城市
     */
    @Schema(description = "城市")
    private String city;
}
