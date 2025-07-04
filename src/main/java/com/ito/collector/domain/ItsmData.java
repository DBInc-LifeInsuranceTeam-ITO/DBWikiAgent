package com.ito.collector.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ItsmData {

    @Id
    private Long id;

    private String systemName; // ← 이 필드가 있어야 해

    public ItsmData() {
    }

    public ItsmData(Long id, String systemName) {
        this.id = id;
        this.systemName = systemName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSystemName() {  // ← 이 getter가 꼭 필요해!
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
}
