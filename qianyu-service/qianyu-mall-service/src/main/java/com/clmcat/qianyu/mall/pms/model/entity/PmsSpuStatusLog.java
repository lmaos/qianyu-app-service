package com.clmcat.qianyu.mall.pms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * SPU 状态变更流水。由 {@code PmsSpuStatusChanger}（SPU 状态变更唯一入口）写入，
 * 供楼层自动投放任务消费（扫 processed=0 的上架/下架事件）。
 *
 * <p>event 取值：LIST_ON / LIST_OFF / SUBMIT_AUDIT / AUDIT_PASS / AUDIT_REGRESS / EDIT_REGRESS / CREATE。
 * source 取值：MERCHANT / ADMIN / SYSTEM。
 */
@Data
@Table("pms_spu_status_log")
public class PmsSpuStatusLog {

    public static final int PROCESSED_NO = 0;
    public static final int PROCESSED_YES = 1;

    @Id(keyType = KeyType.None)
    @Column(value = "id", comment = "主键（Snowflake ID）")
    private Long id;

    @Column(value = "spu_id", comment = "商品 SPU ID")
    private Long spuId;

    @Column(value = "from_status", comment = "变更前状态")
    private Integer fromStatus;

    @Column(value = "to_status", comment = "变更后状态")
    private Integer toStatus;

    @Column(value = "event", comment = "事件码")
    private String event;

    @Column(value = "source", comment = "来源: MERCHANT/ADMIN/SYSTEM")
    private String source;

    @Column(value = "operator_id", comment = "操作者（userId 或 adminId）")
    private Long operatorId;

    @Column(value = "reason", comment = "原因（如下架原因）")
    private String reason;

    @Column(value = "processed", comment = "0=未消费 1=已消费（投放任务）")
    private Integer processed;

    @Column(value = "process_time", comment = "消费时间（毫秒时间戳）")
    private Long processTime;

    @Column(value = "create_time", comment = "创建时间（毫秒时间戳）")
    private Long createTime;
}
