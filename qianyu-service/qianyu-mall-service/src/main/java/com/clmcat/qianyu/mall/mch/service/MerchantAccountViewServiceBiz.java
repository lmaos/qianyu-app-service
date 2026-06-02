package com.clmcat.qianyu.mall.mch.service;

import com.clmcat.qianyu.mall.mch.model.dto.BillQueryDTO;
import com.clmcat.qianyu.mall.mch.model.dto.SettlementQueryDTO;
import com.clmcat.qianyu.mall.mch.model.dto.WithdrawApplyDTO;
import com.clmcat.qianyu.mall.mch.model.dto.WithdrawQueryDTO;
import com.clmcat.qianyu.mall.mch.model.vo.AccountInfoVO;
import com.clmcat.qianyu.mall.mch.model.vo.BillItemVO;
import com.clmcat.qianyu.mall.mch.model.vo.SettlementItemVO;
import com.clmcat.qianyu.mall.mch.model.vo.WithdrawItemVO;
import com.mybatisflex.core.paginate.Page;

public interface MerchantAccountViewServiceBiz {

    AccountInfoVO getAccountInfo(long userId);

    Page<BillItemVO> getBillList(long userId, BillQueryDTO dto);

    Page<SettlementItemVO> getSettlementList(long userId, SettlementQueryDTO dto);

    String withdrawApply(long userId, WithdrawApplyDTO dto);

    Page<WithdrawItemVO> getWithdrawList(long userId, WithdrawQueryDTO dto);

}