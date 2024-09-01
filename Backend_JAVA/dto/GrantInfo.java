package com.example.demo.domain.xrpl.dto;

import org.xrpl.xrpl4j.model.transactions.Address;

public class GrantInfo {
  private String grantId;
  private Address owner;
  private String title;
  private String description;
  private long grantStart;
  private long grantDeadline = getGrantStart() + 2419200;
  private long totalDonationAmount = 0;
  private long matchingPoolAmount = 0;
  private GrantStatus status;
  
  public GrantStatus getStatus() {
    return status;
  }
  public void setStatus(GrantStatus status) {
    this.status = status;
  }
  public String getGrantId() {
    return grantId;
  }
  public void setGrantId(String grantId) {
    this.grantId = grantId;
  }
  public Address getOwner() {
    return owner;
  }
  public void setOwner(Address owner) {
    this.owner = owner;
  }
  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public long getGrantStart() {
    return grantStart;
  }

  public long getGrantDeadline() {
    return grantDeadline;
  }

  public long getTotalDonationAmount() {
    return totalDonationAmount;
  }
  public void setTotalDonationAmount(long totalDonationAmount) {
    this.totalDonationAmount = totalDonationAmount;
  }
  public long getMatchingPoolAmount() {
    return matchingPoolAmount;
  }
  public void setMatchingPoolAmount(long matchingPoolAmount) {
    this.matchingPoolAmount = matchingPoolAmount;
  }

  public GrantInfo(String grantId, Address owner, String title, String description, long currentTime) {
    this.grantId = grantId;
    this.owner = owner;
    this.title = title;
    this.description = description;
    this.grantStart = currentTime + 604800;
    this.grantDeadline = this.grantStart + 2592000;
    this.status = GrantStatus.PENDING;    
  }
}
