package com.example.hibval;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// 010 sec04: Flyway 構築スキーマ widget(id) に対し、エンティティは name 列を宣言する。
// ddl-auto=validate 相当（Hibernate hbm2ddl.auto=validate）で不整合を検出させる。
@Entity
@Table(name = "widget")
public class Widget {

    @Id
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    public Long getId() { return id; }
    public String getName() { return name; }
}
