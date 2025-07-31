package com.ito.collector.controller;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.service.CmdbAssetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cmdb")
public class CmdbAssetController {

    private final CmdbAssetService cmdbAssetService;

    public CmdbAssetController(CmdbAssetService cmdbAssetService) {
        this.cmdbAssetService = cmdbAssetService;
    }

    @GetMapping("/assets")
    public List<CmdbAsset> getAssets() {
        return cmdbAssetService.getAllAssets();
    }
}
