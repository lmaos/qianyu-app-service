package com.clmcat.qianyu.mall.api.rev;

import com.clmcat.qianyu.mall.api.rev.model.dto.RevReviewDto;
import com.clmcat.qianyu.mall.api.rev.model.dto.ReviewPageQueryDTO;

import java.util.List;

public interface RevReviewApi {

    /**
     * RPC: 查询评价
     */
    RevReviewDto getById(Long reviewId);

    /**
     * RPC: 运营端跨店分页查询评价。
     * <p>按 spuId/merchantId/status/score 过滤，按 create_time DESC 排序，
     * 返回当前页 RevReviewDto 列表（含所有状态：0=隐藏/1=正常/2=违规，便于运营审计）。
     *
     * @param query 分页与过滤条件（pageNum/pageSize 缺省 1/10）
     * @return 当前页评价 DTO 列表；无数据返回空列表
     */
    List<RevReviewDto> pageByPlatform(ReviewPageQueryDTO query);

    /**
     * RPC: 批量修改评价状态。
     * <p>运营管控场景：将一批评价统一置为 0=隐藏 / 1=正常 / 2=违规。
     * 实现按 id 逐条 update（避免动态 IN 拼接，保持与逻辑删除 deleted=0 兼容），
     * 整体在事务内完成。
     *
     * @param ids    评价 ID 列表（Snowflake），不可为空
     * @param status 目标状态：0=隐藏 / 1=正常 / 2=违规
     */
    void batchUpdateStatus(List<Long> ids, int status);
}
