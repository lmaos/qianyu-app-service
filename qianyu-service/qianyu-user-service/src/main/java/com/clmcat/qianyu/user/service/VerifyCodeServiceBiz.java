package com.clmcat.qianyu.user.service;

import com.clmcat.qianyu.user.model.vo.PhoneVerifyVo;
import com.clmcat.qianyu.user.support.LoginSupport;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
public class VerifyCodeServiceBiz {

    @Resource
    RedisTemplate<String, String> redisTemplate;


    public void saveVerifyCodeToRedis(String identityType, String identifier, String code, long expireTime) {
        String verifyCodeKey = getVerifyCodeKey(identityType, identifier);
        redisTemplate.opsForValue().set(verifyCodeKey, code, expireTime, TimeUnit.MILLISECONDS);
    }


    public boolean isVerifiedByRedis(String identityType, String identifier, String code) {
        /// 测试手机号，直接通过验证即可。
        if (LoginSupport.isTestPhone(identifier)) {
            return true;
        }

        String verifyCodeKey = getVerifyCodeKey(identityType, identifier);

        Long expire = redisTemplate.getExpire(verifyCodeKey, TimeUnit.MILLISECONDS);

        /// 过期了， 不要了。 提前 2ms 容错。
        if (expire == null || expire <= 2) {
            return false;
        }

        ///  Redis中存储的验证码
        String storedCode = redisTemplate.opsForValue().get(verifyCodeKey);

        /// 验证失败
        if (storedCode == null) {
            return false;
        }

        ///  最大验证次数 25
        if (redisTemplate.opsForValue().increment(verifyCodeKey + ".increase", 1) <= 25) {
            if (storedCode.equals(code)) {
                redisTemplate.delete(Arrays.asList(verifyCodeKey, verifyCodeKey + ".incr", verifyCodeKey + ".status"));
                return true;
            }
        } else {
            redisTemplate.delete(Arrays.asList(verifyCodeKey, verifyCodeKey + ".incr", verifyCodeKey + ".status"));
        }

        return false;
    }



    public String getVerifyCodeKey(String identityType, String identifier) {
        return "VERIFY_CODE_" + identityType + "_" + identifier;
    }

}
