package com.ito.collector.service;

import com.ito.collector.adapter.MediaWikiAdapter;
import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.repository.CmdbAssetRepository;

import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
        List<CmdbAsset> data = assetRepository.findAll();
        String content = buildWikiText(data);
        wikiAdapter.uploadToWiki("시스템이력", content, "토큰", "쿠키");
    }

    private String buildWikiText(List<CmdbAsset> dataList) {
        StringBuilder sb = new StringBuilder("== 시스템 이력 ==\n");

        for (CmdbAsset asset : dataList) {
            sb.append("** Hostname: ").append(asset.getHostname()).append("\n");
            sb.append("* IP: ").append(asset.getIp()).append("\n");
            sb.append("** CPU: ").append(asset.getCpu()).append("\n");
            sb.append("** Memory: ").append(asset.getMem()).append("\n");
            sb.append("** 업무명: ").append(asset.getworkType()).append("\n");
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 엑셀에서 hostname, osManager, mwManager를 읽고 DB에 반영
     */
    public void updateManagersFromExcel(File excelFile) {
        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() < 2) continue; // 제목 및 헤더 스킵

                String hostname = getCellValue(row, 4);      // 호스트명
                String osManager = getCellValue(row, 20);    // OS 담당
                String mwManager = getCellValue(row, 21);    // MW 담당

                if (hostname.isBlank()) continue;

                CmdbAsset asset = assetRepository.findAll().stream()
                        .filter(a -> hostname.equals(a.getHostname()))
                        .findFirst()
                        .orElse(null);

                if (asset != null) {
                    boolean changed = false;

                    if (!osManager.isBlank() && !Objects.equals(asset.getOsManager(), osManager)) {
                        asset.setOsManager(osManager);
                        changed = true;
                    }

                    if (!mwManager.isBlank() && !Objects.equals(asset.getMwManager(), mwManager)) {
                        asset.setMwManager(mwManager);
                        changed = true;
                    }

                    if (changed) {
                        assetRepository.save(asset);
                        System.out.println("Updated asset: " + hostname);
                    }
                } else {
                    System.out.println("Not found in DB (skipped): " + hostname);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 셀에서 문자열 안전하게 꺼내기
     */
    private String getCellValue(Row row, int index) {
        try {
            Cell cell = row.getCell(index);
            if (cell == null) return "";
            cell.setCellType(CellType.STRING);
            return cell.getStringCellValue().trim();
        } catch (Exception e) {
            return "";
        }
    }
    @PostConstruct
    public void initExcelUpdate() {
        File excelFile = new File("C:\\Users\\Administrator\\Desktop\\project\\wiki\\DBWikiAgent\\src\\main\\resources\\server_linux.xlsx");
        updateManagersFromExcel(excelFile);
    }
}
