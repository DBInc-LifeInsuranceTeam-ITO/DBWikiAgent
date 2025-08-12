package com.ito.collector.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "issue_history")
public class IssueHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 내부 PK

    @Column(name = "title", nullable = false, columnDefinition = "TEXT")
    private String title;  // 제목

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;  // 내용

    @Column(name = "status", length = 50)
    private String status;  // 상태

    @Column(name = "issue_owner", length = 100)
    private String issueOwner;  // Issue Owner

    @Column(name = "work_part", length = 100)
    private String workPart;  // 업무파트

    @Column(name = "target_servers", columnDefinition = "TEXT")
    private String targetServers;  // 대상서버명

    @Column(name = "itsm_csd_no", length = 50)
    private String itsmCsdNo;  // ITSM CSD 번호

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;  // 생성일시

    // ===== Getter & Setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getIssueOwner() { return issueOwner; }
    public void setIssueOwner(String issueOwner) { this.issueOwner = issueOwner; }
    public String getWorkPart() { return workPart; }
    public void setWorkPart(String workPart) { this.workPart = workPart; }
    public String getTargetServers() { return targetServers; }
    public void setTargetServers(String targetServers) { this.targetServers = targetServers; }
    public String getItsmCsdNo() { return itsmCsdNo; }
    public void setItsmCsdNo(String itsmCsdNo) { this.itsmCsdNo = itsmCsdNo; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}