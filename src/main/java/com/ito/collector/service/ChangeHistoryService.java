package com.ito.collector.service;

import com.ito.collector.entity.ChangeHistory;
import com.ito.collector.repository.ChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeHistoryService {

    private final ChangeHistoryRepository changeHistoryRepository;

    public String buildChangeHistoryBlock(String hostname) {
        List<ChangeHistory> historyList = changeHistoryRepository.findByHostname(hostname);

        if (historyList.isEmpty()) {
            return "== 변경이력 ==\n- 등록된 변경 이력이 없습니다.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("== 변경이력 ==\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (ChangeHistory history : historyList) {
            sb.append(String.format("- [%s] %s (%s)\n",
                    history.getItsmId(),
                    history.getTaskName(),
                    history.getCreatedAt().format(formatter)));
        }

        return sb.toString();
    }
}
