package com.clmcat.qianyu.social.api.moment.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MomentContent implements Serializable {
    @Serial
    private final static long serialVersionUID = 1L;
    /**
     * 作品类型， image, video, text
     */
    private String type;
    private MomentContentText text;
    private MomentContentImageList image;
    private MomentContentVideo video;
}
