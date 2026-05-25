package com.clmcat.qianyu.social.base.service;

import com.clmcat.qianyu.social.api.base.UserSocialCounterApi;
import com.clmcat.qianyu.social.api.base.model.dto.UserSocialCounterDto;
import com.clmcat.qianyu.social.base.mapper.UserSocialCounterMapper;
import com.clmcat.qianyu.social.base.model.entity.UserSocialCounter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@DubboService
@Slf4j
public class UserSocialCounterServiceBiz implements UserSocialCounterApi {

    @Resource
    UserSocialCounterMapper  userSocialCounterMapper;

    @Override
    public void increment(UserSocialCounterDto dto) {
        if (dto.getUserId() == null) {
            log.error("UserSocialCounterServiceBiz increment failed: userId is null");
            return;
        }
        UserSocialCounter  userSocialCounter = new UserSocialCounter();
        BeanUtils.copyProperties(dto, userSocialCounter);
        if (userSocialCounterMapper.incrementById(userSocialCounter) == 0) {
            /// 极端情况缺少数据的情况简单兼容一下。
            log.warn("UserSocialCounterServiceBiz incrementById returned 0, trying insertIfNotExist: userId={}", dto.getUserId());
            userSocialCounterMapper.insertIfNotExist(dto.getUserId());
            userSocialCounterMapper.incrementById(userSocialCounter);
        }
    }

    @Override
    public void init(long userId) {
        userSocialCounterMapper.insertIfNotExist(userId);
    }
}
