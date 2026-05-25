package com.clmcat.qianyu.social.moment.model.dto;

import com.clmcat.framework.webmvc.anns.Params;
import lombok.Data;

import java.util.List;

@Data
public class MomentIdsDto {
    /**
     * 动态编号集合，适合 JSON 请求体。
     */
    private List<Long> momentIds;
    /**
     * 兼容 query/form 的逗号分隔参数，如：1,2,3
     */
    @Params(name = "momentIds", required = false)
    private String momentIdsText;
}
