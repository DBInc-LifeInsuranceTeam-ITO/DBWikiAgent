package com.ito.collector.service;

import com.ito.collector.adapter.MediaWikiAdapter;
import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.repository.CmdbAssetRepository;
import com.ito.collector.util.WikiAuthUtil;
import com.ito.collector.util.WikiAuthUtil.WikiSession;
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

    // 엑셀 기반 DB 업데이트 + 위키 자동 업로드
    @PostConstruct
    public void init() {
        File excelFile = new File("C:\\Users\\Administrator\\Desktop\\project\\wiki\\DBWikiAgent\\src\\main\\resources\\server_linux.xlsx");
        updateManagersFromExcel(excelFile); // DB 업데이트

        WikiSession session = WikiAuthUtil.loginAndGetToken();
        uploadPagesFromAssets(session.token, session.cookie); // 위키 업로드
    }

    public void updateManagersFromExcel(File excelFile) {
        boolean anyUpdated = false;

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() < 1) continue;

                String ip = getCellValue(row, 5);
                String hostname = getCellValue(row, 4);
                String osManager = getCellValue(row, 20);
                String mwManager = getCellValue(row, 21);
                String cpu = getCellValue(row, 9);
                String mem = getCellValue(row, 10);
                String workType = getCellValue(row, 13);
                String workCategory = getCellValue(row, 11);

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
                    if ((asset.getWorkCategory() == null || asset.getWorkCategory().isBlank()) && !workCategory.isBlank()) {
                        asset.setWorkCategory(workCategory);
                        changed = true;
                    }

                    if (changed) {
                        assetRepository.save(asset);
                        anyUpdated = true;
                        System.out.println("Updated asset: " + hostname);
                    }
                } else {
                    CmdbAsset newAsset = new CmdbAsset();
                    newAsset.setHostname(hostname);
                    newAsset.setIp(ip);
                    newAsset.setOsManager(osManager);
                    newAsset.setMwManager(mwManager);
                    newAsset.setCpu(cpu);
                    newAsset.setMem(mem);
                    newAsset.setworkType(workType);
                    newAsset.setWorkCategory(workCategory);
                    assetRepository.save(newAsset);
                    anyUpdated = true;
                    System.out.println("Inserted new asset: " + hostname);
                }
            }

            if (anyUpdated) {
                System.out.println("DB 업데이트 완료");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCellValue(Row row, int index) {
        try {
            Cell cell = row.getCell(index);
            if (cell == null) return "";
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue().trim();
                case NUMERIC -> String.valueOf(cell.getNumericCellValue()).trim();
                default -> "";
            };
        } catch (Exception e) {
            return "";
        }
    }

    private void resetPageToAutogenBlockOnly(String pageTitle, String autogenContent, String token, String cookie) {
        String newText = """
        <!-- AUTOGEN:START -->
        %s
        <!-- AUTOGEN:END -->
        """.formatted(autogenContent);

        wikiAdapter.uploadToWiki(pageTitle, newText, token, cookie);
        System.out.println("Reset Wiki Page to AUTOGEN only: " + pageTitle);
    }

    public void uploadPagesFromAssets(String token, String cookie) {
        List<CmdbAsset> assets = assetRepository.findAll();

        for (CmdbAsset asset : assets) {
            String pageTitle = asset.getHostname();
            String autogenContent = buildServerPageContent(asset); // 테이블만 생성

            // 기존 문서 내용 가져오기
            String originalContent = wikiAdapter.fetchPageContent(pageTitle, cookie);

            // 자동 생성 영역만 교체
            String mergedContent = wikiAdapter.mergeAutoGenSection(originalContent, autogenContent);

            // 위키 업로드
            //wikiAdapter.uploadToWiki(pageTitle, mergedContent, token, cookie);

            resetPageToAutogenBlockOnly(pageTitle, autogenContent, token, cookie);
            System.out.println("Uploaded Wiki Page: " + pageTitle);
        }
    }

    private String buildServerPageContent(CmdbAsset asset) {
        return """
        <div style="display: flex; gap: 20px; align-items: flex-start;">

            <!-- 📑 목차 -->
            __TOC__

            <div style="width: 300px; flex-shrink: 0; margin-left: auto; border: 2px solid #bbb; border-radius: 10px; padding: 12px; background-color: #f0f8ff;">
            {| class="wikitable" style="width: 100%%; font-size: 90%%;"
             |+ <b style="font-size: 110%%; color: #005bac;">🔧 상세 정보</b>
             |-
             ! style="width: 40%%; background-color: #e6f2ff;" | 항목 🏷
             ! style="background-color: #e6f2ff;" | 내용 📋
             |-
             | '''🖥 서버명'''
             | <span style="color: #2b3856;">%s</span>
             |-
             | '''🌐 IP'''
             | <code>%s</code>
             |-
             | '''🗂️ 업무분류'''
             | <span style="color: #444;">%s</span>
             |-
             | '''🏢 업무계'''
             | <span style="color: #1a4d1a; font-weight: bold;">%s</span>
             |-
             | '''⚙️ CPU'''
             | <span style="color: #444;">%s</span>
             |-
             | '''💾 Memory'''
             | <span style="color: #444;">%s</span>
             |}
            </div>
        </div>


        == <span id="개요">📘 개요</span> ==
        <div style="margin: 0.5em 0 1.5em 0; font-size: 100%%;">
        <b style="color: #005bac;">%s</b> 서버는 <b style="color: #1a4d1a;">%s</b> 업무를 수행하는 시스템입니다.  
        관리자는 정기적으로 상태를 점검해 주세요. 🔍
        </div>

        == <span id="서버 변경 내역">🖥 서버 변경 내역</span> ==
        (본문 내용이 여기에 옵니다.)

        == <span id="기타 참고사항">📎 참고사항</span> ==
        * 위 정보는 최신 DB 기준 자동 생성된 내용입니다.  
        * 변경사항 발생 시 데이터센터 담당자에게 문의 바랍니다. 📬

        [[Category:%s]]
        """.formatted(
                safe(asset.getHostname()),
                safe(asset.getIp()),
                safe(asset.getWorkCategory()),
                safe(asset.getworkType()),
                safe(asset.getCpu()),
                safe(asset.getMem()),
                safe(asset.getHostname()),
                safe(asset.getworkType()),
                safe(asset.getworkType())
        );
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }
}