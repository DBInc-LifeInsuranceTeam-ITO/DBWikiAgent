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
import java.util.*;

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

        log.info("[ISSUE-UPLOAD] 시작 - 파일: {}, 시트: {}", EXCEL_PATH, SHEET_NAME);

        try (FileInputStream fis = new FileInputStream(new File(EXCEL_PATH));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                log.error("[ISSUE-UPLOAD] 시트 '{}'를 찾을 수 없습니다.", SHEET_NAME);
                return 0;
            }

            // 첫 줄(헤더) 제외
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String issueType     = getCellValue(row, 0);
                String title         = getCellValue(row, 1);
                String content       = getCellValue(row, 2);
                String status        = getCellValue(row, 3);
                String issueOwner    = getCellValue(row, 4);
                String workPart      = getCellValue(row, 5);
                String targetServers = getCellValue(row, 6);
                String itsmCsdNo     = getCellValue(row, 7);

                // 데이터 확인 로그
                log.debug("[ISSUE-UPLOAD] Row {} → title='{}', status='{}', servers='{}'",
                        row.getRowNum(), title, status, targetServers);

                if (title.isBlank()) {
                    log.warn("[ISSUE-UPLOAD] Row {} → 제목이 비어있어 스킵", row.getRowNum());
                    continue;
                }

                IssueHistory entity = new IssueHistory();
                entity.setTitle(title);
                entity.setContent(content);
                entity.setStatus(status);
                entity.setIssueOwner(issueOwner);
                entity.setWorkPart(workPart);
                entity.setTargetServers(targetServers);
                entity.setItsmCsdNo(itsmCsdNo);

                issueHistoryRepository.save(entity);
                insertCount++;
            }

            log.info("[ISSUE-UPLOAD] INSERT 완료: {}건", insertCount);

        } catch (Exception e) {
            log.error("[ISSUE-UPLOAD] 오류 발생", e);
        }

        return insertCount;
    }

    private String getCellValue(Row row, int colIdx) {
        try {
            Cell cell = row.getCell(colIdx);
            if (cell == null) return "";
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue().trim();
                case NUMERIC -> String.valueOf(cell.getNumericCellValue()).trim();
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                default -> "";
            };
        } catch (Exception e) {
            return "";
        }
    }
}