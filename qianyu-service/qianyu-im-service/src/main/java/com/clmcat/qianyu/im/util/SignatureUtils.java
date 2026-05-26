package com.clmcat.qianyu.im.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * IM 签名工具类
 * 提供各 IM 厂商所需的加密算法
 */
public final class SignatureUtils {

    private SignatureUtils() {}

    /**
     * SHA1 哈希（小写十六进制）
     * 用于：融云签名 Signature = SHA1(AppSecret + Nonce + Timestamp)
     * 用于：网易云信 CheckSum = SHA1(AppSecret + Nonce + CurTime)
     */
    public static String sha1Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return hexEncode(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA1 计算失败", e);
        }
    }

    /**
     * SHA256 哈希（小写十六进制）
     * 用于：环信动态 Token 签名
     */
    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return hexEncode(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA256 计算失败", e);
        }
    }

    /**
     * HMAC-SHA256（小写十六进制）
     * 用于：腾讯云 UserSig 签名
     */
    public static String hmacSha256Hex(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return hexEncode(digest);
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 计算失败", e);
        }
    }

    /**
     * Base64 URL 安全编码（无填充）
     * 用于：腾讯云 UserSig、环信动态 Token
     */
    public static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    /**
     * Base64 URL 安全编码（字符串输入）
     */
    public static String base64UrlEncode(String input) {
        return base64UrlEncode(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 十六进制编码（小写）
     */
    private static String hexEncode(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
