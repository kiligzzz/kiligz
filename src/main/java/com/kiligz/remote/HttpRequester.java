package com.kiligz.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * http请求工具类
 *
 * @author ivan.zhu
 * @since 2024/11/6
 */
public class HttpRequester {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    /**
     * 发送get请求，并指定resp处理方式
     */
    public static <T> T get(String url, HttpParams params, HttpResponse.BodyHandler<T> handler) {
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
            HttpResponse<T> resp = HTTP_CLIENT.send(request, handler);
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
    public static <T> T post(String url, HttpParams params, ContentType contentType, HttpResponse.BodyHandler<T> handler) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", contentType.desc)
                    .POST(HttpRequest.BodyPublishers.ofString(contentType.toBody(params)))
                    .timeout(Duration.ofHours(1))
                    .build();
            HttpResponse<T> resp = HTTP_CLIENT.send(request, handler);
            if (resp.statusCode() == 200) {
                return resp.body();
            } else {
                throw new RuntimeException("status code error: " + resp.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("request url error ", e);
        }
    }

    public static HttpResponse.BodyHandler<String> strRespHandler() {
        return HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
    }

    public static HttpResponse.BodyHandler<Path> fileRespHandler(Path path) {
        return HttpResponse.BodyHandlers.ofFile(path);
    }

    @AllArgsConstructor
    public enum ContentType {
        JSON("application/json") {
            @Override
            public String toBody(HttpParams params) throws Exception {
                return MAPPER.writeValueAsString(params.getParams());
            }
        },
        FORM("application/x-www-form-urlencoded") {
            @Override
            public String toBody(HttpParams params) {
                return params.getParams()
                        .entrySet()
                        .stream()
                        .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
                        .collect(Collectors.joining("&"));
            }
        },
        FILE("multipart/form-data; boundary=---boundary") {
            @Override
            public String toBody(HttpParams params) throws Exception {
                String filePath = params.getFilePath();

                String boundary = "---boundary";
                byte[] bytes = Files.readAllBytes(Paths.get(filePath));

                StringBuilder body = new StringBuilder();
                body.append("--").append(boundary).append("\r\n");
                body.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(new File(filePath).getName()).append("\"\r\n")
                        .append("Content-Type: application/octet-stream\r\n\r\n");
                body.append(new String(bytes)).append("\r\n");
                params.getParams().forEach((k, v) -> body.append("--").append(boundary).append("\r\n")
                        .append("Content-Disposition: form-data; name=\"").append(k).append("\"\r\n\r\n")
                        .append(v).append("\r\n"));
                body.append("--").append(boundary).append("--\r\n");
                return body.toString();
            }
        };
        private static final ObjectMapper MAPPER = new ObjectMapper();

        private final String desc;

        public abstract String toBody(HttpParams params) throws Exception;
    }

    /**
     * http参数
     */
    @Getter
    public static class HttpParams {
        String filePath;
        Map<String, Object> params = new HashMap<>();

        private HttpParams() {
        }
        public static HttpParams build(String filePath) {
            return new HttpParams().add(filePath);
        }

        public static HttpParams build(String key, Object value) {
            return new HttpParams().add(key, value);
        }

        public HttpParams add(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public HttpParams add(String key, Object value) {
            params.put(key, value);
            return this;
        }

        public boolean isEmpty() {
            return params.isEmpty();
        }
    }
}
