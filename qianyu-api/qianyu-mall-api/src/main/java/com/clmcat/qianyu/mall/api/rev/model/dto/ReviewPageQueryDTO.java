package com.clmcat.qianyu.mall.api.rev.model.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 运营端评价分页查询条件（跨店）。
 * <p>所有字段均可空：空表示不过滤。pageNum/pageSize 缺省由实现兜底（1/10）。
 */
@Data
public class ReviewPageQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品 SPU ID（精确匹配，空=全部 SPU） */
    private Long spuId;

    /** 商家 ID（精确匹配，空=全平台） */
    private Long merchantId;

    /** 评价状态：0=隐藏 / 1=正常 / 2=违规（空=全部，便于运营审计） */
    private Integer status;

    /** 评分：1~5 分（空=全部） */
    private Integer score;

    /** 页码，缺省 1 */
    private Integer pageNum;

    /** 每页条数，缺省 10 */
    private Integer pageSize;
}
