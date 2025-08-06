package com.ito.collector.service;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.repository.CmdbAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor

public class ExcelAssetUpdaterService {

    private final CmdbAssetRepository assetRepository;

    // 엑셀 파일 경로 (수정 가능)
    private static final String EXCEL_PATH = "src/main/resources/server_linux.xlsx";

    public void updateAssetsFromExcel() {
        try {
            File excelFile = new File(EXCEL_PATH);
            if (!excelFile.exists()) {
                log.warn("엑셀 파일이 존재하지 않습니다: {}", EXCEL_PATH);
                return;
            }

            try (FileInputStream fis = new FileInputStream(excelFile);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Sheet sheet = workbook.getSheetAt(0);

                for (Row row : sheet) {
                    if (row.getRowNum() < 1) continue; // 첫 행(헤더) 무시

                    String hostname   = getCellValue(row, 4);
                    String ip         = getCellValue(row, 5);
                    String vip        = ""; // 추후 컬럼 확장 시 대응
                    String cpu        = getCellValue(row, 9);
                    String mem        = getCellValue(row, 10);
                    String workType   = getCellValue(row, 13);
                    String osManager  = getCellValue(row, 20);
                    String mwManager  = getCellValue(row, 21);

                    if (hostname.isBlank()) continue;

                    Optional<CmdbAsset> existingOpt = assetRepository.findById(hostname);
                    CmdbAsset asset = existingOpt.orElse(new CmdbAsset());
                    asset.setHostname(hostname);
                    asset.setIp(ip);
                    asset.setVip(vip);
                    asset.setCpu(cpu);
                    asset.setMem(mem);
                    asset.setWorkType(workType);
                    asset.setOsManager(osManager);
                    asset.setMwManager(mwManager);

                    assetRepository.save(asset);

                    if (existingOpt.isPresent()) {
                        log.info("자산 업데이트됨: {}", hostname);
                    } else {
                        log.info("신규 자산 추가됨: {}", hostname);
                    }
                }

                log.info("엑셀 기반 자산 업데이트 완료");

            }

        } catch (Exception e) {
            log.error("엑셀 파일 처리 중 오류 발생", e);
        }
    }

    private String getCellValue(Row row, int colIdx) {
        try {
            Cell cell = row.getCell(colIdx);
            if (cell == null) return "";
            return switch (cell.getCellType()) {
                case STRING  -> cell.getStringCellValue().trim();
                case NUMERIC -> String.valueOf(cell.getNumericCellValue()).trim();
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default      -> "";
            };
        } catch (Exception e) {
            return "";
        }
    }
}
