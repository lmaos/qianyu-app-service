package com.clmcat.qianyu.core.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

/**
 * 通用 JWT RSA 工具
 * 拆分：签发器(JwtSigner) + 验签器(JwtVerifier)
 * 支持：密钥文件路径 / 直接传入密钥对象
 */
public class JwtRsaKit {

    // ========================== 全局配置 ==========================
    private static final String ALGORITHM = "RSA";
    private static final SignatureAlgorithm SIGN_ALG = SignatureAlgorithm.RS256;
    private static final DefaultResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

    // ========================== 【签发器】私钥专用 ==========================
    public static class JwtSigner {
        private final PrivateKey privateKey;

        // ===================== 构造方法1：直接传入 私钥对象 =====================
        public JwtSigner(PrivateKey privateKey) {
            this.privateKey = privateKey;
        }

        // ===================== 构造方法2：传入 私钥文件路径 =====================
        // 支持：classpath:rsa/pri.pem | file:D:/pri.pem | https://xxx.com/pri.pem
        public JwtSigner(String privateKeyPath) throws Exception {
            this.privateKey = loadPrivateKey(privateKeyPath);
        }

        // ===================== 签发Token =====================
        public String generateToken(Map<String, Object> claims, long expireMillis) {
            long now = System.currentTimeMillis();
            claims.put("iat", now);
            claims.put("exp", now + expireMillis);
            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(new Date(now))
                    .setExpiration(new Date(now + expireMillis))
                    .signWith(privateKey, SIGN_ALG)
                    .compact();
        }

        // 简化：默认7天过期
        public String generateToken(Map<String, Object> claims) {
            return generateToken(claims, 7 * 24 * 3600 * 1000L);
        }
    }

    // ========================== 【验签器】公钥专用 ==========================
    public static class JwtVerifier {
        private final PublicKey publicKey;

        // ===================== 构造方法1：直接传入 公钥对象 =====================
        public JwtVerifier(PublicKey publicKey) {
            this.publicKey = publicKey;
        }

        // ===================== 构造方法2：传入 公钥文件路径 =====================
        // 支持：classpath:rsa/pub.pem | file:D:/pub.pem | https://xxx.com/pub.pem
        public JwtVerifier(String publicKeyPath) throws Exception {
            this.publicKey = loadPublicKey(publicKeyPath);
        }

        // ===================== 验签 + 解析 =====================

        /**
         * <pre>
         * 可选则的异常拦截
         * SignatureException - 签名不合法（例如被篡改）
         * ExpiredJwtException - JWT 已过期
         * MalformedJwtException - JWT 格式错误
         * UnsupportedJwtException - JWT 不受支持
         * </pre>
         */
        public Claims parseToken(String token) {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }
    }

    // ========================== 通用密钥加载（自动识别路径） ==========================
    private static PrivateKey loadPrivateKey(String path) throws Exception {
        String pem = loadPemContent(path);
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
        return factory.generatePrivate(keySpec);
    }

    private static PublicKey loadPublicKey(String path) throws Exception {
        String pem = loadPemContent(path);
        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
        return factory.generatePublic(keySpec);
    }

    public static PrivateKey parserPrivateKey(String base64) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
        return factory.generatePrivate(keySpec);
    }

    public static PublicKey parserPublicKey(String base64) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory factory = KeyFactory.getInstance(ALGORITHM);
        return factory.generatePublic(keySpec);
    }

    // 加载 PEM 内容（自动清洗格式）
    private static String loadPemContent(String path) throws Exception {
        Resource resource = RESOURCE_LOADER.getResource(path);
        try (InputStream in = resource.getInputStream()) {
            String content = StreamUtils.copyToString(in, java.nio.charset.StandardCharsets.UTF_8);
            // 清洗 PEM 格式
            return content.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");
        }
    }
}