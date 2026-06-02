package com.clmcat.qianyu.mall.rev.service;

import com.clmcat.qianyu.mall.rev.model.vo.EvaluatePageVO;

/**
 * 评价详情页聚合查询接口
 * 一次返回商品信息 + 评价统计 + 评价分页列表
 */
public interface EvaluatePageInterface {

    /**
     * 查询评价详情页数据
     *
     * @param spuId     商品 SPU ID（必填）
     * @param score     评价筛选: 0=全部 / 1=差评 / 2=中评 / 3=好评 / 4=有图
     * @param sortField 排序字段: createTime / score
     * @param pageNum   页码
     * @param pageSize  每页条数
     * @return 评价详情页聚合数据
     */
    EvaluatePageVO query(Long spuId, Integer score, String sortField, int pageNum, int pageSize);
}
