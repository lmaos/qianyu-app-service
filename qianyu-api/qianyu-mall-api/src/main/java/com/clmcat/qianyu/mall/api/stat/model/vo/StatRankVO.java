package com.clmcat.qianyu.mall.api.stat.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StatRankVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String image;
    private long sales;
    private BigDecimal amount;
}
