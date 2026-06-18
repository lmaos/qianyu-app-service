package com.clmcat.qianyu.social.api.moment.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class MomentContentText implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 纯文本
     */
    private String text;
    /**
     * @ 谁
     */
    private List<Long> atIds;
}
