package com.clmcat.qianyu.social.api.moment.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MomentDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 作品 ID
     */

    protected Long momentId;
    /**
     * 用户 ID
     */
    protected Long authorId;

    /**
     * 作品内容
     */
    protected MomentContent content;

    /**
     * 纬度
     */
    protected double latitude;

    /**
     * 经度
     */
    protected double longitude;

    /**
     * 2 位国家代码
     */
    protected String country;

    /**
     * MomentStatus
     */
    protected Integer status;
    /**
     * 创建的时间
     */
    protected Long createTime;

    /**
     * 点赞数（冗余）
     */
    protected Long likes;

    /**
     * 评论数（冗余）
     */
    protected Long comments;

}
