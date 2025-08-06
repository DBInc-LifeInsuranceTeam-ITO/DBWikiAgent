package com.ito.collector.service;

import com.ito.collector.entity.ChangeHistory;
import com.ito.collector.repository.ChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;


    public ChangeHistoryService(ChangeHistoryRepository changeHistoryRepository) {
        this.changeHistoryRepository = changeHistoryRepository;
    }

    public String buildChangeHistoryBlock(String cinm) {
        List<ChangeHistory> historyList = changeHistoryRepository.findByCiNm(cinm);

        if (historyList.isEmpty()) {
            return "== 변경이력 ==\n- 등록된 변경 이력이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("== 변경이력 ==\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        // 변경 이력 테이블 헤더
        sb.append("{| class=\"wikitable\" style=\"width: 100%; font-size: 90%; border: 1px solid #ddd; border-collapse: collapse;\"\n");
        sb.append("|+ <b style=\"color: #005bac; font-size: 110%;\">🔧 변경 이력</b>\n");
        sb.append("|-\n");
        sb.append("! style=\"background-color: #e6f2ff;\" | 요청 번호\n");
        sb.append("! style=\"background-color: #e6f2ff;\" | 요청 제목\n");
        sb.append("! style=\"background-color: #e6f2ff;\" | 요청자\n");
        sb.append("|-\n");

        // 변경 이력 항목을 테이블로 추가
        for (ChangeHistory history : historyList) {
            sb.append(String.format("| %s || %s || %s || %s\n",
                    history.getReqNo(),            // 요청 번호
                    history.getReqTitle(),         // 요청 제목
                    history.getReqPerson()        // 요청자
                     // 요청 날짜
            ));
            sb.append("|-\n");
        }
        // 테이블 닫기
        sb.append("|}\n");
        return sb.toString();
    }
}
