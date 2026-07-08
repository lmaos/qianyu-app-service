package com.clmcat.qianyu.mall.backstage.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/** 登录日志（t_admin_login_log，无 deleted，审计只增）。result 1成功/0失败。 */
@Data
@Table("t_admin_login_log")
public class AdminLoginLog {
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;
    @Column("account_id") private Long accountId;
    @Column("username") private String username;
    @Column("login_at") private Long loginAt;
    @Column("login_ip") private String loginIp;
    @Column("user_agent") private String userAgent;
    @Column("result") private Integer result;
    @Column("fail_reason") private String failReason;
    @Column("create_time") private Long createTime;
}
