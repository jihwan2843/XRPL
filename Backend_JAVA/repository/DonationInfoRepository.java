package com.example.demo.domain.xrpl.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.xrpl.xrpl4j.model.transactions.Address;

import com.example.demo.domain.xrpl.dto.DonationInfo;

public interface DonationInfoRepository extends JpaRepository<DonationInfo, Address>{
  List<DonationInfo> findBygrantId(String grantId);
}
