package com.clmcat.qianyu.social.moment.model.vo;

import com.clmcat.qianyu.social.api.moment.model.dto.MomentContent;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MomentVo {
    private Long momentId;
    private Long authorId;
    private MomentContent content;
    private double latitude;
    private double longitude;
    private String country;
    private Long likes;
    private Long comments;
    private boolean hasLike;
    private Integer status;
    private Long createTime;

    /** 作者昵称 */
    private String nickname;

    /** 作者头像 URL */
    private String avatar;
}
