package com.clmcat.qianyu.user.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.qianyu.user.api.model.dto.AccountLoginDto;
import com.clmcat.qianyu.user.api.model.dto.EMailLoginDto;
import com.clmcat.qianyu.user.api.model.dto.PhoneLoginDto;
import com.clmcat.qianyu.user.api.model.dto.SocialLoginDto;
import com.clmcat.qianyu.user.model.vo.LoginResultVo;
import com.clmcat.qianyu.user.service.UserLoginServiceBiz;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;

@ApiController
@RequestMapping("/api/user/login")
@Slf4j
public class LoginUserController {

    @Resource
    UserLoginServiceBiz userLoginServiceBiz;


    @RequestMapping("/phone")
    public LoginResultVo phone(@Params PhoneLoginDto dto) {
        return userLoginServiceBiz.phone(dto);
    }
    @RequestMapping("/email")
    public LoginResultVo email(@Params EMailLoginDto dto) {
        return userLoginServiceBiz.email(dto);
    }
    @RequestMapping("/social")
    public LoginResultVo social(@Params SocialLoginDto dto) {
        return userLoginServiceBiz.social(dto);
    }
    @RequestMapping("/account")
    public LoginResultVo account(@Params AccountLoginDto dto) {
        log.info("account:{}", dto);
        return userLoginServiceBiz.account(dto);
    }

}
