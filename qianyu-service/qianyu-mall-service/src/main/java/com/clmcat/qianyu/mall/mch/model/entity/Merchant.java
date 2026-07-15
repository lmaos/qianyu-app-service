package com.clmcat.qianyu.mall.mch.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("mch_merchant")
public class Merchant {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "user_id", comment = "关联用户ID")
    private Long userId;

    @Column(value = "name", comment = "商家名称")
    private String name;

    @Column(value = "type", comment = "商家类型: 1=个人 2=企业")
    private Integer type;

    @Column(value = "contact_name", comment = "联系人姓名")
    private String contactName;

    @Column(value = "contact_phone", comment = "联系电话")
    private String contactPhone;

    @Column(value = "license_no", comment = "营业执照编号（企业商家必填）")
    private String licenseNo;

    @Column(value = "license_image", comment = "营业执照图片URL")
    private String licenseImage;

    @Column(value = "description", comment = "商家简介")
    private String description;

    @Column(value = "bank_name", comment = "结算银行名称")
    private String bankName;

    @Column(value = "bank_account", comment = "结算银行账号（加密存储）")
    private String bankAccount;

    @Column(value = "bank_holder", comment = "开户人姓名（加密存储）")
    private String bankHolder;

    @Column(value = "bank_branch", comment = "开户支行")
    private String bankBranch;

    @Column(value = "legal_person_name", comment = "法人/经营者姓名")
    private String legalPersonName;

    @Column(value = "legal_person_id_card", comment = "法人/经营者身份证号")
    private String legalPersonIdCard;

    @Column(value = "contact_email", comment = "联系人邮箱")
    private String contactEmail;

    @Column(value = "settlement_cycle", comment = "结算周期: 1=T+1 2=T+7 3=T+15")
    private Integer settlementCycle;

    @Column(value = "audit_status", comment = "审核状态: 0=待审核 1=已通过 2=已拒绝")
    private Integer auditStatus;

    @Column(value = "audit_remark", comment = "审核备注/拒绝原因")
    private String auditRemark;

    @Column(value = "status", comment = "商家状态: 0=禁用 1=正常 2=冻结")
    private Integer status;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=正常 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
