package com.clmcat.qianyu.social.comment.model.dto;

import com.clmcat.framework.webmvc.anns.Params;
import lombok.Data;

import java.util.List;

@Data
public class CommentIdsDto {
    /**
     * 评论ID列表，适合 JSON 请求体。
     */
    private List<Long> commentIds;

    /**
     * 兼容 query/form 的逗号分隔参数。
     */
    @Params(name = "commentIds", required = false)
    private String commentIdsText;
}
