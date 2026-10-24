package com.wms.common.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.wms.common.exception.UtilException;

import java.nio.charset.Charset;
import java.util.Map;

public class HttpHelper {
    private static RestTemplate restTemplate;

    public static String BasePostRequest(String jsonData, String url, Map<String, Object> jsonHead) {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        for (Map.Entry para : jsonHead.entrySet()) {
            headers.set(para.getKey().toString(), para.getValue().toString());
        }
        // 使用fastjson的SerializerFeature配置
        HttpEntity<String> entity = new HttpEntity<>(jsonData, headers);
        try {
            // 通过RestTemplate发送POST请求
            ResponseEntity<String> response = getInstance("utf-8").exchange(url, HttpMethod.POST, entity, String.class);
            // 获取并返回响应体
            return response.getBody();
        } catch (Exception ex) {
            throw new UtilException("发送请求失败，" + ex.getMessage(), ex);
        }
    }

    public static RestTemplate getInstance(String charset) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName(charset)));
        return restTemplate;
    }
}

