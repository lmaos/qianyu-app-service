package com.clmcat.qianyu.social.moment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "作者动态分页查询参数")
public class MomentAuthorQueryDto {
    /**
     * 作者编号
     */
    @Schema(description = "作者ID")
    private Long authorId;
    /**
     * 游标 momentId，查询比它更早的数据。
     */
    @Schema(description = "倒序分页游标 momentId，查询比它更早的数据")
    private Long momentId;
    /**
     * 分页条数
     */
    @Schema(description = "分页大小")
    private Integer limit;
}
