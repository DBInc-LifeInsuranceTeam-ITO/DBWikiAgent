package com.ito.collector.service;

import com.ito.collector.entity.CmdbAsset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WikiRenderService {

    private final CmdbAssetService cmdbAssetService;
    private final ChangeHistoryService changeHistoryService;

    public String renderFullContent(String hostname) {
        // 1. CMDB 자산 정보 테이블 블럭
        CmdbAsset asset = cmdbAssetService.getByHostname(hostname);
        String assetBlock = buildAssetTable(asset);

        // 2. 변경이력 블럭
        String changeBlock = changeHistoryService.buildChangeHistoryBlock(hostname);

        // 3. 전체 위키 페이지 콘텐츠 반환
        return assetBlock + "\n\n----\n\n" + changeBlock;
    }

    private String buildAssetTable(CmdbAsset asset) {
        if (asset == null) {
            return "== 서버 자산 정보 ==\n- 해당 호스트에 대한 자산 정보가 없습니다.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("== 서버 자산 정보 ==\n\n");
        sb.append("이 페이지는 CMDB에서 자동으로 갱신되는 서버 자산 정보입니다.\n\n");
        sb.append("----\n\n");

        sb.append("{| class=\"wikitable\" style=\"width: 60%; border: 1px solid #aaa;\"\n");
        sb.append("! 항목 !! 값\n");
        sb.append("|-\n| Hostname || ").append(safe(asset.getHostname())).append("\n");
        sb.append("|-\n| IP || ").append(safe(asset.getIp())).append("\n");
        sb.append("|-\n| VIP || ").append(safe(asset.getVip())).append("\n");
        sb.append("|-\n| CPU || ").append(safe(asset.getCpu())).append("\n");
        sb.append("|-\n| Memory || ").append(safe(asset.getMem())).append("\n");
        sb.append("|-\n| Work Type || ").append(safe(asset.getWorkType())).append("\n");
        sb.append("|-\n| OS 담당자 || ").append(safe(asset.getOsManager())).append("\n");
        sb.append("|-\n| MW 담당자 || ").append(safe(asset.getMwManager())).append("\n");
        sb.append("|}\n\n");

        return sb.toString();
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }
}
