package com.ito.collector.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "change_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChangeHistory {

    @Id
    @Column(name = "itsm_id", nullable = false, length = 30)
    private String itsmId; // ITSM 번호 (PK)

    @Column(name = "hostname", nullable = false, length = 100)
    private String hostname;

    @Column(name = "task_name", length = 255)
    private String taskName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
