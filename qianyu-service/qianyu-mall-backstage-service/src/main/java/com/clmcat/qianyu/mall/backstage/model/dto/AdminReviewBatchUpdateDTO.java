package com.clmcat.qianyu.mall.backstage.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 运营端批量修改评价状态请求。
 */
@Data
public class AdminReviewBatchUpdateDTO {

    /** 评价 ID 列表（Snowflake），不可为空 */
    private List<Long> ids;

    /** 目标状态：0=隐藏 / 1=正常 / 2=违规 */
    private Integer status;
}
