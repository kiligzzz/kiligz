package com.kiligz.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    public static <T> T get(String url, HttpParams params, RespType<T> respType) {
        try {
            ReqType reqType = ReqType.FORM;
            if (!params.isEmpty()) {
                url += "?" + reqType.toBody(params);
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofHours(1))
                    .build();
            HttpResponse<T> resp = HTTP_CLIENT.send(request, respType.handler);
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
     * 发送post请求，并指定req类型以及resp处理方式
     */
    public static <T> T post(String url, HttpParams params, ReqType reqType, RespType<T> respType) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", reqType.desc)
                    .POST(reqType.toBody(params))
                    .timeout(Duration.ofHours(1))
                    .build();
            HttpResponse<T> resp = HTTP_CLIENT.send(request, respType.handler);
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
     * 请求类型
     */
    @AllArgsConstructor
    public enum ReqType {
        JSON("application/json") {
            @Override
            public HttpRequest.BodyPublisher toBody(HttpParams params) throws Exception {
                return HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(params.getParams()));
            }
        },
        FORM("application/x-www-form-urlencoded") {
            @Override
            public HttpRequest.BodyPublisher toBody(HttpParams params) {
                String paramsStr = params.getParams()
                        .entrySet()
                        .stream()
                        .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
                        .collect(Collectors.joining("&"));
                return HttpRequest.BodyPublishers.ofString(paramsStr);
            }
        },
        FILE("multipart/form-data; boundary=---boundary") {
            @Override
            public HttpRequest.BodyPublisher toBody(HttpParams params) throws Exception {
                String boundary = "---boundary";
                String filePath = params.getFilePath();
                File file = new File(filePath);
                byte[] fileBytes = Files.readAllBytes(file.toPath());

                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                DataOutputStream dataStream = new DataOutputStream(byteStream);

                // 添加文件部分
                dataStream.writeBytes("--" + boundary + "\r\n");
                dataStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n");
                dataStream.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
                dataStream.write(fileBytes);
                dataStream.writeBytes("\r\n");

                // 添加其他参数
                params.getParams().forEach((key, value) -> {
                    try {
                        dataStream.writeBytes("--" + boundary + "\r\n");
                        dataStream.writeBytes("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n");
                        dataStream.writeBytes(value + "\r\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

                // 结束边界
                dataStream.writeBytes("--" + boundary + "--\r\n");
                dataStream.flush();

                return HttpRequest.BodyPublishers.ofByteArray(byteStream.toByteArray());
            }
        };
        private static final ObjectMapper MAPPER = new ObjectMapper();

        private final String desc;

        public abstract HttpRequest.BodyPublisher toBody(HttpParams params) throws Exception;
    }

    /**
     * 返回类型
     */
    @AllArgsConstructor
    public static class RespType<T> {

        HttpResponse.BodyHandler<T> handler;

        public static final RespType<String> STRING = new RespType<>(
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        public static RespType<Path> FILE(Path path) {
            return new RespType<>(HttpResponse.BodyHandlers.ofFile(path));
        }
    }

    /**
     * 请求参数
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
