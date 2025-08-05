package com.ito.collector;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.service.CmdbAssetService;
import com.ito.collector.service.ExcelAssetUpdaterService;
import com.ito.collector.service.WikiUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Slf4j
@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class CollectorApplication implements CommandLineRunner {

    private final ExcelAssetUpdaterService excelAssetUpdaterService;
    private final CmdbAssetService cmdbAssetService;
    private final WikiUploadService wikiUploadService;

    // 테스트를 위해 main 실행하면 바로 페이지 업데이트되게 설정해놓음. 나중에 스케쥴링만 걸리게 설정 필요 !
    public static void main(String[] args) {
        SpringApplication.run(CollectorApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("엑셀 기반 자산 업데이트 시작");
        excelAssetUpdaterService.updateAssetsFromExcel();
        log.info("엑셀 기반 자산 업데이트 완료");

        log.info("위키 페이지 업데이트 시작");

        List<CmdbAsset> assets = cmdbAssetService.getAllAssets();
        for (CmdbAsset asset : assets) {
            String hostname = asset.getHostname();
            try {
                wikiUploadService.uploadPage(hostname);
                log.info("위키 업데이트 성공: {}", hostname);
            } catch (Exception e) {
                log.error("위키 업데이트 실패: {}", hostname, e);
            }
        }

        log.info("실행 완료! 스케줄러는 매일 자정 자동 실행됩니다.");
    }
}
