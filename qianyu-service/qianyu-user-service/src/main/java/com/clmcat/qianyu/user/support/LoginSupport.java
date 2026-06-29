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

import java.util.Set;

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

    public static final Set<String> TEST_PHONES = Set.of(
            // 原始白名单
            "+86-13800138000", "+86-10000000000",
            // Mock 测试账号白名单（21 个，对应 user_info 表的 user_id 5257117397155841 / 5274664540569601 ~ 5274664620261396）
            // 这些账号在 mock 数据中都已设置统一密码 123456，加入白名单后可直接登录（也可走密码校验）
            "+86-13800138001", "+86-13800138002", "+86-13800138003", "+86-13800138004",
            "+86-13800138005", "+86-13800138006", "+86-13800138007", "+86-13800138008",
            "+86-13800138009", "+86-13800138010", "+86-13800138011", "+86-13800138012",
            "+86-13800138013", "+86-13800138014", "+86-13800138015", "+86-13800138016",
            "+86-13800138017", "+86-13800138018", "+86-13800138019",
            // Shop-0 商家绑定账号（user_id=5274664540569601）
            "+86-13800138020"
    );

    public static boolean  isTestPhone(String phone) {
        return TEST_PHONES.contains(phone);
    }

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

