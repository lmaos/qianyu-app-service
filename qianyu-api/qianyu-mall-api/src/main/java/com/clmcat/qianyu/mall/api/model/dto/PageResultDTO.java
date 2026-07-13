package com.clmcat.qianyu.mall.api.model.dto;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 通用分页结果信封（RPC/Service 共用）。
 * <p>承载「当前页记录 + 总数 + 页码」三要素，便于前端渲染分页 UI。
 * 替代此前「只返 List、丢 total」的契约，避免调用方用 {@code list.size()} 当 total 的反模式。
 *
 * @param <T> 记录类型
 */
@Data
@Builder
public class PageResultDTO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 当前页记录列表（不为 null，无数据为空列表）。 */
    private List<T> records;

    /** 总记录数（跨所有页）。 */
    private long total;

    /** 当前页码（从 1 起）。 */
    private long pageNum;

    /** 每页条数。 */
    private long pageSize;
}
