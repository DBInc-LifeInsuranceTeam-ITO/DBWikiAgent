package com.ito.collector.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cmdb_server_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CmdbAsset {

    @Id
    @Column(name = "hostname")
    private String hostname;

    @Column(name = "ip")
    private String ip;

    @Column(name = "vip")
    private String vip;

    @Column(name = "cpu")
    private String cpu;

    @Column(name = "mem")
    private String mem;

    @Column(name = "work_type")
    private String workType;

    @Column(name = "os_manager")
    private String osManager;

    @Column(name = "mw_manager")
    private String mwManager;
}
