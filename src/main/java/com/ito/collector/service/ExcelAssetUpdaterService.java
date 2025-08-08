package com.ito.collector.service;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.repository.CmdbAssetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelAssetUpdaterService {

    private final CmdbAssetRepository assetRepository;
    private final WikiUploadService wikiUploadService;  // WikiUploadService 추가

    // 엑셀 파일 경로 (수정 가능)
    //private static final String EXCEL_PATH = "C:\\Users\\Administrator\\Desktop\\project\\wiki\\DBWikiAgent\\src\\main\\resources\\server_linux.xlsx";
    private static final String EXCEL_PATH = "src/main/resources/server_linux.xlsx";

    @Transactional // 트랜잭션을 명시적으로 관리
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

                // 동일한 호스트명을 가진 서버 정보 저장
                Map<String, CmdbAsset> hostnameToAssetMap = new HashMap<>();

                for (Row row : sheet) {
                    if (row.getRowNum() < 1) continue; // 첫 행(헤더) 무시

                    String hostname   = getCellValue(row, 4);
                    String ip         = getCellValue(row, 5);
                    String vip        = ""; // 추후 컬럼 확장 시 대응
                    String cpu        = getCellValue(row, 9);
                    String mem        = getCellValue(row, 10);
                    String workType   = getCellValue(row, 13).trim();  // 공백 제거
                    String osManager  = getCellValue(row, 20);
                    String mwManager  = getCellValue(row, 21);
                    String workCategory = getCellValue(row, 11);

                    // workType이 "DR"이면 해당 항목을 건너뜁니다.
                    if ("DR".equalsIgnoreCase(workType)) {  // 대소문자 구분 없이 비교
                        continue;
                    }

                    if (hostname.isBlank()) continue;

                    // 동일한 호스트명을 가진 서버가 이미 있으면 `DR`이 아닌 항목만 저장
                    if (hostnameToAssetMap.containsKey(hostname)) {
                        CmdbAsset existingAsset = hostnameToAssetMap.get(hostname);
                        // 이미 해당 호스트명이 있으면 workType이 "DR"이 아닌 최신 정보를 사용
                        if (!"DR".equalsIgnoreCase(existingAsset.getWorkType())) {
                            // 최신 데이터를 유지
                            existingAsset.setIp(ip);
                            existingAsset.setVip(vip);
                            existingAsset.setCpu(cpu);
                            existingAsset.setMem(mem);
                            existingAsset.setOsManager(osManager);
                            existingAsset.setMwManager(mwManager);
                            existingAsset.setWorkCategory(workCategory);
                        }
                    } else {
                        CmdbAsset newAsset = new CmdbAsset();
                        newAsset.setHostname(hostname);
                        newAsset.setIp(ip);
                        newAsset.setVip(vip);
                        newAsset.setCpu(cpu);
                        newAsset.setMem(mem);
                        newAsset.setWorkType(workType);
                        newAsset.setOsManager(osManager);
                        newAsset.setMwManager(mwManager);
                        newAsset.setWorkCategory(workCategory);

                        hostnameToAssetMap.put(hostname, newAsset);
                    }
                }

                // **"DR" 항목 삭제**: DR 항목을 DB에서 삭제
                for (CmdbAsset asset : hostnameToAssetMap.values()) {
                    if ("DR".equalsIgnoreCase(asset.getWorkType())) {
                        assetRepository.delete(asset);  // DB에서 삭제
                        log.info("삭제된 자산: {}", asset.getHostname());
                    }
                }

                // 저장된 자산 정보를 DB에 저장
                for (CmdbAsset asset : hostnameToAssetMap.values()) {
                    // DR이 아닌 자산만 저장
                    if (!"DR".equalsIgnoreCase(asset.getWorkType())) {
                        assetRepository.save(asset);
                        assetRepository.flush();  // 즉시 DB에 반영
                        log.info("자산 업데이트됨: {}", asset.getHostname());
                    }
                }

                log.info("엑셀 기반 자산 업데이트 완료");

                // DB에 업데이트가 완료된 후, 해당 자산들의 위키 페이지도 업데이트
                for (CmdbAsset asset : hostnameToAssetMap.values()) {
                    if (!"DR".equalsIgnoreCase(asset.getWorkType())) {
                        wikiUploadService.uploadPage(asset.getHostname());  // 위키에 페이지 업로드
                        log.info("위키 페이지 업로드됨: {}", asset.getHostname());
                    }
                }

            }

        } catch (Exception e) {
            log.error("엑셀 파일 처리 중 오류 발생", e);
        }
    }

    // 셀 값 가져오기 (null 처리 및 공백 제거)
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