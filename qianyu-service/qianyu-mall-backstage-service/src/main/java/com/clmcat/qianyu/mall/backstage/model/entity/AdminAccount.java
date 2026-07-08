package com.clmcat.qianyu.mall.backstage.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/** 运营账号（t_admin_account）。密码 BCrypt≥12 + 盐；status 1启用/0禁用/2冻结；fail_count 达 5 锁 15min。 */
@Data
@Table("t_admin_account")
public class AdminAccount {
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;
    @Column("username") private String username;
    @Column("pwd_hash") private String pwdHash;
    @Column("pwd_salt") private String pwdSalt;
    @Column("real_name") private String realName;
    @Column("mobile") private String mobile;
    @Column("email") private String email;
    @Column("status") private Integer status;
    @Column("last_login_at") private Long lastLoginAt;
    @Column("last_login_ip") private String lastLoginIp;
    @Column("fail_count") private Integer failCount;
    @Column("create_time") private Long createTime;
    @Column("update_time") private Long updateTime;
    @Column("deleted") private Integer deleted;
}
