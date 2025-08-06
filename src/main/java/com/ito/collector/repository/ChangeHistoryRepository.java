package com.ito.collector.repository;

import com.ito.collector.entity.ChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeHistoryRepository extends JpaRepository<ChangeHistory, String> {
    List<ChangeHistory> findByCiNm(String cinm); // 특정 hostname의 변경이력 목록 조회
}
