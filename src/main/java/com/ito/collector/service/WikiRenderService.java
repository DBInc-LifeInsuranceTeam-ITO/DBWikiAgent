package com.ito.collector.service;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.entity.IssueHistory;
import com.ito.collector.repository.IssueHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WikiRenderService {

    private final CmdbAssetService cmdbAssetService;
    private final ChangeHistoryService changeHistoryService;
    private final IssueHistoryRepository issueHistoryRepository; // ✅ 운영 이슈 조회용

    /**
     * 단일 호스트 위키 페이지 풀 렌더링
     */
    public String renderFullContent(String hostname) {
        // 1) CMDB 자산 정보
        CmdbAsset asset = cmdbAssetService.getByHostname(hostname);
        String assetBlock = buildAssetTable(asset);

        // 2) 서버 변경 이력
        String changeBlock = changeHistoryService.buildChangeHistoryBlock(hostname);

        // 3) (신규) 운영 이슈 블럭: "서버 변경 내역" 바로 아래에 붙임
        String opsIssueBlock = buildOpsIssuesBlock(hostname);

        // 4) 전체 병합
        //    변경 내역 아래에 운영 이슈를 두고, 마지막에 카테고리(자산 표에서 내려받은 workCategory)를 붙이고 싶다면
        //    assetBlock 안에 이미 포함된 카테고리가 있다면 중복되지 않도록 조정하세요.
        return assetBlock + "\n\n----\n\n" + changeBlock + "\n\n" + opsIssueBlock;
    }

    /**
     * 자산 요약 테이블
     */
    private String buildAssetTable(CmdbAsset asset) {
        return """
                <div style="display: flex; gap: 20px; align-items: flex-start;">
                
                    <!-- 📑 목차 -->
                    __TOC__
                
                    <div style="width: 300px; flex-shrink: 0; margin-left: auto; border: 2px solid #bbb; border-radius: 10px; padding: 12px; background-color: #f0f8ff;">
                    {| class="wikitable" style="width: 100%%; font-size: 90%%;"
                     |+ <b style="font-size: 110%%; color: #005bac;">🔧 상세 정보</b>
                     |-
                     ! style="width: 40%%; background-color: #e6f2ff;" | 항목 🏷
                     ! style="background-color: #e6f2ff;" | 내용 📋
                     |-
                     | '''🖥 서버명'''
                     | <span style="color: #2b3856;">%s</span>
                     |-
                     | '''🌐 IP'''
                     | <code>%s</code>
                     |-
                     | '''🗂️ 업무분류'''
                     | <span style="color: #444;">%s</span>
                     |-
                     | '''🏢 업무계'''
                     | <span style="color: #1a4d1a; font-weight: bold;">%s</span>
                     |-
                     | '''⚙️ CPU'''
                     | <span style="color: #444;">%s</span>
                     |-
                     | '''💾 Memory'''
                     | <span style="color: #444;">%s</span>
                     |-
                     | '''🔧 OS 담당자'''
                     | <span style="color: #444;">%s</span>
                     |-
                     | '''💻 MW 담당자'''
                     | <span style="color: #444;">%s</span>
                     |}
                    </div>
                </div>
                
                == <span id="개요">📘 개요</span> ==
                <div style="margin: 0.5em 0 1.5em 0; font-size: 100%%;">
                <b style="color: #005bac;">%s</b> 서버는 <b style="color: #1a4d1a;">%s</b> 업무를 수행하는 시스템입니다.  
                관리자는 정기적으로 상태를 점검해 주세요. 🔍
                </div>
                
                == <span id="서버 변경 내역">🖥 서버 변경 내역</span> ==
                (본문 내용이 여기에 옵니다.)
                
                == <span id="기타 참고사항">📎 참고사항</span> ==
                * 위 정보는 최신 DB 기준 자동 생성된 내용입니다.  
                * 변경사항 발생 시 데이터센터 담당자에게 문의 바랍니다. 📬
                
                %s
                """.formatted(
                safe(asset.getHostname()),
                safe(asset.getIp()),
                safe(asset.getWorkCategory()),
                safe(asset.getWorkType()),
                safe(asset.getCpu()),
                safe(asset.getMem()),
                safe(asset.getOsManager()),
                safe(asset.getMwManager()),
                safe(asset.getHostname()),
                safe(asset.getWorkType()),
                safe(asset.getWorkCategory())
        );
    }

    /**
     * (신규) 운영 이슈 블록: targetServers가 콤마(,)로 구분된 서버 목록.
     * 현재 hostname이 포함된 이슈만 표로 노출.
     */
    private String buildOpsIssuesBlock(String currentHostname) {
        String h = normalizeHost(currentHostname);
        if (!StringUtils.hasText(h)) {
            return """
                   == <span id="운영 이슈">🛠 운영 이슈</span> ==
                   (서버명이 비어 있어 운영 이슈를 표시할 수 없습니다.)
                   """;
        }

        List<IssueHistory> all = issueHistoryRepository.findAll();
        List<IssueHistory> matched = new ArrayList<>();

        for (IssueHistory ih : all) {
            if (containsHost(ih.getTargetServers(), h)) {
                matched.add(ih);
            }
        }

        if (matched.isEmpty()) {
            return """
                   == <span id="운영 이슈">🛠 운영 이슈</span> ==
                   * 해당 서버 관련 운영 이슈가 없습니다.
                   """;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("== <span id=\"운영 이슈\">🛠 운영 이슈</span> ==\n");
        sb.append("{| class=\"wikitable\" style=\"width: 100%; font-size: 90%\"\n");
        sb.append("|+ '''운영 이슈 현황 (").append(escape(currentHostname)).append(")'''\n");
        sb.append("|-\n");
        sb.append("! style=\"width:8%\" | 이슈유형\n");
        sb.append("! style=\"width:28%\" | 제목\n");
        sb.append("! style=\"width:8%\" | 상태\n");
        sb.append("! style=\"width:12%\" | Issue Owner\n");
        sb.append("! style=\"width:10%\" | 업무파트\n");
        sb.append("! style=\"width:12%\" | ITSM CSD 번호\n");
        sb.append("! | 내용(요약)\n");

        for (IssueHistory ih : matched) {
            sb.append("|-\n");
            sb.append("| ").append(escape(nz(ih.getType()))).append("\n");
            sb.append("| ").append(escape(nz(ih.getTitle()))).append("\n");
            sb.append("| ").append(escape(nz(ih.getStatus()))).append("\n");
            sb.append("| ").append(escape(nz(ih.getIssueOwner()))).append("\n");
            sb.append("| ").append(escape(nz(ih.getWorkPart()))).append("\n");
            sb.append("| ").append(escape(nz(ih.getItsmCsdNo()))).append("\n");
            sb.append("| ").append(escape(summary(nz(ih.getContent()), 400))).append("\n");
        }

        sb.append("|}\n");
        return sb.toString();
    }

    /**
     * targetServers 문자열에 currentHostname이 포함되는지 정확하게 판단
     * - 구분자: 콤마(,) 기준 + 공백 제거
     * - 대소문자 무시, 완전 일치
     * 예) "vmetopiawas1, vmetopiawas2" 에서 "vmetopiawas1" 매칭
     */
    private boolean containsHost(String targetServers, String currentLowerHost) {
        if (!StringUtils.hasText(targetServers)) return false;
        // 콤마/공백 기준 분할. (연속 콤마/끝 콤마 허용)
        String[] tokens = targetServers.split("[,\\s]+");
        for (String raw : tokens) {
            String t = normalizeHost(raw);
            if (StringUtils.hasText(t) && t.equals(currentLowerHost)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeHost(String h) {
        return h == null ? "" : h.trim().toLowerCase();
    }

    private String nz(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * 위키테이블 안전 문자열: 파이프(|) → &#124;, 줄바꿈 → <br/>
     */
    private String escape(String s) {
        if (s == null) return "";
        String x = s.replace("|", "&#124;");
        x = x.replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br/>");
        return x;
    }

    /**
     * 내용 요약(최대 n자, 잘리면 …)
     */
    private String summary(String s, int n) {
        if (s == null) return "";
        s = s.trim();
        if (s.length() <= n) return s;
        return s.substring(0, n) + "…";
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }
}