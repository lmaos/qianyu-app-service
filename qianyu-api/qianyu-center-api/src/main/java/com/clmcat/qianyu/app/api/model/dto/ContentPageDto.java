package com.clmcat.qianyu.app.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 内容列表分页结果。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentPageDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 内容列表 */
    @Builder.Default
    private List<ContentTabDto> items = new ArrayList<>();

    /** 下一页游标 */
    private Long nextCursor;

    /** 是否还有更多 */
    private Boolean hasMore;
}
