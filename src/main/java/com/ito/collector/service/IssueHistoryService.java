package com.ito.collector.service;

import com.ito.collector.entity.IssueHistory;
import com.ito.collector.repository.IssueHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueHistoryService {

    private final IssueHistoryRepository issueHistoryRepository;

    private static final String EXCEL_PATH = "C:\\Users\\Administrator\\Desktop\\project\\wiki\\DBWikiAgent\\src\\main\\resources\\server_issue.xlsx";
    private static final String SHEET_NAME = "2025";

    @Transactional
    public int uploadIssueHistoryFromExcel() {
        int insertCount = 0;
        log.info("▶ IssueHistory 업로드 시작 - 파일: {}, 시트: {}", EXCEL_PATH, SHEET_NAME);

        File excelFile = new File(EXCEL_PATH);
        if (!excelFile.exists()) {
            log.error("[ISSUE-UPLOAD] 파일이 존재하지 않습니다: {}", EXCEL_PATH);
            return 0;
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                log.error("[ISSUE-UPLOAD] 시트를 찾을 수 없습니다: {}", SHEET_NAME);
                return 0;
            }

            for (Row row : sheet) {
                if (row.getRowNum() < 1) continue; // 헤더 무시


                String issueType    = getCellValue(row, 1);
                String title        = getCellValue(row, 2);
                String content      = getCellValue(row, 3);
                String status       = getCellValue(row, 4);
                String issueOwner   = getCellValue(row, 5);
                String workPart     = getCellValue(row, 7);
                String targetServers= getCellValue(row, 12);
                String itsmCsdNo    = getCellValue(row, 13);

                if (issueType.isBlank()) continue;

                IssueHistory issue = new IssueHistory();
                issue.setTitle(title);
                issue.setContent(content);
                issue.setStatus(status);
                issue.setIssueOwner(issueOwner);
                issue.setWorkPart(workPart);
                issue.setTargetServers(targetServers);
                issue.setItsmCsdNo(itsmCsdNo);

                issueHistoryRepository.save(issue);
                insertCount++;

                log.info("▶ IssueHistory 저장:  type='{}', title='{}', owner='{}'",
                         issueType, title, issueOwner);
            }

            issueHistoryRepository.flush();
            log.info("▶ IssueHistory 업로드 완료 - 총 {}건 저장", insertCount);

        } catch (Exception e) {
            log.error("[ISSUE-UPLOAD] 엑셀 처리 중 오류 발생", e);
        }

        return insertCount;
    }

    private String getCellValue(Row row, int colIdx) {
        try {
            Cell cell = row.getCell(colIdx);
            if (cell == null) return "";
            return switch (cell.getCellType()) {
                case STRING  -> cell.getStringCellValue().trim();
                case NUMERIC -> {
                    double d = cell.getNumericCellValue();
                    // 정수면 소수점 제거
                    if (d == (long)d) yield String.valueOf((long)d);
                    else yield String.valueOf(d);
                }
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default      -> "";
            };
        } catch (Exception e) {
            log.warn("▶ 셀 값 읽기 오류 - 행: {}, 열: {}", row.getRowNum(), colIdx, e);
            return "";
        }
    }
}