package com.clmcat.qianyu.user.support;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.basics.commons.snowflake.SnowflakeCustomBuilder;
import com.clmcat.basics.commons.snowflake.strategy.MachineStrategy;
import com.clmcat.basics.commons.snowflake.strategy.SequenceStrategy;
import com.clmcat.basics.commons.snowflake.strategy.TimeStrategy;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginSupport {
    /**
     * 雪花算法基准时间， 项目上线后不可再修改
     */
    public static final long BASE_TIME = SnowflakeSupport.BASE_TIME;
    /**
     * 雪花算法
     */
    public static final CustomSnowflake LOGIN_USER_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
//    public static final CustomSnowflake LOGIN_USER_ID_SNOWFLAKE = SnowflakeCustomBuilder.builder()
//            .add(0) // 首位 0
//            .add("TimeStrategy", 42, TimeStrategy.millisecond(BASE_TIME))
//            .add("MachineStrategy", 10, MachineStrategy.autoByIp())
//            .add("SequenceStrategy", 11, SequenceStrategy.create(), "TimeStrategy")
//            .build();

    /**
     * 根据雪花算法解析登录用户ID中的时间戳
     * @param userId 用户ID
     * @return 时间戳
     */
    public static long parseTimeBySnowflake(long userId) {
        return SnowflakeSupport.parseTimeBySnowflake(LOGIN_USER_ID_SNOWFLAKE, userId);
    }

    public static long allocUserId() {
        return LOGIN_USER_ID_SNOWFLAKE.nextId();
    }


    public  static boolean isValidTelephone(String telephone) {
        if (StringUtils.isBlank(telephone)) {
            return false;
        }
        if (!telephone.startsWith("+")) {
            return false;
        }
        if (telephone.length() < 5 || telephone.indexOf('-', 2, 6) == -1) {
            return false;
        }

        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber num =  util.parse(telephone , null);
            return util.isValidNumber(num);
        } catch (NumberParseException e) {
            log.debug("手机解析失败, {}" ,telephone ,e);
            return false;
        }
    }

    /**
     * 转为阿里云国内短信使用的 11 位手机号。
     *
     * @param telephone 带国家码的手机号，例如 +86-13800138000
     * @return 11 位国内手机号；不合法时返回 null
     */
    public static String normalizeCnSmsPhone(String telephone) {
        if (!isValidTelephone(telephone)) {
            return null;
        }
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber num = util.parse(telephone, null);
            if (!util.isValidNumber(num) || num.getCountryCode() != 86) {
                return null;
            }
            String nationalNumber = String.valueOf(num.getNationalNumber());
            if (nationalNumber.length() != 11 || !nationalNumber.startsWith("1")) {
                return null;
            }
            return nationalNumber;
        } catch (NumberParseException e) {
            log.debug("手机归一化失败, {}", telephone, e);
            return null;
        }
    }


}
