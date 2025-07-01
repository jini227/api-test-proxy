package org.example.apitestproxy.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ApiRequest {
    private String serverIp;
    private String serverPort;
    private String apiUrl;
    private String apiMethod;
    private Map<String, Object> apiParams;
    private String token;
    private String loginId;
    private String loginPw;
}
