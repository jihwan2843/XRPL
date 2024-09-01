package com.example.demo.domain.xrpl.entity;

import java.sql.Timestamp;

import com.example.demo.domain.xrpl.dto.GrantStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "grant_info")
public class GrantInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String grantId;

    @Column(nullable = false)
    private String grantName;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Timestamp startDate;

    @Column(nullable = false)
    private Timestamp endDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GrantStatus status;
}
