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
     * 엑셀에서 hostname, osManager, mwManager, ip를 읽고 DB에 반영
     */
    public void updateManagersFromExcel(File excelFile) {
        boolean anyUpdated = false;

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() < 1) continue;

                String ip = getCellValue(row, 5);           // IP (예: D열)
                String hostname = getCellValue(row, 4);     // 호스트명 (예: E열)
                String osManager = getCellValue(row, 20);   // OS 담당 (U열)
                String mwManager = getCellValue(row, 21);   // MW 담당 (V열)
                String cpu = getCellValue(row, 9);       // G열: CPU
                String mem = getCellValue(row, 10);       // H열: Memory
                String workType = getCellValue(row, 13);

                if (hostname.isBlank() || hostname.equals("호스트명")) continue;

                CmdbAsset asset = assetRepository.findAll().stream()
                        .filter(a -> hostname.equalsIgnoreCase(a.getHostname()))
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

                    if ((asset.getIp() == null || asset.getIp().isBlank()) && !ip.isBlank()) {
                        asset.setIp(ip);
                        changed = true;
                    }

                    if ((asset.getCpu() == null || asset.getCpu().isBlank()) && !cpu.isBlank()) {
                        asset.setCpu(cpu);
                        changed = true;
                    }

                    if ((asset.getMem() == null || asset.getMem().isBlank()) && !mem.isBlank()) {
                        asset.setMem(mem);
                        changed = true;
                    }

                    if ((asset.getworkType() == null || asset.getworkType().isBlank()) && !workType.isBlank()) {
                        asset.setworkType(workType);
                        changed = true;
                    }

                    if (changed) {
                        assetRepository.save(asset);
                        anyUpdated = true;
                        System.out.println("Updated asset: " + hostname);
                    }
                } else {
                    // 새 자산 생성
                    CmdbAsset newAsset = new CmdbAsset();
                    newAsset.setHostname(hostname);
                    newAsset.setIp(ip);
                    newAsset.setOsManager(osManager);
                    newAsset.setMwManager(mwManager);

                    assetRepository.save(newAsset);
                    anyUpdated = true;
                    System.out.println("Inserted new asset: " + hostname);
                }
            }

            if (anyUpdated) {
                collect(); // DB 반영 후 위키 업로드
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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