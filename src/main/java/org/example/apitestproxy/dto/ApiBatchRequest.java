package org.example.apitestproxy.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ApiBatchRequest {
    private String serverIp;
    private String serverPort;
    private List<ApiCall> apiList;  // 여러 API를 순서대로 호출

    private String token;

    @Data
    public static class ApiCall {
        private String apiUrl;
        private String apiMethod;
        private Map<String, Object> apiParams;
    }
}
