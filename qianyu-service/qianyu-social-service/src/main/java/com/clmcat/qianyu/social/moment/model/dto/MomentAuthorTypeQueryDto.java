package com.clmcat.qianyu.social.moment.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "按作者+类型查询动态分页参数")
public class MomentAuthorTypeQueryDto extends MomentAuthorQueryDto {
    /**
     * 作品类型：text / image / video
     */
    @Schema(description = "作品类型：text、image、video", requiredMode = Schema.RequiredMode.REQUIRED)
    private String momentType;
}
