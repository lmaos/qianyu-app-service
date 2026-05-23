package com.clmcat.qianyu.user.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户多方式授权登录表
 *
 * @author author
 * @date 2025-01-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("user_auth")
public class UserAuth {

    /**
     * 物理主键(仅数据库行标识，分表可重复)
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 核心业务用户ID，关联用户主表
     */
    private Long userId;

    /**
     * 授权类型：phone/email/username/wechat/qq等
     */
    private String identityType;

    /**
     * 授权标识：手机号/邮箱/用户名/三方openid
     */
    private String identifier;

    /**
     * 凭证(密码/令牌，三方登录可为空)
     */
    private String credential;

    /**
     * 创建时间戳
     */
    private Long createTime;

    /**
     * 更新时间戳
     */
    private Long updateTime;
}