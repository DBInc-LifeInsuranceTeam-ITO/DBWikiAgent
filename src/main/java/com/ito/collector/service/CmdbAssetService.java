package com.ito.collector.service;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.repository.CmdbAssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CmdbAssetService {
    private final CmdbAssetRepository cmdbAssetRepository;

    public CmdbAssetService(CmdbAssetRepository cmdbAssetRepository) {
        this.cmdbAssetRepository = cmdbAssetRepository;
    }

    public List<CmdbAsset> getAllAssets() {
        return cmdbAssetRepository.findAll();
    }

    public CmdbAsset getByHostname(String hostname) {
        return cmdbAssetRepository.findById(hostname).orElse(null);
    }
}
