package com.ito.collector.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class ItsmData {

    @Id
    private Long id;

    private String systemName; 

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

    public String getSystemName() {  
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }
}
