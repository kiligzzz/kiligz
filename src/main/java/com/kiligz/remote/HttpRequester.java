package com.kiligz.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * http请求工具类
 *
 * @author ivan.zhu
 * @since 2024/11/6
 */
public class HttpRequester {
    /**
     * 发送get请求，并指定resp处理方式
     */
    public static <T> T get(String url, Map<String, Object> params, HttpResponse.BodyHandler<T> handler) {
        try {
            ContentType contentType = ContentType.FORM;
            if (!params.isEmpty()) {
                url += "?" + contentType.toBody(params);
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofHours(1))
                    .build();
            HttpResponse<T> resp = HttpClient.newHttpClient().send(request, handler);
            if (resp.statusCode() == 200) {
                return resp.body();
            } else {
                throw new RuntimeException("status code error: " + resp.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("request url error ", e);
        }
    }

    /**
     * 发送post请求，并指定content类型以及resp处理方式
     */
    public static <T> T post(String url, Map<String, Object> params, ContentType contentType, HttpResponse.BodyHandler<T> handler) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", contentType.desc)
                    .POST(HttpRequest.BodyPublishers.ofString(contentType.toBody(params)))
                    .timeout(Duration.ofHours(1))
                    .build();
            HttpResponse<T> resp = HttpClient.newHttpClient().send(request, handler);
            if (resp.statusCode() == 200) {
                return resp.body();
            } else {
                throw new RuntimeException("status code error: " + resp.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("request url error ", e);
        }
    }

    /**
     * str类型接收
     */
    public static HttpResponse.BodyHandler<String> strRespHandler() {
        return HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
    }

    /**
     * 文件类型接收并返回文件路径
     */
    public static HttpResponse.BodyHandler<Path> fileRespHandler(Path path) {
        return HttpResponse.BodyHandlers.ofFile(path);
    }

    @AllArgsConstructor
    public enum ContentType {
        JSON("application/json") {
            @Override
            public String toBody(Map<String, Object> params) throws Exception {
                return MAPPER.writeValueAsString(params);
            }
        },
        FORM("application/x-www-form-urlencoded") {
            @Override
            public String toBody(Map<String, Object> params) {
                return params.entrySet().stream()
                        .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
                        .collect(Collectors.joining("&"));
            }
        };
        private static final ObjectMapper MAPPER = new ObjectMapper();

        private final String desc;

        public abstract String toBody(Map<String, Object> params) throws Exception;
    }
}
