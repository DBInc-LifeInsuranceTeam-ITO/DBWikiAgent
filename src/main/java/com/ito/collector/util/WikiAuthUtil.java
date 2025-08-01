package com.ito.collector.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class WikiAuthUtil {

    private static final String API_URL = "http://10.90.40.231/wiki/api.php";
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String USERNAME = "192168";
    private static final String PASSWORD = "1q1q!Q!Q";

    public static class WikiSession {
        public String cookie;
        public String token;
    }

    public static WikiSession loginAndGetToken() {
        WikiSession session = new WikiSession();

        try {
            // 1단계: 로그인 토큰 요청
            ResponseEntity<String> tokenResp = restTemplate.getForEntity(
                    API_URL + "?action=query&meta=tokens&type=login&format=json",
                    String.class);

            String loginToken;
            String firstCookie = tokenResp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
            JsonNode json = objectMapper.readTree(tokenResp.getBody());
            loginToken = json.path("query").path("tokens").path("logintoken").asText();

            // 2단계: 로그인 요청
            String loginBody = String.format(
                    "action=login&format=json&lgname=%s&lgpassword=%s&lgtoken=%s",
                    URLEncoder.encode(USERNAME, StandardCharsets.UTF_8),
                    URLEncoder.encode(PASSWORD, StandardCharsets.UTF_8),
                    URLEncoder.encode(loginToken, StandardCharsets.UTF_8));

            HttpHeaders loginHeaders = new HttpHeaders();
            loginHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            loginHeaders.set("Cookie", firstCookie);

            HttpEntity<String> loginReq = new HttpEntity<>(loginBody, loginHeaders);
            ResponseEntity<String> loginResp = restTemplate.postForEntity(API_URL, loginReq, String.class);
            String loginCookie = loginResp.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

            String sessionCookie = firstCookie + "; " + loginCookie;
            session.cookie = sessionCookie;

            if (!loginResp.getBody().contains("\"result\":\"Success\"")) {
                throw new RuntimeException("로그인 실패: " + loginResp.getBody());
            }

            // 3단계: CSRF 토큰 요청
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.set("Cookie", sessionCookie);
            HttpEntity<Void> tokenReq = new HttpEntity<>(tokenHeaders);

            ResponseEntity<String> csrfResp = restTemplate.exchange(
                    API_URL + "?action=query&meta=tokens&type=csrf&format=json",
                    HttpMethod.GET,
                    tokenReq,
                    String.class
            );

            JsonNode csrfJson = objectMapper.readTree(csrfResp.getBody());
            session.token = csrfJson.path("query").path("tokens").path("csrftoken").asText();

        } catch (Exception e) {
            throw new RuntimeException("MediaWiki 로그인 또는 토큰 발급 실패", e);
        }

        return session;
    }
}
