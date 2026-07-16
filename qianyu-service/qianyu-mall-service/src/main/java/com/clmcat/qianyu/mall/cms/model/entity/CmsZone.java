package com.clmcat.qianyu.mall.cms.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Data
@Table("cms_zone")
public class CmsZone {

    @Id(keyType = KeyType.None)
    @Column(value = "id")
    private Long id;

    @Column(value = "title")
    private String title;

    @Column(value = "tag_text")
    private String tagText;

    @Column(value = "more_text")
    private String moreText;

    @Column(value = "layout_mode")
    private String layoutMode;

    @Column(value = "product_count")
    private Integer productCount;

    @Column(value = "surface_background")
    private String surfaceBackground;

    @Column(value = "surface_shadow")
    private String surfaceShadow;

    @Column(value = "category_id")
    private Long categoryId;

    /** 填充模式常量 */
    public static final int FILL_MANUAL_ONLY = 0;
    public static final int FILL_AUTO_ONLY = 1;
    public static final int FILL_MIXED = 2;

    @Column(value = "fill_mode", comment = "填充模式: 0=仅手动 1=仅自动 2=手动优先+自动补足")
    private Integer fillMode;

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
