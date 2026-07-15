package com.clmcat.qianyu.mall.mch.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 资金类操作日志（映射 {@code t_admin_op_log}）。
 *
 * <p>BG-02：与 backstage 的 {@code com.clmcat.qianyu.mall.backstage.model.entity.AdminOpLog}
 * 映射**同一张表**的「有意镜像」——因 {@code qianyu-mall-service} 无法依赖 backstage 模块
 * （依赖方向为 backstage → mall-api），资金类 op_log 需在 mall-service 侧写入以便与资金原语
 * 共享同一 {@code @Transactional}（决策 2：A 强一致）。列定义见 {@code backstage/sql/admin.sql}，
 * 两实体须保持一致；改动任一处需同步另一处。
 *
 * <p>资金 op_log（approve/reject/markTransferred）由 {@link com.clmcat.qianyu.mall.mch.rpc.MerchantWithdrawalApiImpl}
 * 在其 {@code @Transactional} 方法尾部写入；非资金/状态机类由 backstage 的 {@code OpLogAspect} 走元数据同步落库。
 */
@Data
@Table("t_admin_op_log")
public class FundOpLog {
    @Id(keyType = KeyType.None)
    @Column("id")
    private Long id;
    @Column("account_id") private Long accountId;
    @Column("username") private String username;
    @Column("perm_code") private String permCode;
    @Column("target_entity") private String targetEntity;
    @Column("target_id") private String targetId;
    @Column("before_json") private String beforeJson;
    @Column("after_json") private String afterJson;
    @Column("ip") private String ip;
    @Column("user_agent") private String userAgent;
    @Column("ts") private Long ts;
    @Column("result") private Integer result;
    @Column("cost_ms") private Integer costMs;
    @Column("err_msg") private String errMsg;
    @Column("create_time") private Long createTime;
}
