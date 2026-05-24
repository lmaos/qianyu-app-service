package com.clmcat.qianyu.user.api;

import com.clmcat.qianyu.user.api.model.dto.*;

public interface UserLoginApi {

    LoginResultDto accountLogin(AccountLoginDto dto);

    LoginResultDto phoneLogin(PhoneLoginDto dto);

    LoginResultDto emailLogin(EMailLoginDto dto);

    LoginResultDto socialLogin(SocialLoginDto dto);

    /**
     * 解析token；如果发生异常，则无效。
     * @param token token
     * @return 签发的数据
     */
    SignerDto verifier(String token);

    /**
     * true 有效, false 无效
     * @param token token
     * @return 是否有效
     */
    boolean isValidToken(String token);
}
