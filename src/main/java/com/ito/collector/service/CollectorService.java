package com.ito.collector.service;

import com.ito.collector.adapter.CsvAdapter;
import com.ito.collector.adapter.ItsmDbAdapter;
import com.ito.collector.adapter.MediaWikiAdapter;
import com.ito.collector.domain.ItsmData;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectorService {

    private final CsvAdapter csvAdapter;
    private final ItsmDbAdapter dbAdapter;
    private final MediaWikiAdapter wikiAdapter;

    public CollectorService(CsvAdapter csvAdapter, ItsmDbAdapter dbAdapter, MediaWikiAdapter wikiAdapter) {
        this.csvAdapter = csvAdapter;
        this.dbAdapter = dbAdapter;
        this.wikiAdapter = wikiAdapter;
    }

    @Scheduled(cron = "0 0 * * * *") // 매 시간 실행
    public void collect() {
        // DB에서 데이터 가져오기 (혹은 CSV 선택 가능)
        List<ItsmData> data = dbAdapter.fetch();

        String content = buildWikiText(data);

        // MediaWiki에 업로드 (토큰, 쿠키는 실제 값으로 교체 필요)
        wikiAdapter.uploadToWiki("시스템이력", content, "토큰", "쿠키");
    }

    private String buildWikiText(List<ItsmData> dataList) {
        StringBuilder sb = new StringBuilder("== 시스템 이력 ==\n");
        for (ItsmData data : dataList) {
            sb.append("* 시스템명: ").append(data.getSystemName()).append("\n");
        }
        return sb.toString();
    }

    @PostConstruct
    public void testCsv() {
        List<String[]> lines = csvAdapter.readCsv("C:\\Users\\soyeon\\sample.csv");
        for (String[] line : lines) {
            System.out.println("시스템명: " + line[0] + ", 설명: " + line[1]);
        }
    }

}
