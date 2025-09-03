package com.ito.collector.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;

@Entity
@Table(name = "itsm_csd")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ChangeHistory {

    @Id
    @Column(name = "req_no", nullable = false, length = 50)
    private String reqNo;

    @Column(name = "hostname", length = 100)
    private String hostname;

    /** 요청 제목: PostgreSQL text → String으로! (CLOB 아님) */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)       // getString()으로 읽히게 유도
    @Column(name = "req_title", columnDefinition = "text")
    private String reqTitle;

    /** 요청 설명: PostgreSQL text → String으로! (CLOB 아님) */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)       // getString()으로 읽히게 유도
    @Column(name = "req_desc", columnDefinition = "text")
    private String reqDesc;

    @Column(name = "req_dt")
    private LocalDate reqDt;

    @Column(name = "ci_nm", length = 200)
    private String ciNm;
}
