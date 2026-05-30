package com.clmcat.qianyu.mall.cms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("cms_banner")
public class CmsBanner {

    @Id(keyType = KeyType.None)
    @Column(value = "id")
    private Long id;

    @Column(value = "title")
    private String title;

    @Column(value = "description")
    private String description;

    @Column(value = "action_text")
    private String actionText;

    @Column(value = "tag_text")
    private String tagText;

    @Column(value = "image")
    private String image;

    @Column(value = "link_url")
    private String linkUrl;

    @Column(value = "link_type")
    private Integer linkType;

    @Column(value = "link_value")
    private String linkValue;

    @Column(value = "sort")
    private Integer sort;

    @Column(value = "status")
    private Integer status;

    @Column(value = "create_time")
    private Long createTime;

    @Column(value = "update_time")
    private Long updateTime;

    @Column(value = "deleted", isLogicDelete = true)
    private Integer deleted;
}
