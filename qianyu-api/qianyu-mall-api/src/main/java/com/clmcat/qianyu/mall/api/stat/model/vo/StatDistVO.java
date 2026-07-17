package com.clmcat.qianyu.mall.api.stat.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class StatDistVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String key;
    private String label;
    private long count;
    private BigDecimal amount;
}
