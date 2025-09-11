package com.ito.collector.service;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.entity.IssueHistory;
import com.ito.collector.repository.IssueHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class WikiRenderService {

    private final CmdbAssetService cmdbAssetService;
    private final ChangeHistoryService changeHistoryService;
    private final IssueHistoryRepository issueHistoryRepository; // ✅ 운영 이슈 조회용

    /**
     * 단일 호스트 위키 페이지 풀 렌더링
     * 순서: 자산 요약 → (구분선) 변경 이력 → 운영 이슈(연도별 접힘+카드/마커) → 참고사항
     */
    public String renderFullContent(String hostname) {
        // 1) CMDB 자산 정보
        CmdbAsset asset = cmdbAssetService.getByHostname(hostname);
        String assetBlock = buildAssetTable(asset);

        // 2) 서버 변경 이력
        String changeBlock = changeHistoryService.buildChangeHistoryBlock(hostname);

        // 3) 운영 이슈 블록 (요청 포맷)
        String opsIssueBlock = buildOpsIssuesBlock(hostname);

        // 4) 참고사항(맨 아래)
        String referenceBlock = buildReferenceNotesBlock();

        // 합치기
        return assetBlock
                + "\n\n----\n\n" + changeBlock
                + "\n\n" + opsIssueBlock
                + "\n\n" + referenceBlock;
    }

    /**
     * 자산 요약: 오른쪽 카드형 “상세 정보”(마커 포함)로 교체
     */
    private String buildAssetTable(CmdbAsset asset) {
        String hostname = html(safe(asset.getHostname()));
        String ip       = html(safe(asset.getIp()));
        String stage    = html(safe(asset.getEnvType()));     // 구분
        String workCat  = html(safe(asset.getWorkCategory())); // 업무분류
        String workSys  = html(safe(asset.getWorkType()));     // 업무시스템
        String cpu      = html(safe(asset.getCpu()));
        String mem      = html(safe(asset.getMem()));
        String osMgr    = html(safe(asset.getOsManager()));
        String mwMgr    = html(safe(asset.getMwManager()));

        return """
                <div style="display: flex; gap: 20px; align-items: flex-start;">
                
                    <!-- 📑 목차 -->
                    __TOC__
                
                    <!-- [DETAIL_INFO__START] -->
                    <div style="width:340px; border:1px solid #ccc; border-radius:8px; padding:12px; background:#f7f9fb; box-shadow:0 1px 3px rgba(0,0,0,0.1); margin-left:auto;">
                      <!-- 제목 -->
                      <div style="font-size:110%%; font-weight:bold; color:#005bac; margin-bottom:8px;">ℹ 상세 정보</div>
                      
                      <!-- [SYSTEM_INFO_START] -->
                      <!-- 1. 시스템 정보 -->
                      <div style="margin-bottom:10px; background:#eaf4ff; border:1px solid #d0e3f5; border-radius:6px; padding:10px;">
                        <div style="font-weight:bold; color:#333; margin-bottom:4px;">📝 시스템 정보</div>
                        <table style="width:100%%; font-size:90%%; border-collapse:collapse; background-color:#fff;">
                          <tr><td style="width:40%%; background:#fafafa; padding:6px; font-weight:bold;">구분</td><td style="padding:6px;"><!-- [STAGE_START] -->%s<!-- [STAGE_END] --></td></tr>
                          <tr><td style="width:40%%; background:#fafafa; padding:6px; font-weight:bold;">서버명</td><td style="padding:6px;"><!-- [HOSTNAME_START] -->%s<!-- [HOSTNAME_END] --></td></tr>
                          <tr><td style="background:#fafafa; padding:6px; font-weight:bold;">IP</td><td style="padding:6px; font-family:monospace;"><code><!-- [IP_START] -->%s<!-- [IP_END] --></code></td></tr>
                          <tr><td style="background:#fafafa; padding:6px; font-weight:bold;">CPU</td><td style="padding:6px;"><!-- [CPU_START] -->%s<!-- [CPU_END] --></td></tr>
                          <tr><td style="background:#fafafa; padding:6px; font-weight:bold;">Memory</td><td style="padding:6px;"><!-- [MEM_START] -->%s<!-- [MEM_END] --></td></tr>
                        </table>
                      </div>
                      <!-- [SYSTEM_INFO_END] -->
                      
                      <!-- [BIZ_INFO_START] -->
                      <!-- 2. 업무 영역 -->
                      <div style="margin-bottom:10px; background:#fff9e6; border:1px solid #ddd; border-radius:6px; padding:10px;">
                        <div style="font-weight:bold; color:#333; margin-bottom:4px;">📂 업무 영역</div>
                        <table style="width:100%%; font-size:90%%; border-collapse:collapse; background-color:#fff;">
                          <tr><td style="width:40%%; background:#fafafa; padding:6px; font-weight:bold;">업무분류</td><td style="padding:6px;"><!-- [WORKCATEGORY_START] -->%s<!-- [WORKCATEGORY_END] --></td></tr>
                          <tr><td style="background:#fafafa; padding:6px; font-weight:bold;">업무시스템</td><td style="padding:6px;"><!-- [WORKSYSTEM_START] -->%s<!-- [WORKSYSTEM_END] --></td></tr>
                        </table>
                      </div>
                      <!-- [BIZ_INFO_END] -->
                      
                      <!-- [MANAGER_INFO_START] -->
                      <!-- 3. 담당자 -->
                      <div style="background:#eafcf0; border:1px solid #ddd; border-radius:6px; padding:10px;">
                        <div style="font-weight:bold; color:#333; margin-bottom:4px;">👩‍💻 담당자</div>
                        <table style="width:100%%; font-size:90%%; border-collapse:collapse; background-color:#fff;">
                          <tr><td style="width:40%%; background:#fafafa; padding:6px; font-weight:bold;">OS 담당자</td><td style="padding:6px;"><!-- [OSMANAGER_START] -->%s<!-- [OSMANAGER_END] --></td></tr>
                          <tr><td style="background:#fafafa; padding:6px; font-weight:bold;">MW 담당자</td><td style="padding:6px;"><!-- [MWMANAGER_START] -->%s<!-- [MWMANAGER_END] --></td></tr>
                        </table>
                      </div>
                      <!-- [MANAGER_INFO_END] -->
                    </div>
                    <!-- [DETAIL_INFO_END] -->
                </div>
                
                == <span id="개요">📘 개요</span> ==
                <div style="margin: 0.5em 0 1.5em 0; font-size: 100%%;">
                * <b style="color: #005bac;">%s</b> 서버는 <b style="color: #1a4d1a;">%s</b> 업무를 수행하는 시스템입니다.<br/>          
                </div>
                """.formatted(
                stage, hostname, ip, cpu, mem,
                workCat, workSys,
                osMgr, mwMgr,
                hostname, workSys
        );
    }

    /**
     * 참고사항 (기존 유지)
     */
    private String buildReferenceNotesBlock() {
        return """
                == <span id="기타 참고사항">📎 참고사항</span> ==
                * 위 정보는 최신 DB 기준 자동 생성된 내용입니다.  
                * 변경사항 발생 시 데이터센터 담당자에게 문의 바랍니다. 📬
                """;
    }

    /**
     * 운영 이슈 블록 (연도별 접힘 + 카드/마커)
     */
    private String buildOpsIssuesBlock(String currentHostname) {
        String h = normalizeHost(currentHostname);
        if (!StringUtils.hasText(h)) {
            return "== 🚧 운영 이슈 ==\n(서버명이 비어 있어 운영 이슈를 표시할 수 없습니다.)";
        }

        // 해당 호스트 포함 이슈 필터링
        List<IssueHistory> matched = new ArrayList<>();
        for (IssueHistory ih : issueHistoryRepository.findAll()) {
            if (containsHost(ih.getTargetServers(), h)) {
                matched.add(ih);
            }
        }
        if (matched.isEmpty()) {
            return "== 🚧 운영 이슈 ==\n* 해당 서버 관련 운영 이슈가 없습니다.";
        }

        // 날짜/연도 추출 및 그룹핑
        Map<Integer, List<IssueHistory>> byYear = new TreeMap<>(Comparator.reverseOrder()); // 최신연도 먼저
        Map<IssueHistory, LocalDate> dateCache = new HashMap<>();
        for (IssueHistory ih : matched) {
            LocalDate d = inferIssueDate(ih);
            if (d != null) dateCache.put(ih, d);
            int year = (d != null) ? d.getYear() : inferIssueYearFallback(ih);
            byYear.computeIfAbsent(year, k -> new ArrayList<>()).add(ih);
        }
        // 연도 내 정렬: 날짜 desc, 없는 건 뒤로
        for (List<IssueHistory> list : byYear.values()) {
            list.sort((a, b) -> {
                LocalDate da = dateCache.get(a);
                LocalDate db = dateCache.get(b);
                if (da == null && db == null) return 0;
                if (da == null) return 1;
                if (db == null) return -1;
                return db.compareTo(da);
            });
        }

        StringBuilder sb = new StringBuilder();
        sb.append("== 🚧 운영 이슈 ==\n");
        sb.append("<!-- [ISSUE_LOG_SECTION_START] -->\n");

        for (Map.Entry<Integer, List<IssueHistory>> entry : byYear.entrySet()) {
            int year = entry.getKey();
            List<IssueHistory> list = entry.getValue();

            sb.append("<!-- [ISSUE_LOG_").append(year).append("_START] -->\n");
            sb.append("{| class=\"mw-collapsible mw-collapsed wikitable\" style=\"width:100%; margin-bottom:20px;\"\n");
            sb.append("! style=\"background:#E67E22; color:white;\" | ").append(year).append("년 운영이슈\n");
            sb.append("|-\n");
            sb.append("|\n");
            sb.append("<!-- [ISSUE_LOG_").append(year).append("_INSERT_HERE] -->\n");

            for (IssueHistory ih : list) {
                appendIssueCard(sb, ih);
            }

            sb.append("\n<!-- [ISSUE_LOG_").append(year).append("_END] -->\n");
            sb.append("|}\n");
        }

        sb.append("<!-- [ISSUE_LOG_SECTION_END] -->\n\n");
        sb.append("<br/>\n");
        return sb.toString();
    }

    /**
     * 카드 한 개를 요청한 마커/스타일로 추가
     */
    private void appendIssueCard(StringBuilder sb, IssueHistory ih) {
        String issueType   = html(nz(ih.getType()));
        String issuePart   = html(nz(ih.getWorkPart()));
        String issueTitle  = html(nz(ih.getTitle()));
        String issueCsdNo  = html(nz(ih.getItsmCsdNo()));
        String issueStatus = html(nz(ih.getStatus()));
        String issueOwner  = html(nz(ih.getIssueOwner()));

        String statusColor = statusBadgeColor(nz(ih.getStatus())); // 상태에 따른 배경색
        String preSummary  = toPreSummary(nz(ih.getContent()));    // <pre>로 감싸 개행 보존

        sb.append("<!-- [ISSUE_CARD_START] -->\n");
        sb.append("<div style=\"border-left:3px solid #005bac; margin:12px 0; padding-left:12px;\">\n");
        sb.append("<div style=\"display:flex; justify-content:space-between; align-items:center; padding:4px 0; width:100%; background:none;\">\n");
        sb.append("<span style=\"white-space:nowrap;\">\n");
        sb.append("<span style=\"background:#dc3545; color:white; padding:2px 6px; border-radius:4px; font-size:85%%;\"><!-- [ISSUETYPE_START] -->")
                .append(issueType).append("<!-- [ISSUETYPE_END] --></span>\n");
        sb.append("<span style=\"background:#005bac; color:white; padding:2px 6px; border-radius:4px; font-size:85%%;\"><!-- [ISSUEPART_START] -->")
                .append(issuePart).append("<!-- [ISSUEPART_END] --></span>\n");
        sb.append("<b style=\"color:#005bac; font-size:105%%;\"><!-- [ISSUETITLE_START] -->")
                .append(issueTitle).append("<!-- [ISSUETITLE_END] --></b>\n");
        sb.append("<span style=\"color:#888; font-size:85%%;\">(<!-- [ISSUECSDNO_START] -->")
                .append(issueCsdNo.isEmpty() ? "-" : issueCsdNo)
                .append("<!-- [ISSUECSDNO_END] -->)</span>\n");
        sb.append("</span>\n");

        sb.append("<span style=\"background:").append(statusColor)
                .append("; color:white; padding:2px 6px; border-radius:4px; font-size:85%%;\"><!-- [ISSUESTATUS_START] -->")
                .append(issueStatus.isEmpty() ? "상태미기재" : issueStatus)
                .append("<!-- [ISSUESTATUS_END] --></span>\n");
        sb.append("</div>\n\n");

        sb.append("Issue Owner: <b><!-- [ISSUEOWNER_START] -->")
                .append(issueOwner.isEmpty() ? "-" : issueOwner)
                .append("<!-- [ISSUEOWNER_END] --></b> <br/>\n\n");

        sb.append("<b>📌 이슈내용</b><br/>\n");
        sb.append("<!-- [ISSUESUMMARY_START] -->")
                .append(preSummary)
                .append("<br/><!-- [ISSUESUMMARY_END] -->\n");
        sb.append("</div>\n");
        sb.append("<!-- [ISSUE_CARD_END] -->\n\n");
    }

    /* =========================
     * 날짜/연도 추출
     * ========================= */

    // [YYYY.MM.DD], YYYY-MM-DD, YYYY/MM/DD, YYYY.MM.DD, YYYY-MM, YYYY/MM, 단독 YYYY
    private static final Pattern P_FULL = Pattern.compile("(?:\\[)?(20\\d{2})[./-](0[1-9]|1[0-2])[./-](0[1-9]|[12]\\d|3[01])(?:])?");
    private static final Pattern P_YM   = Pattern.compile("(20\\d{2})[./-](0[1-9]|1[0-2])");
    private static final Pattern P_Y    = Pattern.compile("\\b(20\\d{2})\\b");

    private LocalDate inferIssueDate(IssueHistory ih) {
        String text = nz(ih.getContent());
        Matcher m = P_FULL.matcher(text);
        if (m.find()) {
            String y = m.group(1), mo = m.group(2), d = m.group(3);
            return parseDate(y + "-" + mo + "-" + d);
        }
        Matcher m2 = P_YM.matcher(text);
        if (m2.find()) {
            String y = m2.group(1), mo = m2.group(2);
            return parseDate(y + "-" + mo + "-01");
        }
        return null;
    }

    private int inferIssueYearFallback(IssueHistory ih) {
        String text = nz(ih.getContent());
        Matcher m3 = P_Y.matcher(text);
        if (m3.find()) return Integer.parseInt(m3.group(1));
        return Year.now().getValue();
    }

    private LocalDate parseDate(String isoLike) {
        try {
            return LocalDate.parse(isoLike, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /* =========================
     * 유틸/헬퍼
     * ========================= */

    /**
     * targetServers 문자열에 currentHostname이 포함되는지 정확하게 판단
     * - 구분자: 콤마(,) 기준 + 공백 제거
     * - 대소문자 무시, 완전 일치
     */
    private boolean containsHost(String targetServers, String currentLowerHost) {
        if (!StringUtils.hasText(targetServers)) return false;
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

    /** HTML 안전 처리 */
    private String html(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * 요약을 <pre>로 감싸 개행/들여쓰기 보존 (폰트/행간/여백은 요구안 그대로)
     */
    private String toPreSummary(String raw) {
        String esc = html(raw == null ? "" : raw);
        return "<pre style=\"margin:0; padding:0; white-space:pre;line-height:1.4;border:none;background:none;font-family:Pretendard;font-size:0.95rem;\">\n"
                + esc + "\n</pre>";
    }

    /**
     * 상태 텍스트에 따른 배경색 자동 선택
     * - 완료/정상/종료/복구/해결 → #28a745
     * - 진행/처리중/조치중 → #0d6efd
     * - 지연/오류/실패/에러/장애/중단 → #dc3545
     * - 점검/대기/협의/검토/보류 → #fd7e14
     * - 그 외 기본 → #28a745
     */
    private String statusBadgeColor(String s) {
        String low = s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
        if (low.contains("완료") || low.contains("정상") || low.contains("종료") || low.contains("복구") || low.contains("해결")) return "#28a745";
        if (low.contains("진행") || low.contains("처리중") || low.contains("조치중") || low.contains("in progress")) return "#0d6efd";
        if (low.contains("지연") || low.contains("오류") || low.contains("실패") || low.contains("에러") || low.contains("장애") || low.contains("중단") || low.contains("critical") || low.contains("error") || low.contains("fail")) return "#dc3545";
        if (low.contains("점검") || low.contains("대기") || low.contains("협의") || low.contains("검토") || low.contains("보류") || low.contains("pending")) return "#fd7e14";
        return "#28a745";
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }
}