package com.example.demo.domain.xrpl.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.xrpl.xrpl4j.model.transactions.Address;
import com.example.demo.domain.xrpl.dto.DonationInfo;

@Service
public class DonationServiceImpl implements DonationService{

  @Override
  public DonationInfo createDonation(Map<String, Object> data, Address classicAddress, String grantId) throws Exception {
    
    // Donation 만들기
    long amount = Integer.parseInt((String)data.get("amount"));
    long currentTime = System.currentTimeMillis() / 1000;

    DonationInfo donationInfo = new DonationInfo(grantId, classicAddress, amount, currentTime);
    return donationInfo;
  
  }
}