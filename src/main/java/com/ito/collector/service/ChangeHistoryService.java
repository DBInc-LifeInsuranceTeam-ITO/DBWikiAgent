package com.ito.collector.service;

import com.ito.collector.entity.ChangeHistory;
import com.ito.collector.repository.ChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
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
        List<ChangeHistory> historyList = changeHistoryRepository.findByCiNm(cinm);

        if (historyList.isEmpty()) {
            return "== 📝 변경이력 ==\n* 등록된 변경 이력이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("== 📝 변경이력 ==\n");

        // 운영이슈 표와 동일한 헤더 색상 (#2E75B6 예시)
        sb.append("{| class=\"wikitable\" style=\"width:100%; font-size:90%; border:1px solid #ccc; border-collapse:collapse; text-align:center;\"\n");
        sb.append("|-\n");
        sb.append("! style=\"background-color:#2E75B6; color:white; padding:6px;\" | 요청 번호\n");
        // 요청 제목은 왼쪽 정렬
        sb.append("! style=\"background-color:#2E75B6; color:white; padding:6px; text-align:left;\" | 요청 제목\n");
        sb.append("! style=\"background-color:#2E75B6; color:white; padding:6px;\" | 요청자\n");
        sb.append("|-\n");

        boolean odd = true;
        for (ChangeHistory history : historyList) {
            String reqNo = Optional.ofNullable(history.getReqNo()).orElse("N/A");
            String reqTitle = Optional.ofNullable(history.getReqTitle()).orElse("No Title");
            String reqPerson = Optional.ofNullable(history.getReqPerson()).orElse("Unknown");

            String rowColor = odd ? "#f9f9f9" : "#ffffff"; // 줄마다 색상 교차
            sb.append(String.format(
                    "| style=\"background-color:%s; padding:5px;\" | %s " +
                            "|| style=\"background-color:%s; padding:5px; text-align:left;\" | %s " +
                            "|| style=\"background-color:%s; padding:5px;\" | %s \n",
                    rowColor, reqNo, rowColor, reqTitle, rowColor, reqPerson
            ));
            sb.append("|-\n");
            odd = !odd;
        }

        sb.append("|}\n");

        return sb.toString();
    }
}
