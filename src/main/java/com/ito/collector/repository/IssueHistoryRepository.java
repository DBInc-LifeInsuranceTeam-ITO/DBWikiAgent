package com.ito.collector.repository;

import com.ito.collector.entity.IssueHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueHistoryRepository extends JpaRepository<IssueHistory, Long> {
    List<IssueHistory> findByTargetServersContainingIgnoreCaseOrderByCreatedAtDesc(String ciNm);
    // 필요한 경우 추가 조회 메서드 정의 가능
    // 예: 상태로 검색
    // List<IssueHistory> findByStatus(String status);

    // 예: Issue Owner로 검색
    // List<IssueHistory> findByIssueOwner(String issueOwner);

    // 예: ITSM CSD 번호로 검색
    // Optional<IssueHistory> findByItsmCsdNo(String itsmCsdNo);
}