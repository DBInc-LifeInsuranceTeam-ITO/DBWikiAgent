package com.ito.collector.adapter;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class MediaWikiAdapter {

    private final RestTemplate restTemplate = new RestTemplate();

    public void uploadToWiki(String pageTitle, String content, String csrfToken, String sessionCookie) {
        // MediaWiki API URL은 일반적으로 /api.php 이며, 경로 수정 가능
        String apiUrl = "http://10.90.40.231//wiki/api.php"; // 고정 주소 사용 (인증 로직과 통일)

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Cookie", sessionCookie);

        try {
            String encodedTitle = URLEncoder.encode(pageTitle, StandardCharsets.UTF_8);
            String encodedContent = URLEncoder.encode(content, StandardCharsets.UTF_8);
            String encodedToken = URLEncoder.encode(csrfToken, StandardCharsets.UTF_8);

            String body = String.format(
                    "action=edit&format=json&title=%s&text=%s&token=%s",
                    encodedTitle, encodedContent, encodedToken
            );

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            System.out.println("Wiki upload response: " + response.getStatusCode() + " / " + response.getBody());

            if (!response.getStatusCode().is2xxSuccessful() || (response.getBody() != null && response.getBody().contains("\"error\""))) {
                throw new RuntimeException("문서 업로드 실패: " + response.getBody());
            }

        } catch (Exception e) {
            throw new RuntimeException("문서 업로드 중 예외 발생", e);
        }
    }
}
