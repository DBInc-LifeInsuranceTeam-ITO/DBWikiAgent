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
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelAssetUpdaterService {

    private final CmdbAssetRepository assetRepository;

    private static final String EXCEL_PATH =
            "C:\\DBWikiAgent\\src\\main\\resources\\server_all.xlsx";

    // 숫자/문자 혼용 셀을 사람이 보는 문자열 그대로 변환
    private final DataFormatter fmt = new DataFormatter();

    @Transactional
    public void updateAssetsFromExcel() {
        try {
            File excelFile = new File(EXCEL_PATH);
            if (!excelFile.exists()) {
                log.warn("엑셀 파일이 존재하지 않습니다: {}", EXCEL_PATH);
                return;
            }

            log.info("=== Excel import start: path={} ===", EXCEL_PATH);

            try (FileInputStream fis = new FileInputStream(excelFile);
                 Workbook workbook = new XSSFWorkbook(fis)) {

                Map<String, CmdbAsset> hostnameToAssetMap = new HashMap<>();

                // 워크북 시트 수 로깅 및 안전 범위 계산
                int sheetCount = workbook.getNumberOfSheets();
                log.info("workbook sheets = {}", sheetCount);

                // 기존 규칙(2,3,4번째 시트 = index 1~3)을 유지하되,
                // 실제 시트 개수에 맞춰 상한을 안전하게 줄임.
                int startIdx = 1;
                int endIdx   = Math.min(3, sheetCount - 1);

                int peekLimit = 12;  // 행 미리보기 로그 개수(시트 전체 합산)
                int peekSeen  = 0;

                for (int sheetIndex = startIdx; sheetIndex <= endIdx; sheetIndex++) {
                    Sheet sheet = workbook.getSheetAt(sheetIndex);
                    if (sheet == null) {
                        log.warn("sheet {} is null (skipped)", sheetIndex);
                        continue;
                    }

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

                    log.info("sheet {} name='{}' lastRow={}", sheetIndex, sheet.getSheetName(), sheet.getLastRowNum());

                    for (Row row : sheet) {
                        if (row.getRowNum() < 3) continue; // 헤더 스킵

                        // === 엑셀 컬럼 매핑 (0-based) ===
                        String hostname     = getCellValue(row, 4);
                        String ip           = getCellValue(row, 5);
                        String vip          = ""; // 추후 확장
                        String cpu          = getCellValue(row, 9);
                        String mem          = getCellValue(row, 10);
                        String workCategory = getCellValue(row, 11);
                        String workType     = getCellValue(row, 13).trim();
                        String osManager    = getCellValue(row, osManagerColIdx);
                        String mwManager    = getCellValue(row, mwManagerColIdx);
                        String envType      = getCellValue(row, 2).trim();   // ✅ 운영구분(env_type)

                        // 앞몇 줄만 미리보기
                        if (peekSeen < peekLimit) {
                            log.info("[peek] sheet={} row={} host='{}' env='{}' workType='{}'",
                                    sheetIndex, row.getRowNum(), hostname, envType, workType);
                            peekSeen++;
                        }

                        if (hostname == null || hostname.isBlank()) continue;
                        if ("DR".equalsIgnoreCase(workType)) continue; // DR 제외

                        CmdbAsset existing = hostnameToAssetMap.get(hostname);
                        if (existing != null) {
                            // 이미 DR이 아닌 항목이면 최신 값으로 갱신
                            if (!"DR".equalsIgnoreCase(existing.getWorkType())) {
                                existing.setIp(ip);
                                existing.setVip(vip);
                                existing.setCpu(cpu);
                                existing.setMem(mem);
                                existing.setOsManager(osManager);
                                existing.setMwManager(mwManager);
                                existing.setWorkCategory(workCategory);
                                if (!envType.isEmpty()) {
                                    existing.setEnvType(envType);
                                }
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
                            a.setEnvType(envType);
                            hostnameToAssetMap.put(hostname, a);
                        }
                    }
                }

                // 저장 (DR 제외): saveAll + flush
                List<CmdbAsset> toSave = new ArrayList<>();
                for (CmdbAsset asset : hostnameToAssetMap.values()) {
                    if (!"DR".equalsIgnoreCase(asset.getWorkType())) {
                        toSave.add(asset);
                    }
                }

                // 샘플 몇 건만 출력해서 envType 실입력 확인
                for (int i = 0; i < Math.min(5, toSave.size()); i++) {
                    CmdbAsset a = toSave.get(i);
                    log.info("[save-sample] host={} envType='{}' workType='{}'",
                            a.getHostname(), a.getEnvType(), a.getWorkType());
                }

                assetRepository.saveAll(toSave);
                assetRepository.flush();

                long cnt = assetRepository.count();
                log.info("=== Excel import done. imported={}, table_rows_now={} ===", toSave.size(), cnt);
            }

        } catch (Exception e) {
            log.error("엑셀 파일 처리 중 오류 발생", e);
        }
    }

    // 숫자/문자 혼용 컬럼 안전 파싱
    private String getCellValue(Row row, int colIdx) {
        try {
            Cell cell = row.getCell(colIdx);
            if (cell == null) return "";
            return fmt.formatCellValue(cell).trim();
        } catch (Exception e) {
            return "";
        }
    }
}
