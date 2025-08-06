package com.ito.collector.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "change_history")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ChangeHistory {

    @Id
    private String reqNo;        // ITSM 번호
    private String ciNm;         // CI 이름
    private String reqTitle;     // 요청 제목
    private String reqPerId;     // 요청자 ID
    private String reqDesc;      // 요청 설명
    private String reqPerson;    // 요청자


    public ChangeHistory(String reqNo, String ciNm, String reqTitle, String reqPerId, String reqDesc, String reqPerson) {
        this.reqNo = reqNo;
        this.ciNm = ciNm;
        this.reqTitle = reqTitle;
        this.reqPerId = reqPerId;
        this.reqDesc = reqDesc;
        this.reqPerson = reqPerson;
    }
    // Getter & Setter
    public String getReqNo() { return reqNo; }
    public void setReqNo(String reqNo) { this.reqNo = reqNo; }
    public String getCiNm() { return ciNm; }
    public void setCiNm(String ciNm) { this.ciNm = ciNm; }
    public String getReqTitle() { return reqTitle; }
    public void setReqTitle(String reqTitle) { this.reqTitle = reqTitle; }
    public String getReqPerId() { return reqPerId; }
    public void setReqPerId(String reqPerId) { this.reqPerId = reqPerId; }
    public String getReqDesc() { return reqDesc; }
    public void setReqDesc(String reqDesc) { this.reqDesc = reqDesc; }
    public String getReqPerson() { return reqPerson; }
    public void setReqPerson(String reqPerson) { this.reqPerson = reqPerson; }
}