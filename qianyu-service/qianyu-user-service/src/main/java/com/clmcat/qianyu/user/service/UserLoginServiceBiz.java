package com.clmcat.qianyu.user.service;

import com.clmcat.basics.commons.util.Base36;
import com.clmcat.basics.commons.util.Base62;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.core.login.LoginSigner;
import com.clmcat.qianyu.core.login.LoginVerifier;
import com.clmcat.qianyu.social.api.base.UserSocialCounterApi;
import com.clmcat.qianyu.user.api.UserLoginApi;
import com.clmcat.qianyu.user.api.model.dto.*;
import com.clmcat.qianyu.user.mapper.UserAuthMapper;
import com.clmcat.qianyu.user.mapper.UserInfoMapper;
import com.clmcat.qianyu.user.api.model.dto.SignerDto;
import com.clmcat.qianyu.user.model.dto.UserAuthDto;
import com.clmcat.qianyu.user.model.dto.UserAuthResultDto;
import com.clmcat.qianyu.user.model.entity.UserAuth;
import com.clmcat.qianyu.user.model.entity.UserInfo;
import com.clmcat.qianyu.user.model.vo.LoginResultVo;
import com.clmcat.qianyu.user.support.LoginSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@DubboService
public class UserLoginServiceBiz implements UserLoginApi  {

    @Resource
    UserAuthMapper  userAuthMapper ;

    @Resource
    UserInfoMapper  userInfoMapper ;

    @Resource
    VerifyCodeServiceBiz verifyCodeServiceBiz;

    @Resource
    LoginSigner loginSigner;

    @Resource
    LoginVerifier loginVerifier;

    @DubboReference
    UserSocialCounterApi userSocialCounterApi;

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

        UserAuthDto userAuthDto = UserAuthDto.builder()
                .identifier(dto.getPhone())
                .identityType("phone")
                .country("CN")
                .build();

        UserAuthResultDto userAuthResultDto = loginOrRegister(userAuthDto);
        return toLoginResultDto(userAuthResultDto);
    }

    @Override
    public LoginResultDto emailLogin(EMailLoginDto dto) {

        ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(verifyCodeServiceBiz.isVerifiedByRedis("email", dto.getEmail(), dto.getCode()));

        UserAuthDto userAuthDto = UserAuthDto.builder()
                .identifier(dto.getEmail())
                .identityType("email")
                .country("CN")
                .build();

        UserAuthResultDto userAuthResultDto = loginOrRegister(userAuthDto);
        return toLoginResultDto(userAuthResultDto);
    }



    @Override
    public LoginResultDto socialLogin(SocialLoginDto dto) {
        return LoginResultDto.builder()
                .token("21345")
                .userId(134)
                .build();
    }




    /**
     * 手机登录
     */
    public LoginResultVo phone(PhoneLoginDto dto) {
        LoginResultDto resultDto = phoneLogin(dto);
        return toLoginResultVo(resultDto);
    }

    /**
     * 邮件登录
     */
    public LoginResultVo email(EMailLoginDto dto) {
        LoginResultDto resultDto = emailLogin(dto);
        return toLoginResultVo(resultDto);
    }

    /**
     * 社交账户登录
     */
    public LoginResultVo social(SocialLoginDto dto) {
        LoginResultDto resultDto = socialLogin(dto);
        return toLoginResultVo(resultDto);
    }

    /**
     * 账户密码登录
     */
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
     * 登录或注册
     * @param dto
     * @return
     */
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

            try {
                userSocialCounterApi.init(userId);
            } catch (Exception e) {
                log.error("初始化用户社交计数器失败，userId={}", userId, e);
            }

        } else {
            userInfo = getUserInfo(userAuth.getUserId());
        }

        return UserAuthResultDto.builder()
                .userAuth(userAuth)
                .userInfo(userInfo)
                .build();


    }

    /**
     * 授权信息 转为 登录结果
     * @param userAuthResultDto 授权信息
     * @return 登录结果
     */
    LoginResultDto toLoginResultDto(UserAuthResultDto userAuthResultDto) {
        UserInfo userInfo = userAuthResultDto.getUserInfo();
        Long userId = userInfo.getUserId();
        String country = userInfo.getCountry();

        SignerDto signerDto = new SignerDto();
        signerDto.setCountry(country);
        signerDto.setUserId(userId);

        String token = signer(signerDto);

        return LoginResultDto.builder()
                .token(token)
                .userId(userId)
                .build();
    }

    /**
     * 获得用户授权的信息。
     * @param dto 授权查询参数
     * @return 授权信息
     */
    public UserAuth getUserAuth(UserAuthDto dto) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        queryWrapper.eq(UserAuth::getIdentityType, dto.getIdentityType())
                .eq(UserAuth::getIdentifier, dto.getIdentifier());
        return userAuthMapper.selectOneByQuery(queryWrapper);
    }

    /**
     * 获得用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    public UserInfo getUserInfo(long userId) {
        return userInfoMapper.selectOneById(userId);
    }

    /**
     * 签发token
     * @param dto 签发参数
     * @return token
     */
    public String signer(SignerDto dto) {
        return loginSigner.generateToken(dto);
    }

    /**
     * 验证 token 转为签发的传输对象
     * @param token token
     * @return 解析后的对象。
     */
    @Override
    public SignerDto verifier(String token) {
        try {
            return loginVerifier.getFromToken(token, SignerDto.class);
        } catch (Exception e) {
            log.debug("签发的 TOKEN 解析失败", e);
            throw ResponseStatus.AUTH_TOKEN_INVALID.apiEx();
        }
    }

    @Override
    public boolean isValidToken(String token) {
        try {
            return loginVerifier.getFromToken(token, SignerDto.class) != null;
        } catch (Exception e) {
            log.debug("签发的 TOKEN 解析失败", e);
            return false;
        }
    }
}
