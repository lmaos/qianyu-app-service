package com.clmcat.qianyu.user.service;

import com.clmcat.basics.commons.util.Base36;
import com.clmcat.basics.commons.util.Base62;
import com.clmcat.basics.commons.util.CodecUtils;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.core.login.LoginSigner;
import com.clmcat.qianyu.core.login.LoginVerifier;
import com.clmcat.qianyu.social.api.base.UserSocialCounterApi;
import com.clmcat.qianyu.user.api.UserLoginApi;
import com.clmcat.qianyu.user.api.model.dto.*;
import com.clmcat.qianyu.user.mapper.UserAuthMapper;
import com.clmcat.qianyu.user.mapper.UserInfoMapper;
import com.clmcat.qianyu.user.api.model.dto.SignerDto;
import com.clmcat.qianyu.user.model.dto.PasswordBindDto;
import com.clmcat.qianyu.user.model.dto.UserAuthDto;
import com.clmcat.qianyu.user.model.dto.UserAuthResultDto;
import com.clmcat.qianyu.user.model.entity.UserAuth;
import com.clmcat.qianyu.user.model.entity.UserInfo;
import com.clmcat.qianyu.user.model.vo.LoginResultVo;
import com.clmcat.qianyu.user.support.LoginSupport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
@DubboService
public class UserLoginServiceBiz implements UserLoginApi  {
    private static final String IDENTITY_TYPE_ACCOUNT = "account";
    private static final String IDENTITY_TYPE_PHONE = "phone";
    private static final String IDENTITY_TYPE_EMAIL = "email";

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
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(dto == null);
        String username = normalizeAccountIdentifier(dto.getUsername());
        String password = dto.getPassword();
        ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(StringUtils.isAnyBlank(username, password));

        UserAuthDto userAuthDto = UserAuthDto.builder()
                .identifier(username)
                .identityType(IDENTITY_TYPE_ACCOUNT)
                .country(country(dto))
                .build();
        UserAuth userAuth = getUserAuth(userAuthDto);
        // 账户不存在，登录失败。
        ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(userAuth == null);
        assert userAuth != null;
        ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(StringUtils.isBlank(userAuth.getCredential()));

        String inputCredential = encodePassword(password);
        // 密码不相等，登录失败。
        ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(!inputCredential.equals(userAuth.getCredential()));

        UserInfo userInfo = getUserInfo(userAuth.getUserId());
        // 账户不存在。
        ResponseStatus.R_ACCOUNT_NOT_EXIST.assertThrowResEx(userInfo == null);

