package com.ito.collector.service;

import com.ito.collector.entity.ChangeHistory;
import com.ito.collector.repository.ChangeHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;


    public ChangeHistoryService(ChangeHistoryRepository changeHistoryRepository) {
        this.changeHistoryRepository = changeHistoryRepository;
    }

    public String buildChangeHistoryBlock(String cinm) {
        // CI 이름을 기준으로 변경 이력 목록 가져오기
        List<ChangeHistory> historyList = changeHistoryRepository.findByCiNm(cinm);

        if (historyList.isEmpty()) {
            return "== 변경이력 ==\n- 등록된 변경 이력이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("== 변경이력 ==\n");

        // 날짜 포맷 정의
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 변경 이력 테이블 헤더
        sb.append("{| class=\"wikitable\" style=\"width: 100%; font-size: 85%; border: 1px solid #ddd; border-collapse: collapse;\"\n");
        sb.append("|-\n");
        sb.append("! style=\"background-color: #2E75B6, color:white;\" | 요청 번호\n");
        sb.append("! style=\"background-color: #2E75B6, color:white;\" | 요청 제목\n");
        sb.append("! style=\"background-color: #2E75B6, color:white;\" | 요청 내용\n");
        sb.append("! style=\"background-color: #2E75B6, color:white;\" | 요청 날짜\n");  // 요청 날짜 추가
        sb.append("|-\n");

        // 변경 이력 항목을 테이블로 추가
        for (ChangeHistory history : historyList) {
            String reqNo = Optional.ofNullable(history.getReqNo()).orElse("N/A");  // null 처리
            String reqTitle = Optional.ofNullable(history.getReqTitle()).orElse("No Title");  // null 처리
            String reqDesc = Optional.ofNullable(history.getReqDesc()).orElse("Unknown");  // null 처리
            String reqDt = Optional.ofNullable(history.getReqDt()).orElse("UnKnown");
            // 테이블 행 추가
            sb.append(String.format("| %s || %s || %s || %s\n",
                    reqNo,          // 요청 번호
                    reqTitle,       // 요청 제목
                    reqDesc,
                    reqDt
            ));
            sb.append("|-\n");
        }

        sb.append("|}\n"); // 테이블 끝

        return sb.toString();
    }
}