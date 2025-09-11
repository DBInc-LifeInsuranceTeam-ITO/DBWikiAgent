package com.ito.collector.service;

import com.ito.collector.entity.ChangeHistory;
import com.ito.collector.repository.ChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * MediaWiki용 변경이력 섹션(연도별 접힘 + 카드/마커) 생성
     *
     * 출력 구조:
     * <br/>
     * == 📝 변경이력 ==
     * <!-- [CHANGE_LOG_SECTION_START] -->
     *   <!-- [CHANGE_LOG_YYYY_START] -->
     *   {| class="mw-collapsible mw-collapsed wikitable" ...  ! ... | YYYY년 변경이력
     *   |-
     *   |
     *   <!-- [CHANGE_LOG_YYYY_INSERT_HERE] -->
     *     <!-- [REQ_CARD_START] --> ... <!-- [REQ_CARD_END] -->
     *   <!-- [CHANGE_LOG_YYYY_END] -->
     *   |}
     * <!-- [CHANGE_LOG_SECTION_END] -->
     * <br/>
     */
    public String buildChangeHistoryBlock(String hostname) {
        List<ChangeHistory> list = changeHistoryRepository.findByHostnameOrderByReqDtDesc(hostname);

        if (list == null || list.isEmpty()) {
            return "<br/>\n== 📝 변경이력 ==\n* 등록된 변경 이력이 없습니다.";
        }

        // 연도별 그룹핑 (최신 연도 먼저)
        Map<Integer, List<ChangeHistory>> byYear = new TreeMap<>(Comparator.reverseOrder());
        for (ChangeHistory h : list) {
            int year = inferYear(h.getReqDt(), h.getReqDesc());
            byYear.computeIfAbsent(year, k -> new ArrayList<>()).add(h);
        }

        // 연도 내 정렬: reqDt 내림차순(없으면 뒤로)
        for (List<ChangeHistory> yearList : byYear.values()) {
            yearList.sort((a, b) -> {
                LocalDate da = a.getReqDt();
                LocalDate db = b.getReqDt();
                if (da == null && db == null) return 0;
                if (da == null) return 1;
                if (db == null) return -1;
                return db.compareTo(da);
            });
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<br/>\n");
        sb.append("== 📝 변경이력 ==\n");
        sb.append("<!-- [CHANGE_LOG_SECTION_START] -->\n");

        for (Map.Entry<Integer, List<ChangeHistory>> e : byYear.entrySet()) {
            int year = e.getKey();
            List<ChangeHistory> ys = e.getValue();

            sb.append("<!-- [CHANGE_LOG_").append(year).append("_START] -->\n");
            sb.append("{| class=\"mw-collapsible mw-collapsed wikitable\" style=\"width:100%; margin-bottom:20px;\"\n");
            sb.append("! style=\"background:#2E75B6; color:white;\" | ").append(year).append("년 변경이력\n");
            sb.append("|-\n|\n");
            sb.append("<!-- [CHANGE_LOG_").append(year).append("_INSERT_HERE] -->\n");

            for (ChangeHistory h : ys) {
                appendReqCard(sb, h);
            }

            sb.append("\n<!-- [CHANGE_LOG_").append(year).append("_END] -->\n");
            sb.append("|}\n\n");
        }

        sb.append("<!-- [CHANGE_LOG_SECTION_END] -->\n\n");
        sb.append("<br/>\n");

        return sb.toString();
    }

    /** 카드 1개를 템플릿 그대로 추가 */
    private void appendReqCard(StringBuilder sb, ChangeHistory h) {
        String reqNo    = nz(h.getReqNo());           // CSD2312...
        String reqTitle = nz(h.getReqTitle());        // 제목
        String reqDesc  = nz(h.getReqDesc());         // 본문
        String person   = resolveReqPerson(h);        // 리플렉션으로 '담당자' 시도
        // 날짜는 템플릿 상 필수는 아니지만, 필요시 title/desc에 이미 포함된 경우가 많음

        sb.append("<!-- [REQ_CARD_START] -->\n");
        sb.append("<div style=\"margin:6px 0; padding:8px; border:1px solid #ddd; border-radius:6px; background:#fafafa;\">\n");

        // 제목 + 요청번호
        sb.append("<b style=\"color:#005bac;\">")
                .append(html(reqTitle))
                .append("</b>\n");
        sb.append("<span style=\"font-size:85%; color:#888;\">(")
                .append(html(reqNo.isEmpty() ? "-" : reqNo))
                .append(")</span><br/>\n");

        // 담당자
        sb.append("<span style=\"display:inline-block; font-weight:bold; color:#555; width:80px;\">담당자</span>")
                .append("<!-- [REQPERSON_START] -->")
                .append(html(person))
                .append("<!-- [REQPERSON_END] --><br/>\n");

        // 요청설명 (pre 보존)
        sb.append("<span style=\"display:inline-block; font-weight:bold; color:#555; width:80px;\">요청설명</span>\n");
        sb.append(toPre(reqDesc)).append("<br/>\n");

        // 코멘트 라벨(내용은 자유 입력 영역)
        sb.append("<span style=\"display:inline-block; font-weight:bold; color:#999; width:80px;\">💬 코멘트</span>\n");
        sb.append("추후 필요시 담당자가 작성\n");

        sb.append("</div>\n");
        sb.append("<!-- [REQ_CARD_END] -->\n\n");
    }

    /* ----------------------
     * 헬퍼들
     * ---------------------- */

    /** 연도 추론: reqDt가 있으면 그 연도, 없으면 본문에서 대략 추정, 그래도 없으면 현재 연도 */
    private int inferYear(LocalDate reqDt, String reqDesc) {
        if (reqDt != null) return reqDt.getYear();
        // 아주 단순한 추정: 본문에 '2023', '2024' 같은 숫자가 있으면 그 연도 채택(여러 개면 첫 번째)
        if (reqDesc != null) {
            // 2000~2099 범위
            for (int i = 2000; i <= 2099; i++) {
                if (reqDesc.contains(String.valueOf(i))) return i;
            }
        }
        return Year.now().getValue();
    }

    /** 널/공백 안전 트리밍 */
    private String nz(String s) {
        return s == null ? "" : s.trim();
    }

    /** HTML 이스케이프 */
    private String html(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    /** 요청설명 pre 포맷: 개행/들여쓰기 보존 */
    private String toPre(String raw) {
        String esc = html(raw == null ? "" : raw);
        return "<pre style=\"margin:0; padding:0; white-space:pre;line-height:1.4;border:none;background:none;font-family:Pretendard;font-size:0.95rem;\">\n"
                + esc + "\n</pre>";
    }

    /**
     * 엔티티에 담당자 필드가 없을 수 있으므로,
     * 리플렉션으로 getReqPerson()을 찾아보고 없으면 "-"
     */
    private String resolveReqPerson(ChangeHistory h) {
        try {
            Method m = h.getClass().getMethod("getReqPerson");
            Object val = m.invoke(h);
            return val == null ? "-" : String.valueOf(val).trim();
        } catch (Exception ignore) {
            // 다른 이름을 쓰는 경우가 있을 수 있어 보조 시도
            try {
                Method m2 = h.getClass().getMethod("getRequester");
                Object val2 = m2.invoke(h);
                return val2 == null ? "-" : String.valueOf(val2).trim();
            } catch (Exception ignore2) {
                return "-";
            }
        }
    }

    /** 사용 가능: LocalDate → "yyyy-MM-dd" */
    @SuppressWarnings("unused")
    private String formatDate(LocalDate d) {
        return (d == null) ? "-" : d.format(DTF);
    }
}