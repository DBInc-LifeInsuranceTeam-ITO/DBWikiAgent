package com.ito.collector.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class MediaWikiAdapter {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_URL = "http://10.90.40.231/wiki/api.php";

    public String fetchPageContent(String title, String sessionCookie) {
        try {
            String url = API_URL + "?action=query&prop=revisions&titles="
                    + URLEncoder.encode(title, StandardCharsets.UTF_8)
                    + "&rvslots=main&rvprop=content&format=json";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Cookie", sessionCookie);

            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode pages = root.path("query").path("pages");

            // 페이지가 여러 개일 수 있으므로 첫 번째 요소만 가져오기
            JsonNode firstPage = pages.elements().hasNext() ? pages.elements().next() : null;

            if (firstPage == null || firstPage.has("missing")) {
                // 문서가 존재하지 않으면 빈 문자열 반환
                return "";
            }

            JsonNode revisions = firstPage.path("revisions");
            if (!revisions.isArray() || revisions.isEmpty()) {
                return "";
            }

            return revisions.get(0).path("slots").path("main").path("*").asText("");

        } catch (Exception e) {
            throw new RuntimeException("문서 조회 실패", e);
        }
    }

    public String mergeAutoGenSection(String originalContent, String newAutogenBlock) {
        String pattern = "(?s)<!-- AUTOGEN:START -->.*?<!-- AUTOGEN:END -->";
        String replacement = String.format("<!-- AUTOGEN:START -->\n%s\n<!-- AUTOGEN:END -->", newAutogenBlock);

        if (originalContent.matches("(?s).*<!-- AUTOGEN:START -->.*<!-- AUTOGEN:END -->.*")) {
            return originalContent.replaceAll(pattern, replacement);
        } else {
            return replacement + "\n\n" + originalContent;
        }
    }

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
