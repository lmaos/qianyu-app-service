package com.clmcat.qianyu.user.service;

import com.clmcat.basics.commons.util.Base36;
import com.clmcat.basics.commons.util.Base62;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.user.api.UserLoginApi;
import com.clmcat.qianyu.user.api.model.dto.*;
import com.clmcat.qianyu.user.mapper.UserAuthMapper;
import com.clmcat.qianyu.user.mapper.UserInfoMapper;
import com.clmcat.qianyu.user.model.dto.UserAuthDto;
import com.clmcat.qianyu.user.model.dto.UserAuthResultDto;
import com.clmcat.qianyu.user.model.entity.UserAuth;
import com.clmcat.qianyu.user.model.entity.UserInfo;
import com.clmcat.qianyu.user.model.vo.LoginResultVo;
import com.clmcat.qianyu.user.support.LoginSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@DubboService
public class UserLoginServiceBiz implements UserLoginApi  {

    @Resource
    UserAuthMapper  userAuthMapper ;

    @Resource
    UserInfoMapper  userInfoMapper ;

    @Resource
    VerifyCodeServiceBiz verifyCodeServiceBiz;




    @Override
    public LoginResultDto accountLogin(AccountLoginDto dto) {

        return LoginResultDto.builder()
                .token("21345")
                .userId(134)
                .build();
    }

    @Override
    public LoginResultDto phoneLogin(PhoneLoginDto dto) {
        // 登录失败, 手机号： +86-12345678911
        ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(LoginSupport.isValidTelephone(dto.getPhone()));
        ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(verifyCodeServiceBiz.isVerifiedByRedis("phone", dto.getPhone(), dto.getCode()));


        return LoginResultDto.builder()
                .token("21345")
                .userId(134)
                .build();
    }

    @Override
    public LoginResultDto emailLogin(EMailLoginDto dto) {
        return LoginResultDto.builder()
                .token("21345")
                .userId(134)
                .build();
    }

    @Override
    public LoginResultDto socialLogin(SocialLoginDto dto) {
        return LoginResultDto.builder()
                .token("21345")
                .userId(134)
                .build();
    }



    public LoginResultVo phone(PhoneLoginDto dto) {
        LoginResultDto resultDto = phoneLogin(dto);

        return toLoginResultVo(resultDto);
    }

    public LoginResultVo email(EMailLoginDto dto) {
        LoginResultDto resultDto = emailLogin(dto);
        return toLoginResultVo(resultDto);
    }

    public LoginResultVo social(SocialLoginDto dto) {
        LoginResultDto resultDto = socialLogin(dto);
        return toLoginResultVo(resultDto);
    }

    public LoginResultVo account(AccountLoginDto dto) {
        LoginResultDto resultDto = accountLogin(dto);
        return toLoginResultVo(resultDto);
    }

    LoginResultVo toLoginResultVo(LoginResultDto dto) {
        return LoginResultVo.builder()
                .token(dto.getToken())
                .build();
    }

    /**
     *
     * @param dto
     * @return
     */
    @Transactional
    public UserAuthResultDto loginOrRegister(UserAuthDto dto) {

        UserAuth userAuth = getUserAuth(dto);
        UserInfo userInfo;
        if  (userAuth == null) {
            String identityType = dto.getIdentityType();
            String identifier = dto.getIdentifier();
            long userId = LoginSupport.allocUserId();
            String phone = dto.getSocialPhone();
            String email = dto.getSocialEmail();

            if ("phone".equals(identityType)) {
                phone = identifier;
            }

            if ("email".equals(identityType)) {
                email = identifier;
            }

            long time = LoginSupport.parseTimeBySnowflake(userId);

            userAuth = UserAuth.builder()
                    .userId(userId)
                    .identityType(identityType)
                    .identifier(identifier)
                    .credential(dto.getCredential())
                    .createTime(time)
                    .updateTime(time)
                    .build();
            userInfo = UserInfo.builder()
                    .userId(userId)
                    .nickname(identifier)
                    .avatar(dto.getSocialAvatar())
                    .age(0)
                    .gender(0)
                    .bio("")
                    .status(0)
                    .phone(phone)
                    .email(email)
                    .phoneVerifiedTime(StringUtils.isNotBlank(phone) ? time : null)
                    .nickname(Base36.encode(userId)) // 直接编码一个用户昵称。
                    .userNo(Base62.encode(userId))   // 直接编码一个用户NO。
                    .createTime(time)
                    .updateTime(time)
                    .build();

            userAuthMapper.insert(userAuth);
            userInfoMapper.insert(userInfo);

        } else {
            userInfo = getUserInfo(userAuth.getUserId());
        }

        return UserAuthResultDto.builder()
                .userAuth(userAuth)
                .userInfo(userInfo)
                .build();


    }


    public UserAuth getUserAuth(UserAuthDto dto) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(UserAuth::getIdentityType, dto.getIdentityType())
                .eq(UserAuth::getIdentifier, dto.getIdentifier());
        return userAuthMapper.selectOneByQuery(queryWrapper);
    }

    public UserInfo getUserInfo(long userId) {
        return userInfoMapper.selectOneById(userId);
    }


}
