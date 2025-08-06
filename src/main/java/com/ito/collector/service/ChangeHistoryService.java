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

        for (ChangeHistory history : historyList) {
            sb.append(String.format("- [%s] %s (%s)\n",
                    history.getReqNo(),
                    history.getReqTitle(),
                    history.getReqPerson()));
        }

        return sb.toString();
    }
}
