package com.clmcat.qianyu.mall.log.tracker;

import com.clmcat.qianyu.mall.log.config.LogisticsConfig;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * M5: 快递鸟（kdniao.com）即时查询实现。
 * <p>API 文档：https://www.kdniao.com/api-track
 * <p>签名方式：DataSign = Base64( HMAC-SHA256(APIKey, RequestData_URL_Encoded) )
 */
@Slf4j
public class KdniaoTracker implements LogisticsTracker {

    private static final String API_URL = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";
    private static final String CODE_MAP_URL = "https://api.kdniao.com/Ebusiness/EbusinessOrderHandle.aspx";

    private final LogisticsConfig.Kdniao cfg;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public KdniaoTracker(LogisticsConfig.Kdniao cfg) {
        this.cfg = cfg;
    }

    @Override
    public List<TracePoint> track(String logisticsCode, String logisticsNo) {
        try {
            String requestData = "[{\"LogisticCode\":\"" + logisticsNo + "\",\"ShipperCode\":\"" + toKdniaoCode(logisticsCode) + "\"}]";
            String dataSign = sign(cfg.getApiKey(), requestData);

            String form = "RequestData=" + java.net.URLEncoder.encode(requestData, StandardCharsets.UTF_8)
                    + "&EBusinessID=" + cfg.getBusinessId()
                    + "&RequestType=1002"
                    + "&DataSign=" + java.net.URLEncoder.encode(dataSign, StandardCharsets.UTF_8)
                    + "&DataType=2";

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            return parseKdniaoResponse(resp.body());
        } catch (Exception e) {
            log.error("快递鸟轨迹查询失败 code={} no={} error={}", logisticsCode, logisticsNo, e.getMessage());
            return new ArrayList<>();
        }
    }

    /** 快递鸟签名：Base64( HMAC-SHA256( urlEncode(APIKey), RequestData ) ) 反转 */
    private String sign(String apiKey, String requestData) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(apiKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Base64.getEncoder().encodeToString(
                mac.doFinal(requestData.getBytes(StandardCharsets.UTF_8)));
    }

    /** 简易映射：项目编码→快递鸟编码（快递鸟用大写英文如 SF/YTO/ZTO） */
    private String toKdniaoCode(String code) {
        if (code == null) return "";
        return code.toUpperCase();
    }

    @SuppressWarnings("unchecked")
    private List<TracePoint> parseKdniaoResponse(String json) {
        List<TracePoint> points = new ArrayList<>();
        // 简易 JSON 解析（避免引入 jackson 依赖到 tracker 层）
        // 快递鸟返回 {"Traces":[{"AcceptTime":"...","AcceptStation":"...","Location":"..."}], "State":"3", "Success":true}
        try {
            int tracesIdx = json.indexOf("\"Traces\"");
            if (tracesIdx < 0) return points;
            String traces = json.substring(tracesIdx);
            // 逐条提取 AcceptTime / AcceptStation / Location
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                    "\\{\"AcceptTime\":\"([^\"]*)\",\"AcceptStation\":\"([^\"]*)\"(?:,\"Location\":\"([^\"]*)\")?\\}");
            java.util.regex.Matcher m = p.matcher(traces);
            while (m.find()) {
                points.add(new TracePoint(m.group(1), m.group(3) != null ? m.group(3) : "", m.group(2)));
            }
        } catch (Exception e) {
            log.warn("快递鸟响应解析失败: {}", e.getMessage());
        }
        return points;
    }

    @Override
    public String providerName() { return "快递鸟(kdniao)"; }
}
