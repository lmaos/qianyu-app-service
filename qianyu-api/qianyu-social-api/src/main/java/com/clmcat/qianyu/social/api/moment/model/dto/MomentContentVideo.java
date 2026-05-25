package com.clmcat.qianyu.social.api.moment.model.dto;

import lombok.Data;

@Data
public class MomentContentVideo {
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
