package com.example.demo.domain.xrpl.dto;

import org.xrpl.xrpl4j.model.transactions.Address;

import com.google.common.primitives.UnsignedInteger;

public class DonationInfo {
  private Address sponsor;
  private long donationAmount;
  private long grantTime;
  private String grantId;
  private UnsignedInteger EscrowCreateTxSequence;

  public void setDonationAmount(long donationAmount) {
    this.donationAmount = donationAmount;
  }
  public UnsignedInteger getEscrowCreateTxSequence() {
    return EscrowCreateTxSequence;
  }
  public void setEscrowCreateTxSequence(UnsignedInteger escrowCreateTxSequence) {
    EscrowCreateTxSequence = escrowCreateTxSequence;
  }
  public String getGrantId() {
    return grantId;
  }
  public void setGrantId(String grantId) {
    this.grantId = grantId;
  }
  public Address getSponsor() {
    return sponsor;
  }
  public void setSponsor(Address sponsor) {
    this.sponsor = sponsor;
  }
  public long getDonationAmount() {
    return donationAmount;
  }
  public void setDonationAmount(int donationAmount) {
    this.donationAmount = donationAmount;
  }
  public long getGrantTime() {
    return grantTime;
  }
  public void setGrantTime(long grantTime) {
    this.grantTime = grantTime;
  }
  public DonationInfo(String grantId, Address sponsor, long donationAmount, long grantTime) {
    this.grantId = grantId;
    this.sponsor = sponsor;
    this.donationAmount = donationAmount;
    this.grantTime = grantTime;
  }

  
}
