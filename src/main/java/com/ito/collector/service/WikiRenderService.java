package com.ito.collector.service;

import com.ito.collector.entity.CmdbAsset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WikiRenderService {

    private final CmdbAssetService cmdbAssetService;
    private final ChangeHistoryService changeHistoryService;


    public String renderFullContent(String hostname) {
        // 1. CMDB 자산 정보 테이블 블럭
        CmdbAsset asset = cmdbAssetService.getByHostname(hostname);
        String assetBlock = buildAssetTable(asset);

        // 2. 변경이력 블럭
        String changeBlock = changeHistoryService.buildChangeHistoryBlock(hostname);

        // 3. 전체 위키 페이지 콘텐츠 반환
        return assetBlock + "\n\n----\n\n" + changeBlock;
    }

    private String buildAssetTable(CmdbAsset asset) {
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
                     |-
                     | '''🔧 OS 담당자'''
                     | <span style="color: #444;">%s</span>
                     |-
                     | '''💻 MW 담당자'''
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
                
                %s
                """.formatted(
                safe(asset.getHostname()),
                safe(asset.getIp()),
                safe(asset.getWorkCategory()),
                safe(asset.getWorkType()),
                safe(asset.getCpu()),
                safe(asset.getMem()),
                safe(asset.getOsManager()),
                safe(asset.getMwManager()),
                safe(asset.getHostname()),
                safe(asset.getWorkType()),
                safe(asset.getWorkCategory())
        );
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }
}