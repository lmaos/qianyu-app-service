package com.clmcat.qianyu.user.api.model.dto;

import com.clmcat.qianyu.user.api.UserApi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RpcUserInfoDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 核心用户ID，业务主键。由分布式ID生成器生成，不自增。关联所有其他表（user_auth、user_identity等）
     */
    private Long userId;

    /**
     * 用户外显ID，用于搜索、分享、展示。例如："U8a2F9c"。全局唯一，可让用户自定义或系统生成，建议6~20位字母数字组合
     */
    private String userNo;

    /**
     * 用户昵称，可自定义，支持表情符号。最长64个字符，不唯一
     */
    private String nickname;

    /**
     * 用户头像URL。推荐使用CDN地址，支持https。可存OSS临时授权URL
     */
    private String avatar;

    /**
     * 个人简介/签名，最长1000字符。支持文字、表情、链接等
     */
    private String bio;

    /**
     * 性别：0=未知，1=女性，2=男性。由用户自行选择，非强制
     */
    private Integer gender;

    /**
     * 生日，格式 yyyy-MM-dd（如 1990-01-01）。用于年龄计算或星座展示，非必填
     */
    private LocalDate birthday;

    /**
     * 年龄（冗余字段，可由 birthday 实时计算更新）。便于按年龄筛选用户，每天/每周定时任务更新
     */
    private Integer age;

    /**
     * 手机号（带国家码）。例如：+8613800000000。存储前需 AES-256 加密。该字段应加唯一索引（SQL 中未体现，建议 ALTER TABLE 添加），确保手机号与账号一一对应，符合实名制要求
     */
    private String phone;

    /**
     * 手机号认证通过时间戳（Unix 秒级）。0=未验证；>0 表示已通过短信或微信授权完成验证，是社交合规的最低要求
     */
    private Long phoneVerifiedTime;

    /**
     * 邮箱地址。用于登录、找回密码、接收通知。建议加唯一索引，存储前可加密（可选）
     */
    private String email;

    /**
     * 国家/地区，ISO 3166-1 alpha-2 两位代码，如 CN、US、DE。由客户端传递（GPS/用户选择），视为可修改的个人资料标签
     */
    private String country;

    /**
     * 省份/州。字符串形式，例如：北京市、California。自由文本，用于展示位置
     */
    private String province;

    /**
     * 城市。字符串形式，例如：朝阳区、Los Angeles。自由文本
     */
    private String city;

    /**
     * 最后登录时间戳（Unix 秒级）。每次用户登录成功后更新，用于活跃度统计
     */
    private Long lastLoginTime;

    /**
     * 账号状态总开关：0=正常，1=冻结封禁，2=注销。冻结时配合 freeze_end_time 使用；注销后账号不可恢复，所有关联数据应匿名化或删除
     */
    private Integer status;

    /**
     * 冻结到期时间戳（Unix 秒级）。0=未冻结或永久冻结；>0 表示临时冻结到此时间点自动解冻。仅 status=1 时有效
     */
    private Long freezeEndTime;

    /**
     * 账号创建时间戳（Unix 秒级）。用户首次注册成功时记录
     */
    private Long createTime;

    /**
     * 账号信息最后更新时间戳（Unix 秒级）。每次修改 user_info 任何字段时更新
     */
    private Long updateTime;
}
