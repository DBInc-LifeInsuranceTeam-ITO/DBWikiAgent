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
<<<<<<< HEAD
    @Column(name = "req_no", nullable = false, length = 50)
    private String reqNo;
=======
    private String reqNo;        // ITSM CSD 번호
    private String ciNm;         // CI 이름
    private String reqTitle;     // 요청 제목
    private String reqDt;     // 요청 날짜
    private String reqDesc;
    @Column(name = "hostname") // 요청 설명
    private String hostName;    // 서버 호스트네임
>>>>>>> 4e4df541219a437273ecff829624634f924514c5

    @Column(name = "hostname", length = 100)
    private String hostname;

<<<<<<< HEAD
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
=======
    public ChangeHistory(String reqNo, String ciNm, String reqTitle, String reqDt, String reqDesc, String hostName) {
        this.reqNo = reqNo;
        this.ciNm = ciNm;
        this.reqTitle = reqTitle;
        this.reqDt = reqDt;
        this.reqDesc = reqDesc;
        this.hostName = hostName;
    }
    // Getter & Setter
    public String getReqNo() { return reqNo; }
    public void setReqNo(String reqNo) { this.reqNo = reqNo; }
    public String getCiNm() { return ciNm; }
    public void setCiNm(String ciNm) { this.ciNm = ciNm; }
    public String getReqTitle() { return reqTitle; }
    public void setReqTitle(String reqTitle) { this.reqTitle = reqTitle; }
    public String getReqDt() { return reqDt; }
    public void setReqDt(String reqDt) { this.reqDt = reqDt; }
    public String getReqDesc() { return reqDesc; }
    public void setReqDesc(String reqDesc) { this.reqDesc = reqDesc; }
    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }
}
>>>>>>> 4e4df541219a437273ecff829624634f924514c5
