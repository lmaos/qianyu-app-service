package com.clmcat.qianyu.core.login;

import com.clmcat.basics.commons.lang.StringUtils;
import com.clmcat.qianyu.core.jwt.JwtRsaKit;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录 TOKEN 验证
 */
@Component
public class LoginVerifier {

    @Getter
    final LoginVerifierProperties properties;

    private final JwtRsaKit.JwtVerifier jwtVerifier;

    public LoginVerifier(LoginVerifierProperties properties) throws Exception {
        this.properties = properties;
        if (StringUtils.isNotBlank(properties.getPublicKeyPath())) {
            jwtVerifier = new JwtRsaKit.JwtVerifier(properties.getPublicKeyPath());
        } else if (StringUtils.isNotBlank(properties.getPublicKey())) {
            PublicKey publicKey = JwtRsaKit.parserPublicKey(properties.getPublicKey());
            jwtVerifier = new JwtRsaKit.JwtVerifier(publicKey);
        } else {
            jwtVerifier = null;
        }
    }

    /**
     * 从 token 中获取 Claims
     *
     * <pre>
     * 可选则的异常拦截
     * SignatureException - 签名不合法（例如被篡改）
     * ExpiredJwtException - JWT 已过期
     * MalformedJwtException - JWT 格式错误
     * UnsupportedJwtException - JWT 不受支持
     * </pre>
     */
    public Claims getClaimsFromToken(String token) {
        if  (jwtVerifier != null) {
            return jwtVerifier.parseToken(token);
        } else {
            throw new IllegalStateException("没有配置公钥，无法验证token");
        }
    }

    public <T> T getFromToken(String token, Class<T> clazz) {
        Claims claims = getClaimsFromToken(token);   // 假设你已经实现这个方法
        T instance;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("无法创建实例，请确保类 " + clazz.getName() + " 有一个无参构造器", e);
        }
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            // 筛选标准 setter：以 "set" 开头、参数个数为1、返回类型为 void
            String methodName = method.getName();
            if (methodName.startsWith("set") && method.getParameterCount() == 1
                    && method.getReturnType() == void.class) {

                // 推导属性名：setUserId -> userId
                String propertyName = methodName.substring(3);
                propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);

                // 获取 setter 的参数类型
                Class<?> paramType = method.getParameterTypes()[0];

                // 从 claims 中取值，直接利用 JJWT 的类型转换能力
                Object value = claims.get(propertyName, convertType(paramType));

                if (value != null) {
                    try {
                        method.invoke(instance, value);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException("设置属性 '" + propertyName + "' 失败", e);
                    }
                }
            }
        }
        return instance;
    }

    private static Map<Class<?>, Class<?>> convertTypes = new HashMap<>();
    static {
        convertTypes.put(int.class, Integer.class);
        convertTypes.put(long.class, Long.class);
        convertTypes.put(double.class, Double.class);
        convertTypes.put(float.class, Float.class);
        convertTypes.put(boolean.class, Boolean.class);
        convertTypes.put(char.class, Character.class);
        convertTypes.put(short.class, Short.class);
        convertTypes.put(byte.class, Byte.class);
        convertTypes.put(String.class, String.class);
    }
    private Class<?> convertType(Class<?> clazz) {
        return convertTypes.getOrDefault(clazz, clazz);
    }

}
