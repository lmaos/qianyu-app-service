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
     * 买家填退货物流 + CAS 推进（type=2 退货退款：20 商家同意 → 40 用户已发货）。
     * <p>WHERE {@code id} AND {@code status = fromStatus}，同时写 returnShippingNo/Company，防并发。
     *
     * @return true 表示 CAS 成功；false 表示单据已被并发改动
     */
    boolean updateReturnShippingCAS(Long id, int fromStatus, int toStatus, String shippingNo, String shippingCompany);

    /**
     * 商家填寄回物流 + CAS 推进（type=3 换货 / type=4 维修：55 商家已收货 → 70 商家已寄出）。
     * <p>WHERE {@code id} AND {@code status = fromStatus}，同时写 sendBackShippingNo/Company，防并发。
     *
     * @return true 表示 CAS 成功；false 表示单据已被并发改动
     */
    boolean updateSendBackShippingCAS(Long id, int fromStatus, int toStatus, String shippingNo, String shippingCompany);

    /**
     * 平台跨店售后分页（运营端视角）。
     * <p>支持按 merchantId / status / type 过滤，按 create_time DESC 排序。
     *
     * @param query 分页查询条件（pageNum/pageSize 默认 1/10）
     * @return 分页结果（含当前页 records + total/页码），records 空列表而非 null
     */
    PageResultDTO<OmsAfterSaleDto> pageByPlatform(AftersalePageQueryDTO query);
}
