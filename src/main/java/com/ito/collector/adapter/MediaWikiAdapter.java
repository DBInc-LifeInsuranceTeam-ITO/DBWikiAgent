package com.ito.collector.adapter;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class MediaWikiAdapter {

    private final RestTemplate restTemplate = new RestTemplate();

    public void uploadToWiki(String pageTitle, String content, String csrfToken, String sessionCookie) {
        String apiUrl = "http://your.wiki.address/api.php";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Cookie", sessionCookie);

        String body = String.format("action=edit&title=%s&text=%s&format=json&token=%s",
                pageTitle, content, csrfToken);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("문서 업로드 실패: " + response.getBody());
        }
    }
}
