package org.example.apitestproxy.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.apitestproxy.dto.ApiBatchRequest;
import org.example.apitestproxy.dto.ApiRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/call")
public class ApiController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/one")
    public ResponseEntity<Object> one(@RequestBody ApiRequest request) {
        try {
            // 1. 최종 URL 구성
            String fullUrl = "http://" + request.getServerIp() + ":" + request.getServerPort() + "/crebee" + request.getApiUrl();

            // 2. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic Y2JlOmNiZQ==");

            if (request.getToken() != null && !request.getToken().isEmpty()) {
                headers.set("token", request.getToken());
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request.getApiParams(), headers);

            // 3. HTTP 메소드 변환
            HttpMethod method = HttpMethod.resolve(request.getApiMethod().toUpperCase());
            if (method == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("지원되지 않는 HTTP 메소드입니다.");
            }

            // 4. API 호출
            ResponseEntity<Object> response = restTemplate.exchange(
                    fullUrl,
                    method,
                    entity,
                    Object.class
            );

            // 5. 프론트에 결과 전달
            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());

        } catch (Exception e) {
            log.error("API 호출 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("API 호출 실패: " + e.getMessage());
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<Object> batch(@RequestBody ApiBatchRequest batchRequest) {
        try {
            List<Map<String, Object>> results = new ArrayList<>();

            for (ApiBatchRequest.ApiCall call : batchRequest.getApiList()) {
                String fullUrl = "http://" + batchRequest.getServerIp() + ":" + batchRequest.getServerPort() + call.getApiUrl();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                if (batchRequest.getToken() != null && !batchRequest.getToken().isEmpty()) {
                    headers.setBearerAuth(batchRequest.getToken());
                }

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(call.getApiParams(), headers);
                HttpMethod method = HttpMethod.resolve(call.getApiMethod().toUpperCase());

                try {
                    ResponseEntity<Object> response = restTemplate.exchange(
                            fullUrl,
                            method,
                            entity,
                            Object.class
                    );

                    Map<String, Object> oneResult = new HashMap<>();
                    oneResult.put("url", call.getApiUrl());
                    oneResult.put("status", response.getStatusCodeValue());
                    oneResult.put("data", response.getBody());

                    results.add(oneResult);

                } catch (Exception ex) {
                    Map<String, Object> errorResult = new HashMap<>();
                    errorResult.put("url", call.getApiUrl());
                    errorResult.put("status", "ERROR");
                    errorResult.put("error", ex.getMessage());
                    results.add(errorResult);
                }
            }

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            log.error("배치 호출 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("배치 API 호출 실패: " + e.getMessage());
        }
    }
}
