package com.clmcat.qianyu.user.service;

import com.clmcat.qianyu.user.api.UserLoginApi;
import com.clmcat.qianyu.user.api.model.dto.*;
import com.clmcat.qianyu.user.model.vo.LoginResultVo;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@Service
@DubboService
public class UserLoginServiceBiz implements UserLoginApi {

    @Override
    public LoginResultDto accountLogin(AccountLoginDto dto) {

        return LoginResultDto.builder()
                .token("21345")
                .userId(134)
                .build();
    }

    @Override
    public LoginResultDto phoneLogin(PhoneLoginDto dto) {
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
}
