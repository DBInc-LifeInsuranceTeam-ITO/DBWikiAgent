package com.ito.collector.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cmdb_asset")
public class CmdbAsset {

    @Id
    private String hostname;
    private String ip;
    private String vip;
    private String cpu;
    private String mem;
    private String disk;
    private String workType;
    private String description;
    private String osManager;
    private String mwManager;

    // 기본 생성자
    public CmdbAsset() {}


    public CmdbAsset(String ip, String hostname, String cpu, String mem, String disk, String workType, String vip) {
        this.hostname = hostname;
        this.ip = ip;
        this.vip = vip;
        this.cpu = cpu;
        this.mem = mem;
        this.disk = disk;
        this.workType = workType;
    }

    // Getter & Setter
    public String getIp() {
        return ip;
    }

    public void setVip(String vip) {
        this.ip = ip;
    }

    public String getVip() {
        return vip;
    }

    public void setIp(String vip) {
        this.vip = vip;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getMem() {
        return mem;
    }

    public void setMem(String mem) {
        this.mem = mem;
    }

    public String getDisk() {
        return disk;
    }

    public void setDisk(String disk) {
        this.disk = disk;
    }

    public String getworkType() {
        return workType;
    }

    public void setworkType(String workType) {
        this.workType = workType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOsManager() { return osManager; }

    public void setOsManager(String osManager) { this.osManager = osManager; }

    public String getMwManager() { return mwManager; }

    public void setMwManager(String mwManager) { this.mwManager = mwManager; }
}