        return toLoginResultDto(UserAuthResultDto.builder()
                .userAuth(userAuth)
                .userInfo(userInfo)
                .build());
    }

    @Override
    public LoginResultDto accountRegister(AccountRegisterDto dto) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(dto == null);
        String username = normalizeAccountIdentifier(dto.getUsername());
        String password = dto.getPassword();
        verifyAccountIdentifier(username);
        verifyBindPassword(password);

        UserAuthDto userAuthDto = UserAuthDto.builder()
                .identifier(username)
                .identityType(IDENTITY_TYPE_ACCOUNT)
                .credential(encodePassword(password))
                .country(country(dto))
                .build();
        ResponseStatus.U_EXIST_ACCOUNT.assertThrowResEx(getUserAuth(userAuthDto) != null);
        return toLoginResultDto(loginOrRegister(userAuthDto));
    }

    @Override
    public LoginResultDto phoneLogin(PhoneLoginDto dto) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(dto == null);
        String rawPhone = dto.getPhone();
        String phone = normalizePhoneIdentifier(rawPhone);
        AuthMode authMode = normalizeAuthMode(dto.getAuthMode());
        ///  采用密码登录
        if (AuthMode.PASSWORD.equals(authMode)) {
            return phonePasswordLogin(phone, dto.getPassword());
        }
        //ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(!verifyCodeServiceBiz.isVerifiedByRedis(IDENTITY_TYPE_PHONE, rawPhone, dto.getCode()));

        UserAuthDto userAuthDto = UserAuthDto.builder()
                .identifier(phone)
                .identityType(IDENTITY_TYPE_PHONE)
                .country(country(dto))
                .build();

        UserAuthResultDto userAuthResultDto = loginOrRegister(userAuthDto);
        return toLoginResultDto(userAuthResultDto);
    }

    @Override
    public LoginResultDto emailLogin(EMailLoginDto dto) {
        ///  右键 CODE 验证
        ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(verifyCodeServiceBiz.isVerifiedByRedis("email", dto.getEmail(), dto.getCode()));

        UserAuthDto userAuthDto = UserAuthDto.builder()
                .identifier(dto.getEmail())
                .identityType(IDENTITY_TYPE_EMAIL)
                .country(country(dto))
                .build();

        UserAuthResultDto userAuthResultDto = loginOrRegister(userAuthDto);
        return toLoginResultDto(userAuthResultDto);
    }



    @Override
    public LoginResultDto socialLogin(SocialLoginDto dto) {

        String identifier = null; // TODO 三方社交登录方法。 通过 code 获得 openId

        UserAuthDto userAuthDto = UserAuthDto.builder()
                .identifier(identifier)
                .identityType(dto.getPlatform().name())
                .country(country(dto))
                .build();
        UserAuthResultDto userAuthResultDto = loginOrRegister(userAuthDto);
        return toLoginResultDto(userAuthResultDto);
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

    /**
     * 账户密码注册
     */
    public LoginResultVo register(AccountRegisterDto dto) {
        LoginResultDto resultDto = accountRegister(dto);
        return toLoginResultVo(resultDto);
    }

    /**
     * 当前登录用户绑定通用密码
     */
    public boolean bindPassword(long userId, PasswordBindDto dto) {
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(userId <= 0);
        ResponseStatus.P_VALUE_ERROR.assertThrowResEx(dto == null);
        verifyBindPassword(dto.getPassword());
        ResponseStatus.R_ACCOUNT_NOT_EXIST.assertThrowResEx(getUserInfo(userId) == null);

        long time = System.currentTimeMillis();
        String credential = encodePassword(dto.getPassword());
        ResponseStatus.R_NOEXIST_DATA.assertThrowResEx(userAuthMapper.updateCredentialByUserId(userId, credential, time) <= 0);
        return true;
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

            if (IDENTITY_TYPE_PHONE.equals(identityType)) {
                phone = identifier;
            }

            if (IDENTITY_TYPE_EMAIL.equals(identityType)) {
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


    String country(LoginDto dto) {
        return "CN";
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
        long expireMillis = getSignerExpireMs(); // 可以拿到过期时间， 也可以在 token 中写入过期时间， 这里就不写了， 直接用默认的过期时间。
        return LoginResultDto.builder()
                .token(token)
                .userId(userId)
                .expireMs(expireMillis)
                .build();
    }

    /**
     * 获得用户授权的信息。
     * @param dto 授权查询参数
     * @return 授权信息
     */
    public UserAuth getUserAuth(UserAuthDto dto) {
        if (dto == null || StringUtils.isAnyBlank(dto.getIdentityType(), dto.getIdentifier())) {
            return null;
        }
        return userAuthMapper.selectByIdentityTypeAndIdentifier(dto.getIdentityType(), dto.getIdentifier());
    }

    /**
     * 获得用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    public UserInfo getUserInfo(long userId) {
        return userInfoMapper.selectOneById(userId);
    }

    private void verifyAccountIdentifier(String username) {
        ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("username").assertThrowEx(StringUtils.isBlank(username) || username.length() > 64);
    }

    private void verifyBindPassword(String password) {
        ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("password").assertThrowEx(StringUtils.isBlank(password) || password.length() < 6 || password.length() > 64);
    }

    private String normalizeAccountIdentifier(String username) {
        return StringUtils.trimToNull(username);
    }

    private String normalizePhoneIdentifier(String phone) {
        String normalizedPhone = LoginSupport.normalizeCnSmsPhone(phone);
        return normalizedPhone == null ? StringUtils.trimToNull(phone) : normalizedPhone;
    }

    private AuthMode normalizeAuthMode(AuthMode authMode) {
        return authMode == null ? AuthMode.CODE : authMode;
    }

    private LoginResultDto phonePasswordLogin(String phone, String password) {
        ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(StringUtils.isAnyBlank(phone, password));
        UserAuth userAuth = getUserAuth(UserAuthDto.builder()
                .identifier(phone)
                .identityType(IDENTITY_TYPE_PHONE)
                .build());
        ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(userAuth == null || StringUtils.isBlank(userAuth.getCredential()));
        ResponseStatus.AUTH_LOGIN_FAIL.assertThrowResEx(!encodePassword(password).equals(userAuth.getCredential()));

        UserInfo userInfo = getUserInfo(userAuth.getUserId());
        ResponseStatus.R_ACCOUNT_NOT_EXIST.assertThrowResEx(userInfo == null);
        return toLoginResultDto(UserAuthResultDto.builder()
                .userAuth(userAuth)
                .userInfo(userInfo)
                .build());
    }

    private String encodePassword(String password) {
        return Base64.getEncoder().encodeToString(CodecUtils.SHA256.encrypt(password));
    }

    /**
     * 签发token
     * @param dto 签发参数
     * @return token
     */
    public String signer(SignerDto dto) {
        return loginSigner.generateToken(dto);
    }
    public long getSignerExpireMs() {
        return loginSigner.getExpireMillis();
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
