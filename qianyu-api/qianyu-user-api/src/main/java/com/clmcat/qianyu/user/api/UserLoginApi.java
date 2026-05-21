package com.clmcat.qianyu.user.api;

import com.clmcat.qianyu.user.api.model.dto.*;

public interface UserLoginApi {



    LoginResultDto accountLogin(AccountLoginDto dto);

    LoginResultDto phoneLogin(PhoneLoginDto dto);
    LoginResultDto emailLogin(EMailLoginDto dto);

    LoginResultDto socialLogin(SocialLoginDto dto);


}
