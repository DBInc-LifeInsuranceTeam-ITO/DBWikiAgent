package com.ito.collector.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cmdb_server_assets")
public class CmdbAsset {

    @Id
    private String hostname;
    private String ip;
    private String vip;
    private String cpu;
    private String mem;
    private String workType;
    private String osManager;
    private String mwManager;
    private String workCategory;

    // 기본 생성자
    public CmdbAsset() {}


    public CmdbAsset(String ip, String hostname, String cpu, String mem, String disk, String workType, String workCategory, String vip) {
        this.hostname = hostname;
        this.ip = ip;
        this.vip = vip;
        this.cpu = cpu;
        this.mem = mem;
        this.workType = workType;
        this.workCategory = workCategory;
    }

    // Getter & Setter
    public String getIp() { return ip; }
    public void setVip(String vip) { this.vip = vip; }
    public String getVip() { return vip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }
    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }
    public String getMem() { return mem; }
    public void setMem(String mem) { this.mem = mem; }
    public String getworkType() { return workType; }
    public void setworkType(String workType) { this.workType = workType; }
    public String getOsManager() { return osManager; }
    public void setOsManager(String osManager) { this.osManager = osManager; }
    public String getMwManager() { return mwManager; }
    public void setMwManager(String mwManager) { this.mwManager = mwManager; }
    public String getWorkCategory() { return workCategory; }
    public void setWorkCategory(String workCategory) { this.workCategory = workCategory; }
}