package com.clmcat.qianyu.mall.log.tracker;

import com.clmcat.qianyu.mall.log.config.LogisticsConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * M5: 快递100（kuaidi100.com）实时查询实现。
 * <p>API 文档：https://api.kuaidi100.com (订阅推送/实时查询)
 * <p>签名方式：sign = MD5( param + customer + key ).toUpperCase()
 */
@Slf4j
public class Kuaidi100Tracker implements LogisticsTracker {

    private static final String QUERY_URL = "https://poll.kuaidi100.com/poll/query.do";

    private final LogisticsConfig.Kuaidi100 cfg;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public Kuaidi100Tracker(LogisticsConfig.Kuaidi100 cfg) {
        this.cfg = cfg;
    }

    @Override
    public List<TracePoint> track(String logisticsCode, String logisticsNo) {
        try {
            String param = "{\"com\":\"" + toKuaidi100Com(logisticsCode) + "\",\"num\":\"" + logisticsNo + "\"}";
            String sign = md5(param + cfg.getCustomer() + cfg.getKey()).toUpperCase();

            String form = "customer=" + cfg.getCustomer()
                    + "&param=" + java.net.URLEncoder.encode(param, StandardCharsets.UTF_8)
                    + "&sign=" + sign;

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(QUERY_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return parseKuaidi100Response(resp.body());
        } catch (Exception e) {
            log.error("快递100轨迹查询失败 code={} no={} error={}", logisticsCode, logisticsNo, e.getMessage());
            return new ArrayList<>();
        }
    }

    /** MD5 签名 */
    private String md5(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    /** 映射：项目编码→快递100 com 参数（如 SF→shunfeng, YTO→yuantong） */
    private String toKuaidi100Com(String code) {
        if (code == null) return "";
        return switch (code.toUpperCase()) {
            case "SF" -> "shunfeng";
            case "YTO" -> "yuantong";
            case "ZTO" -> "zhongtong";
            case "STO" -> "shentong";
            case "YD" -> "yunda";
            case "JD" -> "jd";
            case "EMS" -> "ems";
            case "TT" -> "tiantian";
            default -> code.toLowerCase();
        };
    }

    private List<TracePoint> parseKuaidi100Response(String json) {
        List<TracePoint> points = new ArrayList<>();
        // 快递100 返回 {"data":[{"time":"...","context":"...","ftime":"...","location":"..."}], "status":"200"}
        try {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                    "\\{\"time\":\"([^\"]*)\",\"ftime\":\"[^\"]*\",\"context\":\"([^\"]*)\"(?:,\"location\":\"([^\"]*)\")?\\}");
            java.util.regex.Matcher m = p.matcher(json);
            while (m.find()) {
                points.add(new TracePoint(m.group(1), m.group(3) != null ? m.group(3) : "", m.group(2)));
            }
        } catch (Exception e) {
            log.warn("快递100响应解析失败: {}", e.getMessage());
        }
        return points;
    }

    @Override
    public String providerName() { return "快递100(kuaidi100)"; }
}
