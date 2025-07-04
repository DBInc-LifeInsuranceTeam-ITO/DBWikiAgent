package com.ito.collector.repository;

import com.ito.collector.domain.ItsmData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItsmViewRepository extends JpaRepository<ItsmData, Long> {
    // 필요한 커스텀 쿼리 메서드 작성 가능
}
