package com.ito.collector.repository;

import com.ito.collector.entity.CmdbAsset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CmdbAssetRepository extends JpaRepository<CmdbAsset, String> {
}
