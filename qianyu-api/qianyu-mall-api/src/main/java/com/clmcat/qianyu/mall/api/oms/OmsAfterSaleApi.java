package com.clmcat.qianyu.mall.api.oms;

import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.api.oms.model.dto.AftersalePageQueryDTO;
import com.clmcat.qianyu.mall.api.oms.model.dto.OmsAfterSaleDto;

public interface OmsAfterSaleApi {

    void insert(OmsAfterSaleDto afterSale);

    OmsAfterSaleDto findById(Long aftersaleId);

    void update(OmsAfterSaleDto afterSale);

    /**
     * 售后状态 CAS 推进（从 impl 提升为契约方法）。
     * <p>WHERE {@code id} AND {@code status = fromStatus}，防并发双推进（{@code OmsAfterSale} 无 version 字段，用 status 作乐观条件）。
     * <p>架构红线：backstage → mall 一律走 {@code @DubboReference}，禁止进程内直调 impl。
     *
     * @param id            售后单 ID
     * @param fromStatus    期望的源状态（CAS 条件）
     * @param toStatus      目标状态
     * @param rejectReason  驳回原因（仅 toStatus=30 等拒绝态有意义，可传 null）
     * @return true 表示 CAS 成功（affected > 0）；false 表示单据已被并发改动
     */
    boolean updateStatusCAS(Long id, int fromStatus, int toStatus, String rejectReason);

    /**
     * 平台跨店售后分页（运营端视角）。
     * <p>支持按 merchantId / status / type 过滤，按 create_time DESC 排序。
     *
     * @param query 分页查询条件（pageNum/pageSize 默认 1/10）
     * @return 分页结果（含当前页 records + total/页码），records 空列表而非 null
     */
    PageResultDTO<OmsAfterSaleDto> pageByPlatform(AftersalePageQueryDTO query);
}
