package com.ito.collector;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.service.CmdbAssetService;
import com.ito.collector.service.CmdbAssetUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class CollectorApplication implements CommandLineRunner {

    @Autowired
    private CmdbAssetService cmdbAssetService;

    @Autowired
    private CmdbAssetUploadService cmdbAssetUploadService;

    public static void main(String[] args) {
        SpringApplication.run(CollectorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        List<CmdbAsset> assets = cmdbAssetService.getAllAssets();

        for (CmdbAsset asset : assets) {
            String hostname = asset.getHostname();
            System.out.println("▶ 위키 페이지 업데이트 시도: " + hostname);
            cmdbAssetUploadService.updateExistingWikiPage(hostname);
        }

        System.out.println("=== 모든 자산 위키 업데이트 완료 ===");
    }
}
