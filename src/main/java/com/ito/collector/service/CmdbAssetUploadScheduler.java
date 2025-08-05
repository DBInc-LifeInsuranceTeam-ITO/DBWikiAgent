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

    // 매일 자정에 실행됨
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
