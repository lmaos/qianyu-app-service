package com.clmcat.qianyu.user.service;

import com.clmcat.qianyu.user.api.UserApi;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import com.clmcat.qianyu.user.mapper.UserInfoMapper;
import com.clmcat.qianyu.user.model.entity.UserInfo;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@DubboService
@Service
public class UserServiceBiz implements UserApi {


    @Resource
    UserInfoMapper userInfoMapper;

    @Override
    public RpcUserInfoDto getUserInfo(long userId) {
        UserInfo userInfo = userInfoMapper.selectOneById(userId);
        if (userInfo == null){
            return null;
        }
        RpcUserInfoDto  rpcUserInfoDto = new RpcUserInfoDto();
        BeanUtils.copyProperties(userInfo,rpcUserInfoDto);
        return rpcUserInfoDto;
    }

    @Override
    public List<RpcUserInfoDto> getUserInfoList(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<UserInfo> userInfos = userInfoMapper.selectListByIds(userIds);
        if (userInfos == null){
            return new ArrayList<>();
        }
        Map<Long, RpcUserInfoDto> userInfoMap = new LinkedHashMap<>();
        for (UserInfo userInfo : userInfos) {
            RpcUserInfoDto rpcUserInfoDto = new RpcUserInfoDto();
            BeanUtils.copyProperties(userInfo,rpcUserInfoDto);
            userInfoMap.put(rpcUserInfoDto.getUserId(), rpcUserInfoDto);
        }
        List<RpcUserInfoDto> rpcUserInfoDtos = new ArrayList<>();
        for (Long userId : userIds) {
            RpcUserInfoDto rpcUserInfoDto = userInfoMap.get(userId);
            if (rpcUserInfoDto != null) {
                rpcUserInfoDtos.add(rpcUserInfoDto);
            }
        }
        return rpcUserInfoDtos;
    }

}
