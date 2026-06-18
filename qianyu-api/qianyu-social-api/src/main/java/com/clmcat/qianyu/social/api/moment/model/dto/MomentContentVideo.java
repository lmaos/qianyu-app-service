package com.clmcat.qianyu.social.api.moment.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MomentContentVideo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 视频ID
     */
    private String videoId;
    /**
     * 视频URL
     */
    private String videoUrl;
    private String coverUrl;

    private Integer width;
    private Integer height;

    private Integer duration;

}
