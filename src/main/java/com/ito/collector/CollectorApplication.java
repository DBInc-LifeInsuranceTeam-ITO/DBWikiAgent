package com.ito.collector;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.service.CmdbAssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class CollectorApplication implements CommandLineRunner {

    @Autowired
    private CmdbAssetService cmdbAssetService;

    public static void main(String[] args) {
        SpringApplication.run(CollectorApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // 애플리케이션 시작 시 실행됨
        List<CmdbAsset> assets = cmdbAssetService.getAllAssets();
        System.out.println("=== CMDB 자산 목록 ===");
        for (CmdbAsset asset : assets) {
            System.out.println(asset);
        }
    }
}
