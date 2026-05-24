package com.clmcat.qianyu.core.login;

import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.verify.DefaultTokenCodec;
import com.clmcat.framework.webmvc.verify.LoginVerifyFunction;
import com.clmcat.framework.webmvc.verify.LoginVerifyService;
import com.clmcat.framework.webmvc.verify.TokenInfo;
import com.clmcat.qianyu.core.api.model.dto.TokenInfoDto;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;

@Slf4j
public class CustomLoginVerifyFunction implements LoginVerifyFunction {

    @Resource
    LoginVerifier loginVerifier;


    @Override
    public boolean verifyLogin(LoginVerify lv, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) {

        String token = LoginVerifyService.getToken(request, lv);

        if (StringUtils.isBlank(token)) {
            return false;
        }
        try {
            CustomTokenInfo fromToken = this.loginVerifier.getFromToken(token, CustomTokenInfo.class);
            if (fromToken == null) {
                return false;
            }

            request.setAttribute(DefaultTokenCodec.TOKEN_INFO_KEY, fromToken);
            return true;
        } catch (Exception e) {
            log.debug("TOKEN 无效！" ,e);
            return false;
        }

    }

    @Override
    public TokenInfo parseTokenInfo(HttpServletRequest request) {
        return (TokenInfo)request.getAttribute(DefaultTokenCodec.TOKEN_INFO_KEY);
    }

    public static class CustomTokenInfo extends TokenInfoDto implements TokenInfo {

        @Override
        public boolean isInvalid() {
            return false;
        }
    }
}
