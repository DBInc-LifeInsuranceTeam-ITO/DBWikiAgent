package com.ito.collector.repository;

import com.ito.collector.entity.IssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueHistoryRepository extends JpaRepository<IssueHistory, Long> {
    List<IssueHistory> findByTargetServersContainingIgnoreCaseOrderByCreatedAtDesc(String ciNm);
}