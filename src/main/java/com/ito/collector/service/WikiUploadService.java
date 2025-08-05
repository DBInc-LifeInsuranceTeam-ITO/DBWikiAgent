package com.ito.collector.service;

import com.ito.collector.adapter.MediaWikiAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WikiUploadService {

    private final WikiRenderService wikiRenderService;
    private final MediaWikiAdapter mediaWikiAdapter;

    public void uploadPage(String hostname) {
        try {
            String pageTitle = hostname;

            // 1. 콘텐츠 렌더링
            String content = wikiRenderService.renderFullContent(hostname);

            // 2. 위키 페이지에 업데이트
            mediaWikiAdapter.updatePageWithAutoGenSection(pageTitle, content);

            log.info("[{}] 위키 페이지 업로드 완료", pageTitle);
        } catch (Exception e) {
            log.error("[{}] 위키 페이지 업로드 실패", hostname, e);
        }
    }
}
