package com.ito.collector.service;

import com.ito.collector.adapter.MediaWikiAdapter;
import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.repository.CmdbAssetRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectorService {

    private final CmdbAssetRepository assetRepository;
    private final MediaWikiAdapter wikiAdapter;

    public CollectorService(CmdbAssetRepository assetRepository, MediaWikiAdapter wikiAdapter) {
        this.assetRepository = assetRepository;
        this.wikiAdapter = wikiAdapter;
    }

    @Scheduled(cron = "0 0 * * * *") // 매 시간 실행
    public void collect() {
        // PostgreSQL에서 cmdb_asset 데이터 가져오기
        List<CmdbAsset> data = assetRepository.findAll();

        String content = buildWikiText(data);

        // MediaWiki에 업로드
        wikiAdapter.uploadToWiki("시스템이력", content, "토큰", "쿠키");
    }

    private String buildWikiText(List<CmdbAsset> dataList) {
        StringBuilder sb = new StringBuilder("== 시스템 이력 ==\n");

        for (CmdbAsset asset : dataList) {
            sb.append("* IP: ").append(asset.getIp()).append("\n");
            sb.append("** Hostname: ").append(asset.getHostname()).append("\n");
            sb.append("** CPU: ").append(asset.getCpu()).append("\n");
            sb.append("** Memory: ").append(asset.getMem()).append("\n");
            sb.append("** Disk: ").append(asset.getDisk()).append("\n");
            sb.append("** 설명: ").append(asset.getBizType()).append("\n");
            sb.append("\n");
        }

        return sb.toString();
    }
   
}
