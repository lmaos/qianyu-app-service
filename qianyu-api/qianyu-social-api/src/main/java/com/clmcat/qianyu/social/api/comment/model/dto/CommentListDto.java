package com.clmcat.qianyu.social.api.comment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CommentListDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final CommentListDto EMPTY = CommentListDto.builder().comments(new ArrayList<>()).build();
    private List<CommentDto> comments;
}
