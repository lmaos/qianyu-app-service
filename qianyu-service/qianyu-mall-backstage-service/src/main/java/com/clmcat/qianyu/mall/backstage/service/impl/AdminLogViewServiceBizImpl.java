package com.clmcat.qianyu.mall.backstage.service.impl;

import com.clmcat.qianyu.mall.backstage.mapper.AdminLoginLogMapper;
import com.clmcat.qianyu.mall.backstage.mapper.AdminOpLogMapper;
import com.clmcat.qianyu.mall.backstage.model.dto.AdminLogQueryDTO;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminLoginLog;
import com.clmcat.qianyu.mall.backstage.model.entity.AdminOpLog;
import com.clmcat.qianyu.mall.backstage.service.AdminLogViewServiceBiz;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 运营日志查询服务实现（操作日志 + 登录日志，审计只增）。
 *
 * <p>实现要点：
 * <ul>
 *   <li>QueryWrapper 占位符参数化（禁拼接）；</li>
 *   <li>oplog 按 admin_id / perm_code / 时间范围 过滤，按 ts DESC；</li>
 *   <li>loginlog 按 admin_id / 时间范围 过滤（无 perm_code 维度），按 login_at DESC。</li>
 * </ul>
 */
@Slf4j
@Service
public class AdminLogViewServiceBizImpl implements AdminLogViewServiceBiz {

    @Resource private AdminOpLogMapper opLogMapper;
    @Resource private AdminLoginLogMapper loginLogMapper;

    @Override
    public Page<AdminOpLog> pageOpLog(AdminLogQueryDTO dto) {
        int pageNum = dto == null || dto.getPageNum() == null || dto.getPageNum() <= 0 ? 1 : dto.getPageNum();
        int pageSize = dto == null || dto.getPageSize() == null || dto.getPageSize() <= 0 ? 10 : dto.getPageSize();

        QueryWrapper qw = QueryWrapper.create();
        if (dto != null) {
            if (dto.getAdminId() != null) {
                qw.and("account_id = ?", dto.getAdminId());
            }
            if (dto.getPermCode() != null && !dto.getPermCode().isEmpty()) {
                qw.and("perm_code = ?", dto.getPermCode());
            }
            if (dto.getStartTime() != null) {
                qw.and("ts >= ?", dto.getStartTime());
            }
            if (dto.getEndTime() != null) {
                qw.and("ts <= ?", dto.getEndTime());
            }
        }
        qw.orderBy("ts DESC");

        Page<AdminOpLog> page = opLogMapper.paginate(Page.of(pageNum, pageSize), qw);
        return page == null ? emptyPage(pageNum, pageSize) : page;
    }

    @Override
    public Page<AdminLoginLog> pageLoginLog(AdminLogQueryDTO dto) {
        int pageNum = dto == null || dto.getPageNum() == null || dto.getPageNum() <= 0 ? 1 : dto.getPageNum();
        int pageSize = dto == null || dto.getPageSize() == null || dto.getPageSize() <= 0 ? 10 : dto.getPageSize();

        QueryWrapper qw = QueryWrapper.create();
        if (dto != null) {
            if (dto.getAdminId() != null) {
                qw.and("account_id = ?", dto.getAdminId());
            }
            // loginlog 无 perm_code 字段，dto.permCode 忽略
            if (dto.getStartTime() != null) {
                qw.and("login_at >= ?", dto.getStartTime());
            }
            if (dto.getEndTime() != null) {
                qw.and("login_at <= ?", dto.getEndTime());
            }
        }
        qw.orderBy("login_at DESC");

        Page<AdminLoginLog> page = loginLogMapper.paginate(Page.of(pageNum, pageSize), qw);
        return page == null ? emptyPage(pageNum, pageSize) : page;
    }

    private static <T> Page<T> emptyPage(int pageNum, int pageSize) {
        Page<T> p = new Page<>(pageNum, pageSize);
        p.setRecords(Collections.emptyList());
        p.setTotalRow(0);
        return p;
    }
}
