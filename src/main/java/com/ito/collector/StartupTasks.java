package com.ito.collector;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.service.CmdbAssetService;
import com.ito.collector.service.ExcelAssetUpdaterService;
import com.ito.collector.service.IssueHistoryService;
import com.ito.collector.service.WikiUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupTasks {

    private final ExcelAssetUpdaterService excelAssetUpdaterService;
    private final IssueHistoryService issueHistoryService;
    private final CmdbAssetService cmdbAssetService;
    private final WikiUploadService wikiUploadService;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("▶ ApplicationReadyEvent fired - 스타트업 작업 시작");

        // 1) 엑셀 기반 자산 업데이트
        try {
            log.info("엑셀 기반 자산 업데이트 시작");
            excelAssetUpdaterService.updateAssetsFromExcel();
            log.info("엑셀 기반 자산 업데이트 완료");
        } catch (Exception e) {
            log.error("엑셀 자산 업데이트 실패", e);
        }

        // 2) IssueHistory 업로드
        try {
            log.info("IssueHistory 업로드 시작");
            int inserted = issueHistoryService.uploadIssueHistoryFromExcel();
            log.info("IssueHistory 업로드 완료: {}건", inserted);
        } catch (Exception e) {
            log.error("IssueHistory 업로드 실패", e);
        }

        // 3) 위키 페이지 업데이트
        try {
            log.info("위키 페이지 업데이트 시작");
            List<CmdbAsset> assets = cmdbAssetService.getAllAssets();
            for (CmdbAsset asset : assets) {
                try {
                    wikiUploadService.uploadPage(asset.getHostname());
                    log.info("위키 업데이트 성공: {}", asset.getHostname());
                } catch (Exception e) {
                    log.error("위키 업데이트 실패: {}", asset.getHostname(), e);
                }
            }
            log.info("위키 페이지 업데이트 완료. 총 {}건", assets.size());
        } catch (Exception e) {
            log.error("위키 전체 업데이트 실패", e);
        }

        log.info("▶ 스타트업 작업 종료");
    }
}