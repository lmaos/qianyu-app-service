package com.clmcat.qianyu.mall.backstage.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 操作日志（t_admin_op_log，无 deleted，审计只增）。
 * <p>op_log @Aspect 三分类（02-tech-design）：
 * 资金类（freezeForApply/settleForApprove/refundForReject/markTransferred/approve/reject）+ 资质审核类（auditMerchant）
 * → 强制同步落库同 @Transactional（决策 2：A 强一致），findById×2 全量 before/after 快照；
 * 纯状态机类 → @Async 异步，仅落 after + 元数据。
 * <p>@Aspect 实现 P1b（提现审批时实际触达资金类 op_log）。
 */
@Data
@Table("t_admin_op_log")
public class AdminOpLog {
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
