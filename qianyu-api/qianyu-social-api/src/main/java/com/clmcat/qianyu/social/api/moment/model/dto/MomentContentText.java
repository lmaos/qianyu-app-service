package com.clmcat.qianyu.social.api.moment.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class MomentContentText {
    /**
     * 纯文本
     */
    private String text;
    /**
     * @ 谁
     */
    private List<Long> atIds;
}
