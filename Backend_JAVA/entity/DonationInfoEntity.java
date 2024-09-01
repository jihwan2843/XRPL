package com.example.demo.domain.xrpl.entity;

import org.xrpl.xrpl4j.model.transactions.Address;

import com.google.common.primitives.UnsignedInteger;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class DonationInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String sponsor;  // Address를 String으로 저장합니다.

    @Column(nullable = false)
    private String grantId;
    
    @Column(nullable = false)
    private long donationAmount;

    @Column(nullable = false)
    private long grantTime;

    @Column(nullable = false)
    private UnsignedInteger escrowCreateTxSequence;  // UnsignedInteger를 String으로 저장합니다.

    // Getters and Setters
    public Address getSponsorAsAddress() {
        return Address.of(sponsor);
    }

    public void setSponsor(Address sponsor) {
        this.sponsor = sponsor.toString();
    }

    public UnsignedInteger getEscrowCreateTxSequence() {
        return escrowCreateTxSequence;
    }

    public void setEscrowCreateTxSequence(UnsignedInteger escrowCreateTxSequence) {
        this.escrowCreateTxSequence = escrowCreateTxSequence;
    }
}
