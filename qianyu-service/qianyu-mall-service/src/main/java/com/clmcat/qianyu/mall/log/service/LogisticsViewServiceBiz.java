package com.clmcat.qianyu.mall.log.service;

import com.clmcat.qianyu.mall.log.model.dto.LogisticsCreateDTO;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsPushDTO;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsQueryDTO;
import com.clmcat.qianyu.mall.log.model.dto.LogisticsUpdateDTO;
import com.clmcat.qianyu.mall.log.model.vo.LogisticsDetailVO;

public interface LogisticsViewServiceBiz {

    LogisticsDetailVO queryByOrderId(long userId, LogisticsQueryDTO dto);

    LogisticsDetailVO trackRealtime(long userId, LogisticsQueryDTO dto);

    Long createLogistics(long userId, LogisticsCreateDTO dto);

    void updateLogistics(long userId, LogisticsUpdateDTO dto);

    Boolean handlePush(LogisticsPushDTO dto);

}