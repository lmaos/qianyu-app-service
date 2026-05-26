package com.clmcat.qianyu.social.comment.model.dto;

import com.clmcat.framework.webmvc.anns.Params;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "评论ID批量查询参数")
public class CommentIdsDto {
    /**
     * 评论ID列表，适合 JSON 请求体。
     */
    @Schema(description = "评论ID列表，适合 JSON 数组传参")
    private List<Long> commentIds;

    /**
     * 兼容 query/form 的逗号分隔参数。
     */
    @Params(name = "commentIds", required = false)
    @Schema(description = "兼容 query/form 的逗号分隔评论ID字符串")
    private String commentIdsText;
}
