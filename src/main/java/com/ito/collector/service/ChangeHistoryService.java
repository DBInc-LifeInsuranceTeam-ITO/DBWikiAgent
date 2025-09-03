package com.ito.collector.service;

import com.ito.collector.entity.ChangeHistory;
import com.ito.collector.repository.ChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;

    /** 공통 스타일 */
    private static final String TABLE_STYLE   = "width:100%; font-size:85%; border:1px solid #ccc; border-collapse:collapse; text-align:center;";
    private static final String TH_BASE_STYLE = "background-color:#2E75B6; color:white; padding:6px;";
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * MediaWiki용 변경이력 테이블 블록 생성 (hostname 기준)
     * 컬럼: 요청 번호 / 요청 제목 / 요청 설명 / 요청 날짜
     */
    public String buildChangeHistoryBlock(String hostname) {
        List<ChangeHistory> historyList = changeHistoryRepository.findByHostnameOrderByReqDtDesc(hostname);

        if (historyList.isEmpty()) {
            return "== 📝 변경이력 ==\n* 등록된 변경 이력이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("== 📝 변경이력 ==\n");

        // 테이블 헤더
        sb.append("{| class=\"wikitable\" style=\"").append(TABLE_STYLE).append("\"\n");
        sb.append("|-\n");
        sb.append("! style=\"").append(TH_BASE_STYLE).append(" width:10%;\" | 요청 번호\n");
        sb.append("! style=\"").append(TH_BASE_STYLE).append(" width:30%; text-align:left;\" | 요청 제목\n");
        sb.append("! style=\"").append(TH_BASE_STYLE).append(" width:51%; text-align:left;\" | 요청 설명\n");
        sb.append("! style=\"").append(TH_BASE_STYLE).append(" width:9%;\"  | 요청 날짜\n");

        boolean odd = true;
        for (ChangeHistory h : historyList) {
            String reqNo    = Optional.ofNullable(h.getReqNo()).orElse("-");
            String reqTitle = escapeForWiki(Optional.ofNullable(h.getReqTitle()).orElse("-"));
            String reqDesc  = escapeForWiki(collapseWhitespace(Optional.ofNullable(h.getReqDesc()).orElse("-")));
            String reqDt    = formatDate(h.getReqDt());

            String rowColor = odd ? "#f9f9f9" : "#ffffff";
            sb.append("|-\n");
            sb.append(String.format(
                    "| style=\"width:10%%; background-color:%s;\" | %s " +
                    "|| style=\"width:30%%; background-color:%s; text-align:left;\" | %s " +
                    "|| style=\"width:51%%; background-color:%s; text-align:left; font-size:95%%;\" | %s " +
                    "|| style=\"width:9%%; background-color:%s;\" | %s\n",
                    rowColor, reqNo, rowColor, reqTitle, rowColor, reqDesc, rowColor, reqDt
            ));
            odd = !odd;
        }

        sb.append("|}\n");
        return sb.toString();
    }

    /** MediaWiki 안전 문자열 변환 */
    private String escapeForWiki(String s) {
        if (s == null) return "";
        return s
                .replace("|", "&#124;")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "<br/>")
                .trim();
    }

    /** 긴 공백/개행을 단일 공백으로 축약 */
    private String collapseWhitespace(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", " ");
    }

    /** LocalDate → 표시 문자열 */
    private String formatDate(LocalDate d) {
        return (d == null) ? "-" : d.format(DTF);
    }
}
