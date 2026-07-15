package com.clmcat.qianyu.mall.mch.mapper;

import com.clmcat.qianyu.mall.mch.model.entity.FundOpLog;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资金类操作日志 Mapper（t_admin_op_log）。BG-02：由 {@code MerchantWithdrawalApiImpl} 在资金
 * {@code @Transactional} 内写入，审计 insert 失败随主事务回滚（决策 2：A 强一致）。
 */
@Mapper
public interface FundOpLogMapper extends BaseMapper<FundOpLog> {
}
