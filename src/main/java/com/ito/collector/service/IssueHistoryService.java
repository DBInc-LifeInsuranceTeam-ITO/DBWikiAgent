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
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueHistoryService {

    private final IssueHistoryRepository issueHistoryRepository;

    private static final String EXCEL_PATH = "C:\\Users\\Administrator\\Desktop\\project\\wiki\\DBWikiAgent\\src\\main\\resources\\server_issue.xlsx";
    private static final String SHEET_NAME = "2025";

    // 엑셀 헤더(한글) → 내부 키(DB 필드명) 매핑
    private static final Map<String, String> HEADER_MAP = Map.of(
            "이슈유형", "issueType",
            "제목", "title",
            "내용", "content",
            "상태", "status",
            "Issue Owner", "issueOwner",
            "업무파트", "workPart",
            "대상서버명", "targetServers",
            "ITSM CSD 번호", "itsmCsdNo"
    );

    @Transactional
    public int uploadIssueHistoryFromExcel() {
        log.info("▶ IssueHistory 업로드 시작 - 파일: {}, 시트: {}", EXCEL_PATH, SHEET_NAME);

        File excelFile = new File(EXCEL_PATH);
        if (!excelFile.exists()) {
            log.error("[ISSUE-UPLOAD] 파일이 존재하지 않습니다: {}", EXCEL_PATH);
            return 0;
        }

        int inserted = 0;

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                log.error("[ISSUE-UPLOAD] 시트를 찾을 수 없습니다: {}", SHEET_NAME);
                return 0;
            }

            // 1) 헤더 행 자동 탐지 + 컬럼 인덱스 맵핑
            int headerRowNum = findHeaderRow(sheet, 10);
            if (headerRowNum < 0) {
                log.error("[ISSUE-UPLOAD] 헤더 행을 찾을 수 없습니다. (앞 10행 검색 실패)");
                return 0;
            }
            Map<String, Integer> colIndex = mapHeaderToIndexes(sheet.getRow(headerRowNum));
            Integer targetIdx = colIndex.get("targetServers");
            log.info("[ISSUE-UPLOAD] 헤더 행: {}, 매핑: {}, targetServersIdx={}", headerRowNum, colIndex, targetIdx);

            if (!colIndex.containsKey("targetServers")) {
                log.warn("[ISSUE-UPLOAD] 헤더에 '대상서버명'(targetServers) 컬럼이 없습니다. HEADER_MAP/엑셀 헤더 철자 확인 요망.");
            }

            // 2) 데이터 행 처리
            for (int r = headerRowNum + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String issueType     = getByKey(row, colIndex, "issueType");
                String title         = getByKey(row, colIndex, "title");
                String content       = getByKey(row, colIndex, "content");
                String status        = getByKey(row, colIndex, "status");
                String issueOwner    = getByKey(row, colIndex, "issueOwner");
                String workPart      = getByKey(row, colIndex, "workPart");
                String targetServers = getByKey(row, colIndex, "targetServers");
                String itsmCsdNo     = getByKey(row, colIndex, "itsmCsdNo");

                // 필수 검증: title 없으면 스킵
                if (isBlank(title)) {
                    log.debug("[ISSUE-UPLOAD] row={} 스킵 (title 비어있음)", r);
                    continue;
                }

                // 길이 제한 컷(스키마에 맞게 조정)
                status     = cut(status, 100);
                issueOwner = cut(issueOwner, 100);
                workPart   = cut(workPart, 100);
                itsmCsdNo  = cut(itsmCsdNo, 100);

                IssueHistory ih = new IssueHistory();
                setIssueTypeSafely(ih, issueType);
                ih.setTitle(nullToEmpty(title));
                ih.setContent(nullToEmpty(content));
                ih.setStatus(nullToEmpty(status));
                ih.setIssueOwner(nullToEmpty(issueOwner));
                ih.setWorkPart(nullToEmpty(workPart));
                ih.setTargetServers(nullToEmpty(targetServers));
                ih.setItsmCsdNo(nullToEmpty(itsmCsdNo));

                if (log.isDebugEnabled()) {
                    log.debug("[ISSUE-UPLOAD][row={}] SAVE preview: title='{}', targetServers='{}'",
                            r, ih.getTitle(), ih.getTargetServers());
                }

                issueHistoryRepository.save(ih);
                inserted++;

                // 앞 몇 행은 요약 덤프
                if (r <= headerRowNum + 5) {
                    log.debug("SAVE row={} type='{}' title='{}' status='{}' owner='{}' part='{}' itsm='{}' target='{}'",
                            r, issueType, title, status, issueOwner, workPart, itsmCsdNo, targetServers);
                }
            }

            issueHistoryRepository.flush();
            log.info("▶ IssueHistory 업로드 완료 - 총 {}건 저장", inserted);

        } catch (Exception e) {
            log.error("[ISSUE-UPLOAD] 엑셀 처리 중 오류 발생", e);
        }

        return inserted;
    }

    /** 앞 N행을 훑어, 우리가 아는 헤더명이 가장 많이 매칭되는 행을 헤더로 간주 */
    private int findHeaderRow(Sheet sheet, int scanRows) {
        int bestRow = -1;
        int bestScore = -1;

        int maxRow = Math.min(sheet.getLastRowNum(), sheet.getFirstRowNum() + scanRows);
        for (int r = sheet.getFirstRowNum(); r <= maxRow; r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            int score = 0;
            for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                String cell = getCellValue(row, c);
                if (HEADER_MAP.containsKey(cell)) score++;
            }
            if (score > bestScore) {
                bestScore = score;
                bestRow = r;
            }
        }
        return bestRow;
    }

    /** 헤더 행을 읽어 내부키(issueType/title/… ) → 컬럼 인덱스 매핑 생성 */
    private Map<String, Integer> mapHeaderToIndexes(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) return map;

        for (int c = headerRow.getFirstCellNum(); c < headerRow.getLastCellNum(); c++) {
            String headerText = getCellValue(headerRow, c);
            String key = HEADER_MAP.get(headerText); // 정확 일치
            if (key != null) {
                map.put(key, c);
            }
        }
        return map;
    }

    private String getByKey(Row row, Map<String, Integer> colIndex, String key) {
        Integer idx = colIndex.get(key);
        return (idx == null) ? "" : getCellValue(row, idx);
    }

    /** 가공된 문자열 값 */
    private String getCellValue(Row row, int colIdx) {
        try {
            Cell cell = row.getCell(colIdx);
            if (cell == null) return "";
            return switch (cell.getCellType()) {
                case STRING  -> cell.getStringCellValue().trim();
                case NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        yield cell.getLocalDateTimeCellValue().toString();
                    }
                    double d = cell.getNumericCellValue();
                    yield (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
                }
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                case FORMULA -> {
                    try {
                        yield cell.getStringCellValue().trim();
                    } catch (Exception ignore) {
                        try {
                            double d = cell.getNumericCellValue();
                            yield (d == Math.floor(d)) ? String.valueOf((long) d) : String.valueOf(d);
                        } catch (Exception ignore2) {
                            yield "";
                        }
                    }
                }
                default -> "";
            };
        } catch (Exception e) {
            log.warn("셀 읽기 오류: row={}, col={}, msg={}", row.getRowNum(), colIdx, e.getMessage());
            return "";
        }
    }

    /** 원본(가공 전) 값 확인용 */
    private static String nullSafeRaw(Cell cell) {
        if (cell == null) return "";
        try {
            return switch (cell.getCellType()) {
                case STRING  -> cell.getRichStringCellValue().getString();
                case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                        ? cell.getLocalDateTimeCellValue().toString()
                        : Double.toString(cell.getNumericCellValue());
                case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
                case FORMULA -> cell.getCellFormula();
                case BLANK   -> "";
                default      -> "";
            };
        } catch (Exception e) {
            return "ERR:" + e.getMessage();
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String cut(String s, int max) {
        if (s == null) return null;
        s = s.trim();
        return s.length() <= max ? s : s.substring(0, max);
    }

    /** 엔티티에 setIssueType 또는 setType 중 있는 걸 자동 호출 */
    private void setIssueTypeSafely(IssueHistory ih, String issueType) {
        try {
            ih.getClass().getMethod("setIssueType", String.class).invoke(ih, issueType);
        } catch (NoSuchMethodException nsme) {
            try {
                ih.getClass().getMethod("setType", String.class).invoke(ih, issueType);
            } catch (Exception e) {
                log.warn("IssueHistory에 setIssueType/setType 메소드가 없어 이슈유형 저장을 건너뜁니다.");
            }
        } catch (Exception e) {
            log.warn("issueType 세팅 리플렉션 실패: {}", e.getMessage());
        }
    }
}