package com.clmcat.qianyu.mall.backstage.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/** GET /api/admin/account/info 返回（前端动态路由权限硬前置：permCodes 驱动按钮显隐）。 */
@Data
@Builder
public class AdminAccountInfoVO {
    private Long adminId;
    private String username;
    private String realName;
    private List<String> permCodes;
}
