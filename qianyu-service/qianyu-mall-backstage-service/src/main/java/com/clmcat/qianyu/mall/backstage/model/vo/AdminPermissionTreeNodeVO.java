package com.clmcat.qianyu.mall.backstage.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 权限树节点 VO（/api/admin/permission/tree）。
 * <p>Biz 层查全部 permission（deleted=0），按 parent_id 递归构建；根节点 parent_id=0。
 * type：1菜单 / 2按钮 / 3接口。
 */
@Data
public class AdminPermissionTreeNodeVO {
    /** 权限 ID（雪花）。 */
    private Long id;
    /** 权限码（M0 冻结字典，前后端 1:1）。 */
    private String permCode;
    /** 权限名（前端展示用）。 */
    private String permName;
    /** 类型：1菜单 / 2按钮 / 3接口。 */
    private Integer type;
    /** 父节点 ID；根节点为 0。 */
    private Long parentId;
    /** 子节点（菜单/按钮层级；接口型叶子节点 children 为空）。 */
    private List<AdminPermissionTreeNodeVO> children;
}
