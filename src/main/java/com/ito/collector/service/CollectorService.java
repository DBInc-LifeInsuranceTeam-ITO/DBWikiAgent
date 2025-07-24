package com.ito.collector.service;

import com.ito.collector.adapter.MediaWikiAdapter;
import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.repository.CmdbAssetRepository;

import jakarta.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Objects;

@Service
public class CollectorService {

    private final CmdbAssetRepository assetRepository;
    private final MediaWikiAdapter wikiAdapter;

    public CollectorService(CmdbAssetRepository assetRepository, MediaWikiAdapter wikiAdapter) {
        this.assetRepository = assetRepository;
        this.wikiAdapter = wikiAdapter;
    }

    @Scheduled(cron = "0 0 * * * *") // 매 시간 실행
    public void collect() {
        // PostgreSQL에서 cmdb_asset 데이터 가져오기
        List<CmdbAsset> data = assetRepository.findAll();

        String content = buildWikiText(data);

        // MediaWiki에 업로드
        wikiAdapter.uploadToWiki("시스템이력", content, "토큰", "쿠키");
    }

    private String buildWikiText(List<CmdbAsset> dataList) {
        StringBuilder sb = new StringBuilder("== 시스템 이력 ==\n");

        for (CmdbAsset asset : dataList) {
            sb.append("* IP: ").append(asset.getIp()).append("\n");
            sb.append("** Hostname: ").append(asset.getHostname()).append("\n");
            sb.append("** CPU: ").append(asset.getCpu()).append("\n");
            sb.append("** Memory: ").append(asset.getMem()).append("\n");
            sb.append("** Disk: ").append(asset.getDisk()).append("\n");
            sb.append("** 설명: ").append(asset.getBizType()).append("\n");
            sb.append("\n");
        }

        return sb.toString();
    }
    public void updateManagersFromExcel(File excelFile) {
        try (FileInputStream fis = new FileInputStream("src/main/resources/asset-update.xlsx");
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 헤더 스킵

                String hostname = row.getCell(0).getStringCellValue().trim();
                String osManager = row.getCell(1).getStringCellValue().trim();
                String mwManager = row.getCell(2).getStringCellValue().trim();

                CmdbAsset asset = assetRepository.findAll().stream()
                        .filter(a -> hostname.equals(a.getHostname()))
                        .findFirst()
                        .orElse(null);

                if (asset != null) {
                    boolean changed = false;

                    if (!Objects.equals(asset.getOsManager(), osManager)) {
                        asset.setOsManager(osManager);
                        changed = true;
                    }

                    if (!Objects.equals(asset.getMwManager(), mwManager)) {
                        asset.setMwManager(mwManager);
                        changed = true;
                    }

                    if (changed) {
                        assetRepository.save(asset);
                        System.out.println("Updated asset: " + hostname);
                    }
                } else {
                    // 신규 추가
                    CmdbAsset newAsset = new CmdbAsset();
                    newAsset.setHostname(hostname);
                    newAsset.setOsManager(osManager);
                    newAsset.setMwManager(mwManager);
                    assetRepository.save(newAsset);
                    System.out.println("Inserted new asset: " + hostname);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
}
