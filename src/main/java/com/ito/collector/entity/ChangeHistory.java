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

    /** ITSM 요청번호 (PK) */
    @Id
    @Column(name = "req_no", nullable = false, length = 50)
    private String reqNo;

    /** 호스트명 */
    @Column(name = "hostname", length = 100)
    private String hostname;

    /** 요청 제목: PostgreSQL text → String (CLOB 아님) */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "req_title", columnDefinition = "text")
    private String reqTitle;

    /** 요청 설명: PostgreSQL text → String (CLOB 아님) */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "req_desc", columnDefinition = "text")
    private String reqDesc;

    /** 요청일자 (date) */
    @Column(name = "req_dt")
    private LocalDate reqDt;

    /** CI 이름 */
    @Column(name = "ci_nm", length = 200)
    private String ciNm;
}
