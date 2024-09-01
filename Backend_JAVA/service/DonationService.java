package com.example.demo.domain.xrpl.service;

import java.util.Map;

import org.xrpl.xrpl4j.model.transactions.Address;
import com.example.demo.domain.xrpl.dto.DonationInfo;


public interface DonationService {
    DonationInfo createDonation(Map<String, Object> data, Address classicAddress, String grantId) throws Exception;

}
