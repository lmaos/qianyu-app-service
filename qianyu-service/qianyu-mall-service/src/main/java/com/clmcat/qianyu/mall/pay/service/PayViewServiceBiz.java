package com.clmcat.qianyu.mall.pay.service;

import com.clmcat.qianyu.mall.pay.model.dto.PayApplyDTO;
import com.clmcat.qianyu.mall.pay.model.dto.PayResultQueryDTO;
import com.clmcat.qianyu.mall.pay.model.vo.PayApplyVO;
import com.clmcat.qianyu.mall.pay.model.vo.PayResultVO;

public interface PayViewServiceBiz {

    PayApplyVO payApply(Long userId, PayApplyDTO dto);

    PayResultVO payResult(Long userId, PayResultQueryDTO dto);

    String handleWechatCallback(String rawBody);

    String handleAlipayCallback(java.util.Map<String, String> params);

}