package com.clmcat.qianyu.user.service;

import com.clmcat.framework.webmvc.ResponseStatus;
import com.clmcat.qianyu.user.model.dto.PhoneVerifyDto;
import com.clmcat.qianyu.user.model.vo.PhoneVerifyVo;
import com.clmcat.qianyu.user.support.LoginSupport;
import com.clmcat.qianyu.user.support.sms.AliyunSmsProperties;
import com.clmcat.qianyu.user.support.sms.AliyunSmsSender;
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

    private static final String PHONE_VERIFY_TEMPLATE_CODE = "SMS_256930197";

    @Resource
    VerifyCodeServiceBiz verifyCodeServiceBiz;

    @Resource
    AliyunSmsSender aliyunSmsSender;

    @Resource
    AliyunSmsProperties aliyunSmsProperties;

    /**
     * 发送手机验证码
     * @param dto 手机号
     */
    public PhoneVerifyVo sendVerifyCode(PhoneVerifyDto dto) {
        /// 测试手机，直接返回成功。（假手机，就是个测试号）
        if (LoginSupport.isTestPhone(dto.getPhone())) {
            return new PhoneVerifyVo();
        }

        String phone = dto.getPhone();
        ///  验证手机号。
        ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("phone").assertThrowEx(!phone.startsWith("+86"));
        ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("phone").assertThrowEx(!LoginSupport.isValidTelephone(phone));
        String smsPhone = LoginSupport.normalizeCnSmsPhone(phone);
        ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("phone").assertThrowEx(smsPhone == null);
        ///  随机验证码
        String code = RandomUtils.secure().randomInt(100000, 999999) + "";
        String templateParam = "{\"code\":\"" + code + "\"}";
        try {
            verifyCodeServiceBiz.saveVerifyCodeToRedis("phone", phone, code, 5 * 60 * 1000);
        } catch (Exception e) {
            log.error("redis请求失败", e);
            throw ResponseStatus.F_SERVICE_UNAVAILABLE.apiEx("验证码发送失败，请稍后再试");
        }
        try {
            aliyunSmsSender.send(smsPhone, aliyunSmsProperties.getSignName(), PHONE_VERIFY_TEMPLATE_CODE, templateParam);
        } catch (IllegalStateException e) {
            verifyCodeServiceBiz.deleteVerifyCodeToRedis("phone", phone);
            if (e.getMessage() != null && e.getMessage().contains("手机号码格式错误")) {
                throw ResponseStatus.P_VALUE_ERROR.apiEx().setErrplace("phone").setMessage("手机号格式错误");
            }
            throw ResponseStatus.F_SERVICE_UNAVAILABLE.apiEx("验证码发送失败，请稍后再试");
        }

        log.info("发送手机验证码成功, phone: {}, code: {}", maskPhone(phone), code);
        PhoneVerifyVo vo = new PhoneVerifyVo();
        vo.setNeedSecondVerify(false);
        return vo;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() <= 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
