package org.example.apitestproxy.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.apitestproxy.dto.ApiRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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
}
