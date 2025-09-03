package com.ito.collector.repository;

import com.ito.collector.entity.ChangeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeHistoryRepository extends JpaRepository<ChangeHistory, String> {

    /** hostname 기준으로 변경이력 조회 (최신 요청일자 우선) */
    List<ChangeHistory> findByHostnameOrderByReqDtDesc(String hostname);
}
