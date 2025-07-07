package com.ito.collector.adapter;

import com.ito.collector.entity.CmdbAsset;
import com.ito.collector.repository.CmdbAssetRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ItsmDbAdapter {

    private final CmdbAssetRepository repository;

    public ItsmDbAdapter(CmdbAssetRepository repository) {
        this.repository = repository;
    }

    public List<CmdbAsset> fetch() {
        return repository.findAll();
    }
}
