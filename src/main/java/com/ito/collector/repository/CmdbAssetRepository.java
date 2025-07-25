package com.ito.collector.repository;

import com.ito.collector.entity.CmdbAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CmdbAssetRepository extends JpaRepository<CmdbAsset, String> {
    // 필요하면 커스텀 메서드 추가 가능
}
