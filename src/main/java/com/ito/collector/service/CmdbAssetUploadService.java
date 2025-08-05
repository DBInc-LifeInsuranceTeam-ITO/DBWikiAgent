package com.ito.collector.service;

import com.ito.collector.adapter.MediaWikiAdapter;
import com.ito.collector.entity.CmdbAsset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmdbAssetUploadService {

    private final CmdbAssetService cmdbAssetService;
    private final MediaWikiAdapter mediaWikiAdapter;

    // 매일 새벽 12시 실행 (서버 로컬 시간 기준)
    @Scheduled(cron = "0 0 0 * * *")
    public void updateAllAssetsToWiki() {
        log.info("[스케줄러] CMDB 자산 전체 위키 업데이트 시작");

        try {
            List<CmdbAsset> allAssets = cmdbAssetService.getAllAssets();

            for (CmdbAsset asset : allAssets) {
                updateExistingWikiPage(asset.getHostname());
            }

            log.info("[스케줄러] CMDB 자산 전체 업데이트 완료. 총 {}건", allAssets.size());
        } catch (Exception e) {
            log.error("[스케줄러] CMDB 자산 위키 업데이트 중 오류 발생", e);
        }
    }

    public void updateExistingWikiPage(String hostname) {
        try {
            CmdbAsset asset = cmdbAssetService.getAllAssets()
                    .stream()
                    .filter(a -> a.getHostname().equals(hostname))
                    .findFirst()
                    .orElse(null);

            if (asset == null) {
                log.warn("호스트명에 해당하는 자산이 없습니다: {}", hostname);
                return;
            }

            String pageTitle = hostname;
            String autoGenContent = buildAutoGenContent(asset);

            mediaWikiAdapter.updatePageWithAutoGenSection(pageTitle, autoGenContent);

            log.info("페이지 업데이트 성공: {}", pageTitle);
        } catch (Exception e) {
            log.error("페이지 업데이트 실패: {}", hostname, e);
        }
    }

    private String buildAutoGenContent(CmdbAsset asset) {
        StringBuilder sb = new StringBuilder();

        sb.append("== 서버 자산 정보 ==\n\n");
        sb.append("이 페이지는 CMDB에서 자동으로 갱신되는 서버 자산 정보입니다.\n\n");
        sb.append("----\n\n");

        sb.append("{| class=\"wikitable\" style=\"width: 60%; border: 1px solid #aaa;\"\n");
        sb.append("! 항목 !! 값\n");
        sb.append("|-\n| Hostname || ").append(asset.getHostname()).append("\n");
        sb.append("|-\n| IP || ").append(asset.getIp()).append("\n");
        sb.append("|-\n| VIP || ").append(asset.getVip()).append("\n");
        sb.append("|-\n| CPU || ").append(asset.getCpu()).append("\n");
        sb.append("|-\n| Memory || ").append(asset.getMem()).append("\n");
        sb.append("|-\n| Work Type || ").append(asset.getWorkType()).append("\n");
        sb.append("|-\n| OS 담당자 || ").append(asset.getOsManager()).append("\n");
        sb.append("|-\n| MW 담당자 || ").append(asset.getMwManager()).append("\n");
        sb.append("|}\n\n");

        sb.append("=== 참고사항 ===\n");
        sb.append("- 이 정보는 매일 자동 갱신됩니다.\n");
        sb.append("- 담당자에게 문의하세요.\n\n");

        sb.append("----\n");

        return sb.toString();
    }
}
