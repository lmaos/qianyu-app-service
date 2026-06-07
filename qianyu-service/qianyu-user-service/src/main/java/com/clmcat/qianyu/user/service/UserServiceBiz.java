package com.clmcat.qianyu.user.service;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.user.api.UserApi;
import com.clmcat.qianyu.user.api.model.dto.RpcUserInfoDto;
import com.clmcat.qianyu.user.mapper.UserInfoMapper;
import com.clmcat.qianyu.user.model.dto.UserInfoUpdateDto;
import com.clmcat.qianyu.user.model.entity.UserInfo;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
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

    @Override
    public RpcUserInfoDto getUserInfoByUserNo(String userNo) {
        // 参数校验
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userNo == null);
        String trimmed = userNo.trim();
        ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("userNo").assertThrowEx(trimmed.isEmpty() || trimmed.length() > 64);

        // 命中返回 1 条 / 未命中返回 null（UNIQUE KEY 兜底）
        UserInfo userInfo = userInfoMapper.selectByUserNo(trimmed);
        if (userInfo == null) {
            return null;
        }
        RpcUserInfoDto rpcUserInfoDto = new RpcUserInfoDto();
        BeanUtils.copyProperties(userInfo, rpcUserInfoDto);
        return rpcUserInfoDto;
    }

    public RpcUserInfoDto updateUserInfo(long userId, UserInfoUpdateDto dto) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        verifyUpdateDto(dto);

        UserInfo updateUserInfo = buildUpdateUserInfo(userId, dto);

        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(!hasUpdatableField(updateUserInfo));
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx(userInfoMapper.updateByUserIdSelective(updateUserInfo) <= 0);
        return getUserInfo(userId);
    }

    private void verifyUpdateDto(UserInfoUpdateDto dto) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(dto == null);
        if (dto.getNickname() != null) {
            String nickname = dto.getNickname().trim();
            ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("nickname").assertThrowEx(nickname.isEmpty() || nickname.length() > 64);
        }
        if (dto.getAvatar() != null) {
            ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("avatar").assertThrowEx(dto.getAvatar().trim().length() > 512);
        }
        if (dto.getBio() != null) {
            ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("bio").assertThrowEx(dto.getBio().trim().length() > 1000);
        }
        if (dto.getGender() != null) {
            int gender = dto.getGender();
            ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("gender").assertThrowEx(gender < 0 || gender > 2);
        }
        if (dto.getBirthday() != null) {
            ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("birthday").assertThrowEx(dto.getBirthday().isAfter(LocalDate.now()));
        }
        if (dto.getCountry() != null) {
            String country = dto.getCountry().trim().toUpperCase();
            ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("country").assertThrowEx(country.length() != 2 || !country.matches("[A-Z]{2}"));
        }
        if (dto.getProvince() != null) {
            ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("province").assertThrowEx(dto.getProvince().trim().length() > 64);
        }
        if (dto.getCity() != null) {
            ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("city").assertThrowEx(dto.getCity().trim().length() > 64);
        }
    }

    private boolean hasUpdatableField(UserInfo userInfo) {
        return userInfo.getNickname() != null
                || userInfo.getAvatar() != null
                || userInfo.getBio() != null
                || userInfo.getGender() != null
                || userInfo.getBirthday() != null
                || userInfo.getCountry() != null
                || userInfo.getProvince() != null
                || userInfo.getCity() != null;
    }

    private UserInfo buildUpdateUserInfo(long userId, UserInfoUpdateDto dto) {
        UserInfo updateUserInfo = new UserInfo();
        updateUserInfo.setUserId(userId);
        updateUserInfo.setNickname(trimToNull(dto.getNickname()));
        updateUserInfo.setAvatar(trimToNull(dto.getAvatar()));
        updateUserInfo.setBio(trimToNull(dto.getBio()));
        updateUserInfo.setGender(dto.getGender());
        updateUserInfo.setBirthday(dto.getBirthday());
        updateUserInfo.setCountry(normalizeCountry(dto.getCountry()));
        updateUserInfo.setProvince(trimToNull(dto.getProvince()));
        updateUserInfo.setCity(trimToNull(dto.getCity()));
        updateUserInfo.setUpdateTime(System.currentTimeMillis());
        if (updateUserInfo.getBirthday() != null) {
            updateUserInfo.setAge(calculateAge(updateUserInfo.getBirthday()));
        }
        return updateUserInfo;
    }

    private String normalizeCountry(String country) {
        String normalizedCountry = trimToNull(country);
        return normalizedCountry == null ? null : normalizedCountry.toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private int calculateAge(LocalDate birthday) {
        return Math.max(0, Period.between(birthday, LocalDate.now()).getYears());
    }
}
