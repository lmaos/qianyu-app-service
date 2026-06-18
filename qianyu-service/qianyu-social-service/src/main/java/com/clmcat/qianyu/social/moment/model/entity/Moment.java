package com.clmcat.qianyu.social.moment.model.entity;

import com.clmcat.qianyu.social.api.moment.model.dto.MomentContent;
import com.clmcat.qianyu.social.moment.typehandler.MomentContentTypeHandler;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 作品表（动态）
 */
@Data
@Table("moment")
public class Moment {

    @Id(keyType = KeyType.None)  // 手动分配雪花ID
    @Column(value = "moment_id", comment = "作品ID（雪花）")
    private Long momentId;

    @Column(value = "author_id", comment = "作者ID")
    private Long authorId;

    @Column(value = "moment_type", comment = "作品类型：text, image, video")
    private String momentType;

    @Column(value = "content", comment = "内容，作品JSON数据 (MomentContent)", typeHandler = MomentContentTypeHandler.class)
    private MomentContent content;      // 存储 JSON 字符串

    @Column(value = "status", comment = "状态：0显示，1隐藏，2删除")
    private Integer status;

    @Column(value = "likes", comment = "点赞数冗余")
    private Long likes;

    @Column(value = "comments", comment = "评论数冗余")
    private Long comments;

    @Column(value = "shares", comment = "转发数冗余")
    private Long shares;

    @Column(value = "latitude", comment = "纬度")
    private double latitude;

    @Column(value = "longitude", comment = "经度")
    private double longitude;

    @Column(value = "country", comment = "国家代码（ISO 3166-1 alpha-2），如 CN, US")
    private String country;

    @Column(value = "create_time", comment = "客户端时间戳（毫秒）")
    private Long createTime;

    @Column(value = "create_time_server", comment = "服务端创建时间（微秒）")
    private LocalDateTime createTimeServer;

    @Column(value = "update_time_server", comment = "服务端更新时间（微秒）")
    private LocalDateTime updateTimeServer;
}