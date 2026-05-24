package com.clmcat.qianyu.user.service;

import ch.qos.logback.core.testUtil.RandomUtil;
import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.user.model.dto.PhoneVerifyDto;
import com.clmcat.qianyu.user.model.vo.PhoneVerifyVo;
import com.clmcat.qianyu.user.support.LoginSupport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

/**
 * 手机验证码发送
 */
@Slf4j
@Service
public class PhoneVerifyCodeSendServiceBiz {

    @Resource
    VerifyCodeServiceBiz verifyCodeServiceBiz;

    /**
     * 发送手机验证码
     * @param dto 手机号
     */
    public PhoneVerifyVo sendVerifyCode(PhoneVerifyDto dto) {
        String phone = dto.getPhone();
        ///  验证手机号。
        ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("phone").assertThrowEx(LoginSupport.isValidTelephone(phone));
        ///  随机验证码
        String code = RandomUtils.secure().randomInt(100000, 999999) + "";
        // 发送验证码， 这里省略了短信发送的逻辑， 直接把验证码存储到 Redis 中， 过期时间 5分钟
        // TODO
        verifyCodeServiceBiz.saveVerifyCodeToRedis("phone", phone, code, 5 * 60 * 1000);
        log.info("发送手机验证码， phone: {}, code: {}", phone, code);
        return new PhoneVerifyVo();
    }
}
