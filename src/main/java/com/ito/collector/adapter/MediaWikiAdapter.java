package com.ito.collector.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MediaWikiAdapter {

    private static final String API_URL = "http://10.90.40.231//wiki/api.php";
    private static final String USERNAME = "191723";
    private static final String PASSWORD = "thdus123!";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client;

    public MediaWikiAdapter() {
        this.client = HttpClient.newBuilder()
                .cookieHandler(new java.net.CookieManager())
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    private void login() throws Exception {
        String loginToken = fetchLoginToken();

        String loginData = "action=clientlogin" +
                "&username=" + URLEncoder.encode(USERNAME, StandardCharsets.UTF_8) +
                "&password=" + URLEncoder.encode(PASSWORD, StandardCharsets.UTF_8) +
                "&logintoken=" + URLEncoder.encode(loginToken, StandardCharsets.UTF_8) +
                "&loginreturnurl=http://localhost/&format=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(BodyPublishers.ofString(loginData))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(stripBom(response.body()));

        String status = root.path("clientlogin").path("status").asText();
        if (!"PASS".equals(status)) {
            log.error("로그인 실패: {}", root.toPrettyString());
            throw new RuntimeException("로그인 실패: " + root.toPrettyString());
        }
        log.info("로그인 성공");
    }

    private String fetchLoginToken() throws Exception {
        String url = API_URL + "?action=query&meta=tokens&type=login&format=json";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(stripBom(response.body()));
        return root.path("query").path("tokens").path("logintoken").asText();
    }

    private String fetchCsrfToken() throws Exception {
        String url = API_URL + "?action=query&meta=tokens&format=json";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(stripBom(response.body()));
        return root.path("query").path("tokens").path("csrftoken").asText();
    }

    public String fetchPageContent(String pageTitle) throws Exception {
        String url = API_URL + "?action=query&prop=revisions&rvprop=content&format=json&titles=" + URLEncoder.encode(pageTitle, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(stripBom(response.body()));

        JsonNode pages = root.path("query").path("pages");
        if (pages.isMissingNode() || pages.isEmpty()) return null;

        JsonNode firstPage = pages.elements().next();
        if (firstPage.has("missing")) return null;

        JsonNode revisions = firstPage.path("revisions");
        if (revisions.isMissingNode() || revisions.isEmpty()) return "";

        JsonNode revision = revisions.get(0);
        if (revision.has("slots")) {
            return revision.path("slots").path("main").path("*").asText("");
        }

        return revision.path("*").asText("");
    }

    public void updatePageWithAutoGenSection(String pageTitle, String autoGenContent) throws Exception {
        String originalContent = fetchPageContent(pageTitle);
        if (originalContent == null) originalContent = "";

        String startTag = "<!-- AUTO-GEN-START -->";
        String endTag = "<!-- AUTO-GEN-END -->";
        String autoGenSection = startTag + "\n" + autoGenContent + "\n" + endTag;

        String newContent;
        Matcher matcher = Pattern.compile(startTag + ".*?" + endTag, Pattern.DOTALL).matcher(originalContent);

        if (matcher.find()) {
            newContent = matcher.replaceFirst(autoGenSection);
        } else {
            newContent = originalContent + "\n\n" + autoGenSection;
        }

        login();

        String csrfToken = fetchCsrfToken();

        String editData = "action=edit" +
                "&title=" + URLEncoder.encode(pageTitle, StandardCharsets.UTF_8) +
                "&text=" + URLEncoder.encode(newContent, StandardCharsets.UTF_8) +
                "&token=" + URLEncoder.encode(csrfToken, StandardCharsets.UTF_8) +
                "&format=json" +
                "&summary=" + URLEncoder.encode("CMDB 자산 자동 업데이트", StandardCharsets.UTF_8);

        HttpRequest editRequest = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(BodyPublishers.ofString(editData))
                .build();

        HttpResponse<String> response = client.send(editRequest, HttpResponse.BodyHandlers.ofString());
        log.info("위키 응답: {}", stripBom(response.body()));
    }

    private String stripBom(String input) {
        if (input != null && input.startsWith("\uFEFF")) {
            return input.substring(1);
        }
        return input;
    }
}
