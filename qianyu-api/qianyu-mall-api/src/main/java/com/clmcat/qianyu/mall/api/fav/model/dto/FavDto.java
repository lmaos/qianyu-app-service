package com.clmcat.qianyu.mall.api.fav.model.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class FavDto implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long targetId;
    private Integer targetType;
    private Long createTime;
}
