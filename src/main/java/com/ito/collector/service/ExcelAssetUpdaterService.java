package com.ito.collector.service;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.repository.CmdbAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelAssetUpdaterService {

    private final CmdbAssetRepository assetRepository;

    private static final String EXCEL_PATH =
            "C:\\Users\\Administrator\\Desktop\\project\\wiki\\DBWikiAgent\\src\\main\\resources\\server_all.xlsx";

    @Transactional
    public void updateAssetsFromExcel() {
        try {
            File excelFile = new File(EXCEL_PATH);
            if (!excelFile.exists()) {
                log.warn("엑셀 파일이 존재하지 않습니다: {}", EXCEL_PATH);
                return;
            }

            try (FileInputStream fis = new FileInputStream(excelFile);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                // hostname 기준 중복 병합용 맵
                Map<String, CmdbAsset> hostnameToAssetMap = new HashMap<>();

                // 2,3,4번째 시트만 사용 (index 1~3)
                for (int sheetIndex = 1; sheetIndex <= 3; sheetIndex++) {
                    Sheet sheet = workbook.getSheetAt(sheetIndex);
                    if (sheet == null) continue;

                    // 시트별 담당자 컬럼 인덱스
                    int osManagerColIdx;
                    int mwManagerColIdx;
                    if (sheetIndex == 1) {          // 두번째 시트
                        osManagerColIdx = 21;
                        mwManagerColIdx = 22;
                    } else if (sheetIndex == 2) {   // 세번째 시트
                        osManagerColIdx = 20;
                        mwManagerColIdx = 21;
                    } else {                         // 네번째 시트
                        osManagerColIdx = 25;
                        mwManagerColIdx = 26;
                    }

                    for (Row row : sheet) {
                        if (row.getRowNum() < 1) continue; // 헤더 스킵

                        String hostname     = getCellValue(row, 4);
                        String ip           = getCellValue(row, 5);
                        String vip          = ""; // 추후 확장
                        String cpu          = getCellValue(row, 9);
                        String mem          = getCellValue(row, 10);
                        String workCategory = getCellValue(row, 11);
                        String workType     = getCellValue(row, 13).trim();
                        String osManager    = getCellValue(row, osManagerColIdx);
                        String mwManager    = getCellValue(row, mwManagerColIdx);

                        if (hostname.isBlank()) continue;
                        if ("DR".equalsIgnoreCase(workType)) continue; // DR 제외

                        if (hostnameToAssetMap.containsKey(hostname)) {
                            CmdbAsset existing = hostnameToAssetMap.get(hostname);
                            // 이미 DR이 아닌 항목이면 최신 값으로 갱신
                            if (!"DR".equalsIgnoreCase(existing.getWorkType())) {
                                existing.setIp(ip);
                                existing.setVip(vip);
                                existing.setCpu(cpu);
                                existing.setMem(mem);
                                existing.setOsManager(osManager);
                                existing.setMwManager(mwManager);
                                existing.setWorkCategory(workCategory);
                            }
                        } else {
                            CmdbAsset a = new CmdbAsset();
                            a.setHostname(hostname);
                            a.setIp(ip);
                            a.setVip(vip);
                            a.setCpu(cpu);
                            a.setMem(mem);
                            a.setWorkType(workType);
                            a.setOsManager(osManager);
                            a.setMwManager(mwManager);
                            a.setWorkCategory(workCategory);
                            hostnameToAssetMap.put(hostname, a);
                        }
                    }
                }

                // 저장 (DR 제외)
                for (CmdbAsset asset : hostnameToAssetMap.values()) {
                    if (!"DR".equalsIgnoreCase(asset.getWorkType())) {
                        assetRepository.save(asset);
                        assetRepository.flush();
                        log.info("자산 업데이트됨: {}", asset.getHostname());
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