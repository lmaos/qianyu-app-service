package com.clmcat.qianyu.mall.mch.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("mch_merchant_cert")
public class MerchantCert {

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "merchant_id", comment = "商家ID")
    private Long merchantId;

    @Column(value = "cert_type", comment = "认证类型: 1=个人实名 2=企业认证")
    private Integer certType;

    @Column(value = "cert_name", comment = "认证人/企业名称")
    private String certName;

    @Column(value = "cert_no", comment = "证件号码（加密存储）")
    private String certNo;

    @Column(value = "cert_front_image", comment = "证件正面照片URL")
    private String certFrontImage;

    @Column(value = "cert_back_image", comment = "证件反面照片URL")
    private String certBackImage;

    @Column(value = "cert_holder_image", comment = "手持证件照片URL")
    private String certHolderImage;

    @Column(value = "legal_person", comment = "法人姓名（企业认证）")
    private String legalPerson;

    @Column(value = "legal_person_id", comment = "法人身份证号（加密存储）")
    private String legalPersonId;

    @Column(value = "address", comment = "企业地址（企业认证）")
    private String address;

    @Column(value = "audit_status", comment = "审核状态: 0=待审核 1=已通过 2=已拒绝")
    private Integer auditStatus;

    @Column(value = "audit_remark", comment = "审核备注/拒绝原因")
    private String auditRemark;

    @Column(value = "audit_time", comment = "审核时间（毫秒时间戳）")
    private Long auditTime;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;

    @Column(value = "update_time", comment = "更新时间（毫秒时间戳）")
    private Long updateTime;

    @Column(value = "deleted", comment = "逻辑删除: 0=未删除 1=已删除", isLogicDelete = true)
    private Integer deleted;
}
