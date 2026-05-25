package com.clmcat.qianyu.social.moment.model.dto;

import lombok.Data;

@Data
public class MomentAuthorQueryDto {
    /**
     * 作者编号
     */
    private Long authorId;
    /**
     * 游标 momentId，查询比它更早的数据。
     */
    private Long momentId;
    /**
     * 分页条数
     */
    private Integer limit;
}
