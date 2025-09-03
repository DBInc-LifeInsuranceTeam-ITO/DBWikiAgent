package com.ito.collector.service;

import com.ito.collector.entity.ChangeHistory;
import com.ito.collector.repository.ChangeHistoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;

    // ✅ 헤더 스타일 공통 상수
    private static final String TH_BASE_STYLE = "background-color:#2E75B6; color:white; padding:6px;";

    public ChangeHistoryService(ChangeHistoryRepository changeHistoryRepository) {
        this.changeHistoryRepository = changeHistoryRepository;
    }

    /**
     * CI 이름을 기준으로 변경 이력 위키 블록 생성 (셀 크기 + 헤더 스타일 적용)
     */
    public String buildChangeHistoryBlock(String cinm) {
        List<ChangeHistory> historyList = changeHistoryRepository.findByCiNm(cinm);

        if (historyList.isEmpty()) {
            return "== 📝 변경이력 ==\n* 등록된 변경 이력이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("== 📝 변경이력 ==\n");

        // 테이블 헤더 (스타일 상수 적용)
        sb.append("{| class=\"wikitable\" style=\"width:100%; font-size:85%;\"\n");
        sb.append("|-\n");
        sb.append("! style=\"" + TH_BASE_STYLE + " width:10%;\" | 요청 번호\n");
        sb.append("! style=\"" + TH_BASE_STYLE + " width:30%;\" | 요청 제목\n");
        sb.append("! style=\"" + TH_BASE_STYLE + " width:51%;\" | 요청 내용\n");
        sb.append("! style=\"" + TH_BASE_STYLE + " width:9%;\"  | 요청 날짜\n");

        // 데이터 행
        for (ChangeHistory history : historyList) {
            String reqNo    = Optional.ofNullable(history.getReqNo()).orElse("-");
            String reqTitle = Optional.ofNullable(history.getReqTitle()).orElse("-");
            String reqDesc  = Optional.ofNullable(history.getReqDesc()).orElse("-");
            String reqDt    = Optional.ofNullable(history.getReqDt()).orElse("-");

            reqDesc = escapeForWiki(reqDesc);

            sb.append("|-\n");
            sb.append(String.format(
                    "| style=\"width:10%%;\" | %s || style=\"width:30%%;\" | %s || style=\"width:51%%; font-size:95%%;\" | %s || style=\"width:9%%;\" | %s\n",
                    reqNo, reqTitle, reqDesc, reqDt
            ));
        }

        sb.append("|}\n");
        return sb.toString();
    }

    /**
     * MediaWiki 안전 문자열 변환
     */
    private String escapeForWiki(String s) {
        if (s == null) return "";
        return s
                .replace("|", "&#124;")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "<br/>")
                .trim();
    }
}