package com.ito.collector.service;

import com.ito.collector.entity.CmdbAsset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
@RequiredArgsConstructor
public class CmdbAssetUploadScheduler {

    private final CmdbAssetService cmdbAssetService;
    private final WikiUploadService wikiUploadService;
    private final IssueHistoryService issueHistoryService;

    // 1) 매일 23:55에 이슈 이력 엑셀 → DB 적재
    @Scheduled(cron = "0 0 0 * * *")
    public void importIssueHistory() {
        log.info("▶ IssueHistory import 시작");
        int inserted = issueHistoryService.uploadIssueHistoryFromExcel();
        log.info("▶ IssueHistory import 완료: {}건", inserted);
    }

    // 2) 매일 자정에 위키 업로드
    @Scheduled(cron = "0 0 0 * * *")
    public void uploadAllAssetsToWiki() {
        log.info("▶ CMDB 자산 전체 위키 업로드 시작");
        List<CmdbAsset> assets = cmdbAssetService.getAllAssets();

        for (CmdbAsset asset : assets) {
            String hostname = asset.getHostname();
            try {
                wikiUploadService.uploadPage(hostname);
            } catch (Exception e) {
                log.error("▶ 위키 업로드 실패: {}", hostname, e);
            }
        }

        log.info("▶ CMDB 자산 전체 위키 업로드 완료. 총 {}건", assets.size());
    }
}