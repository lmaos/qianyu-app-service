package com.clmcat.qianyu.im.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.StringJoiner;

/**
 * IM HTTP 客户端
 * 封装 Java 21 HttpClient，用于调用各 IM 厂商 REST API
 */
@Slf4j
@Component
public class ImHttpClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    /**
     * POST JSON 请求
     *
     * @param url     请求 URL
     * @param headers 请求头
     * @param body    请求体对象（自动序列化为 JSON）
     * @return 响应 JSON 字符串
     */
    public String postJson(String url, Map<String, String> headers, Object body) {
        try {
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

            if (headers != null) {
                headers.forEach(requestBuilder::header);
            }

            log.debug("IM HTTP POST {} headers={} body={}", url, headers, jsonBody);

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            log.debug("IM HTTP Response: status={} body={}", response.statusCode(), response.body());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("IM API 调用失败: HTTP " + response.statusCode() + " " + response.body());
            }

            return response.body();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("IM HTTP 请求异常: " + url, e);
        }
    }

    /**
     * POST application/x-www-form-urlencoded 请求
     *
     * @param url      请求 URL
     * @param headers  请求头
     * @param formData 表单数据
     * @return 响应 JSON 字符串
     */
    public String postForm(String url, Map<String, String> headers, Map<String, String> formData) {
        try {
            String formBody = encodeFormData(formData);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody));

            if (headers != null) {
                headers.forEach(requestBuilder::header);
            }

            log.debug("IM HTTP POST FORM {} headers={} body={}", url, headers, formBody);

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            log.debug("IM HTTP Response: status={} body={}", response.statusCode(), response.body());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("IM API 调用失败: HTTP " + response.statusCode() + " " + response.body());
            }

            return response.body();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("IM HTTP 请求异常: " + url, e);
        }
    }

    /**
     * 解析 JSON 响应
     */
    public JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败: " + json, e);
        }
    }

    /**
     * 编码 form-urlencoded 数据
     */
    private String encodeFormData(Map<String, String> formData) {
        StringJoiner sj = new StringJoiner("&");
        formData.forEach((key, value) -> {
            String encodedKey = java.net.URLEncoder.encode(key, StandardCharsets.UTF_8);
            String encodedValue = java.net.URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
            sj.add(encodedKey + "=" + encodedValue);
        });
        return sj.toString();
    }

    private static class StandardCharsets {
        private static final java.nio.charset.Charset UTF_8 = java.nio.charset.StandardCharsets.UTF_8;
    }
}
