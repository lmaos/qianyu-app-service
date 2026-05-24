package com.clmcat.qianyu.user.support;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.basics.commons.snowflake.SnowflakeCustomBuilder;
import com.clmcat.basics.commons.snowflake.strategy.MachineStrategy;
import com.clmcat.basics.commons.snowflake.strategy.SequenceStrategy;
import com.clmcat.basics.commons.snowflake.strategy.TimeStrategy;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoginSupport {
    /**
     * 雪花算法基准时间， 项目上线后不可再修改
     */
    public static final long BASE_TIME = 1779465600000L;
    /**
     * 雪花算法
     */
    public static final CustomSnowflake LOGIN_USER_ID_SNOWFLAKE = SnowflakeCustomBuilder.builder()
            .add(0) // 首位 0
            .add("TimeStrategy", 42, TimeStrategy.millisecond(BASE_TIME))
            .add("MachineStrategy", 10, MachineStrategy.autoByIp())
            .add("SequenceStrategy", 11, SequenceStrategy.create(), "TimeStrategy")
            .build();

    /**
     * 根据雪花算法解析登录用户ID中的时间戳
     * @param userId 用户ID
     * @return 时间戳
     */
    public static long parseTimeBySnowflake(long userId) {
        return LOGIN_USER_ID_SNOWFLAKE.get("TimeStrategy", userId) + BASE_TIME;
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


}
