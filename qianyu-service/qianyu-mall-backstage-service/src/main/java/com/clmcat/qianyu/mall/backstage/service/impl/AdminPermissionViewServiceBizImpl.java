package com.clmcat.qianyu.mall.backstage.service.impl;

import com.clmcat.qianyu.mall.backstage.mapper.AdminPermissionMapper;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminPermission;
import com.clmcat.qianyu.mall.backstage.model.vo.AdminPermissionTreeNodeVO;
import com.clmcat.qianyu.mall.backstage.service.AdminPermissionViewServiceBiz;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 运营权限查询服务实现（权限树构建）。
 *
 * <p>tree 实现：查全部 permission（deleted=0），按 parent_id 分组，从根（parent_id=0）递归构建。
 * 时间复杂度 O(n)：一次全量查询 + Map 分组，避免递归中逐节点查库。
 */
@Slf4j
@Service
public class AdminPermissionViewServiceBizImpl implements AdminPermissionViewServiceBiz {

    /** 根节点 parent_id（M0 约定）。 */
    private static final long ROOT_PARENT_ID = 0L;

    @Resource private AdminPermissionMapper permissionMapper;

    @Override
    public List<AdminPermissionTreeNodeVO> tree() {
        List<AdminPermission> all = permissionMapper.selectListByQuery(
                QueryWrapper.create().where("deleted = ?", 0).orderBy("id ASC"));
        if (all == null || all.isEmpty()) return Collections.emptyList();

        // 一次分组：parent_id → 子节点列表
        Map<Long, List<AdminPermission>> byParent = all.stream()
                .collect(Collectors.groupingBy(p -> p.getParentId() == null ? ROOT_PARENT_ID : p.getParentId()));

        return buildChildren(ROOT_PARENT_ID, byParent);
    }

    /**
     * 递归构建子树。
     * @param parentId 当前父节点 ID（根为 0）
     * @param byParent 全量按 parent_id 分组的索引
     */
    private List<AdminPermissionTreeNodeVO> buildChildren(Long parentId,
                                                          Map<Long, List<AdminPermission>> byParent) {
        List<AdminPermission> children = byParent.get(parentId);
        if (children == null || children.isEmpty()) return Collections.emptyList();
        List<AdminPermissionTreeNodeVO> result = new ArrayList<>(children.size());
        for (AdminPermission p : children) {
            AdminPermissionTreeNodeVO node = new AdminPermissionTreeNodeVO();
            node.setId(p.getId());
            node.setPermCode(p.getPermCode());
            node.setPermName(p.getPermName());
            node.setType(p.getType());
            node.setParentId(parentId);
            node.setChildren(buildChildren(p.getId(), byParent));
            result.add(node);
        }
        return result;
    }
}
