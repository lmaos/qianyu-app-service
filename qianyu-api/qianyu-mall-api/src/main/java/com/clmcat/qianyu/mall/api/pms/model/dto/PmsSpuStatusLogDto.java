package com.clmcat.qianyu.mall.api.pms.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/** SPU 状态变更流水 DTO（admin 只读查询）。 */
@Data
public class PmsSpuStatusLogDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long spuId;
    private Integer fromStatus;
    private Integer toStatus;
    private String event;
    private String source;
    private Long operatorId;
    private String reason;
    private Integer processed;
    private Long processTime;
    private Long createTime;
}
