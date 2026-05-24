package com.clmcat.qianyu.core.login;

import com.clmcat.basics.commons.lang.StringUtils;
import com.clmcat.qianyu.core.jwt.JwtRsaKit;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.util.Map;
/**
 * 登录 TOKEN 签发
 */
@Component
public class LoginSigner {

    private final LoginSignerProperties properties;

    private final JwtRsaKit.JwtSigner jwtSigner;

    public LoginSigner(LoginSignerProperties properties) throws Exception {
        this.properties = properties;
        if (StringUtils.isNotBlank(properties.getPrivateKeyPath())) {
            jwtSigner = new JwtRsaKit.JwtSigner(properties.getPrivateKeyPath());
        } else if (StringUtils.isNotBlank(properties.getPrivateKey())) {
            PrivateKey privateKey = JwtRsaKit.parserPrivateKey(properties.getPrivateKey());
            jwtSigner = new JwtRsaKit.JwtSigner(privateKey);
        } else {
            jwtSigner = null;
        }
    }

    /**
     * 通过 Map 生成 JWT Token
     *
     * @param claims 自定义 claims 数据
     * @return JWT 字符串
     */
    public String generateToken(Map<String, Object> claims) {
        if (jwtSigner == null) {
            throw new IllegalStateException("未配置 RSA 私钥，无法签发 Token");
        }
        return jwtSigner.generateToken(claims);
    }

    /**
     * 通过 Map 生成 JWT Token（自定义过期时间）
     *
     * @param claims       自定义 claims 数据
     * @param expireMillis 过期时间（毫秒）
     * @return JWT 字符串
     */
    public String generateToken(Map<String, Object> claims, long expireMillis) {
        if (jwtSigner == null) {
            throw new IllegalStateException("未配置 RSA 私钥，无法签发 Token");
        }
        return jwtSigner.generateToken(claims, expireMillis);
    }

    /**
     * 通过任意对象生成 JWT Token（使用默认过期时间）
     * <p>
     * 通过对象的 getter 方法提取属性值，getter 方法需符合 JavaBean 规范：
     * - getXxx() 返回值非 void、无参数
     * - 布尔类型可以使用 isXxx()
     * 支持继承父类的 getter 方法。
     * </p>
     *
     * @param obj 需要转换的对象
     * @return JWT 字符串
     */
    public String generateToken(Object obj) {
        return generateToken(obj, properties.getExpireMillis());
    }

    /**
     * 通过任意对象生成 JWT Token（自定义过期时间）
     *
     * @param obj          需要转换的对象
     * @param expireMillis 过期时间（毫秒）
     * @return JWT 字符串
     */
    public String generateToken(Object obj, long expireMillis) {
        if (obj == null) {
            throw new IllegalArgumentException("对象不能为 null");
        }
        Map<String, Object> claims = extractClaimsFromObject(obj);
        return generateToken(claims, expireMillis);
    }

    /**
     * 从对象中提取所有非 null 的 getter 属性值
     */
    private Map<String, Object> extractClaimsFromObject(Object obj) {
        Map<String, Object> claims = new java.util.HashMap<>();
        Class<?> clazz = obj.getClass();
        // 获取当前类及其所有父类的方法（直到 Object）
        while (clazz != null && clazz != Object.class) {
            java.lang.reflect.Method[] methods = clazz.getDeclaredMethods();
            for (java.lang.reflect.Method method : methods) {
                String methodName = method.getName();
                // 筛选 getter/is 方法：无参数、返回值不是 void
                if (method.getParameterCount() == 0 && method.getReturnType() != void.class) {
                    String propertyName = null;
                    if (methodName.startsWith("get") && methodName.length() > 3) {
                        propertyName = methodName.substring(3);
                    } else if (methodName.startsWith("is") && methodName.length() > 2) {
                        propertyName = methodName.substring(2);
                    }
                    if (propertyName != null) {
                        // 首字母小写
                        propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
                        try {
                            Object value = method.invoke(obj);
                            if (value != null) {
                                claims.put(propertyName, value);
                            }
                        } catch (IllegalAccessException | java.lang.reflect.InvocationTargetException e) {
                            // 忽略无法调用的 getter（理论上 public 方法不会失败）
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return claims;
    }

}
