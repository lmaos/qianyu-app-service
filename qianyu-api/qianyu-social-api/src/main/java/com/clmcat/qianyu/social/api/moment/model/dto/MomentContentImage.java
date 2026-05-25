package com.clmcat.qianyu.social.api.moment.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MomentContentImage implements Serializable {
    @Serial
    private final static long serialVersionUID = 1L;
    /**
     * 图片ID
     */
    private String imageId;
    /**
     * 图片地址
     */
    private String imageUrl;
    /**
     * 图片宽 px
     */
    private Integer width;
    /**
     * 图片高 px
     */
    private Integer height;



    public int width() {
        return width == null ? 0 : width;
    }

    public int height() {
        return height == null ? 0 : height;
    }

}
