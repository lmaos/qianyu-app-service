package com.clmcat.qianyu.mall.api.mch.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 提现审批分页返回 DTO（运营端视角）。
 * <p>字段对齐 {@code MerchantWithdrawal} 实体，富化 merchantName（商家名）/accountBalance（审批时账户余额快照）
 * 与 allowedActions（按当前 status 计算的可执行动作集合）。
 */
@Data
public class WithdrawalPageResultDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String withdrawalNo;
    private Long merchantId;

    /** 商家名（富化自 MerchantApi.getById，便于运营识别）。 */
    private String merchantName;

    private BigDecimal amount;
    private String bankName;
    /** 收款银行账号（脱敏存储）。 */
    private String bankAccount;
    private String accountName;

    /** 提现状态：0待审/1通过/2打款中/3成功/4拒绝/5失败。 */
    private Integer status;

    private String rejectReason;
    private String transferNo;
    private Long transferTime;
    private Long createTime;

    /**
     * 审批时账户余额快照（查询时刻的 balance 字段值）。
     * <p>仅供运营审批参考，非持久化字段（如需持久化快照请走 op_log 审计链路）。
     */
    private BigDecimal accountBalance;

    /**
     * 按当前 status 动态计算的可执行动作集合：
     * <ul>
     *   <li>status=0 待审 → [approve, reject]</li>
     *   <li>status=1 通过 → [markTransferred, reject]</li>
     *   <li>status=2 打款中 → [markTransferred, reject]</li>
     *   <li>其余终态 → []</li>
     * </ul>
     */
    private List<String> allowedActions;
}
