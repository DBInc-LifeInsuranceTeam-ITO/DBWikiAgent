package com.ito.collector.service;

import com.ito.collector.adapter.MediaWikiAdapter;
import com.ito.collector.entity.CmdbAsset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

//import java.util.List;

@Service
@RequiredArgsConstructor
public class CmdbAssetUploadService {

    private final CmdbAssetService cmdbAssetService;
    private final MediaWikiAdapter mediaWikiAdapter;

    public void updateExistingWikiPage(String hostname) {
        try {
            CmdbAsset asset = cmdbAssetService.getAllAssets()
                    .stream()
                    .filter(a -> a.getHostname().equals(hostname))
                    .findFirst()
                    .orElse(null);

            if (asset == null) {
                System.err.println("호스트명에 해당하는 자산이 없습니다: " + hostname);
                return;
            }

            String pageTitle = hostname;

            // 자동생성할 테이블 내용 (위키문법)
            String autoGenContent = buildAutoGenContent(asset);

            // MediaWikiAdapter에 부분 갱신 메서드 호출
            mediaWikiAdapter.updatePageWithAutoGenSection(pageTitle, autoGenContent);

            System.out.println("페이지 업데이트 성공: " + pageTitle);
        } catch (Exception e) {
            System.err.println("페이지 업데이트 실패: " + e.getMessage());
        }
    }

    private String buildAutoGenContent(CmdbAsset asset) {
    StringBuilder sb = new StringBuilder();

    // 1. 페이지 제목 스타일 (2단 제목)
    sb.append("== 서버 자산 정보 ==\n\n");

    // 2. 간단한 설명
    sb.append("이 페이지는 CMDB에서 자동으로 갱신되는 서버 자산 정보입니다.\n\n");

    // 3. 구분선 (---- 은 수평선)
    sb.append("----\n\n");

    // 4. 자산 상세 정보 테이블
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

    // 5. 추가 정보 섹션 (3단 제목)
    sb.append("=== 참고사항 ===\n");
    sb.append("- 이 정보는 매일 자동 갱신됩니다.\n");
    sb.append("- 담당자에게 문의하세요.\n\n");

    // 6. 구분선으로 마무리
    sb.append("----\n");

    return sb.toString();
}

}
