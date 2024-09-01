package com.example.demo.domain.xrpl.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.domain.xrpl.dto.DonationInfo;
import com.example.demo.domain.xrpl.dto.GrantInfo;
import com.example.demo.domain.xrpl.repository.DonationInfoRepository;
import com.example.demo.domain.xrpl.repository.GrantInfoRepository;

@Service
public class QuadraticFundingServiceImpl implements QuadraticFundingService{

  private DonationInfoRepository donationInfoRepository;
  private GrantInfoRepository grantInfoRepository;

  public QuadraticFundingServiceImpl(GrantInfoRepository grantInfoRepository, DonationInfoRepository donationInfoRepository){
    this.donationInfoRepository = donationInfoRepository;
    this.grantInfoRepository = grantInfoRepository;
  }

  @Override
  public double calculateSumOfSqrt(String grantId) throws Exception {

    // 특정 grant 후원 금액의 제곱근의 합
    double sum = 0.0;
    // DB에서 특정 grantid의 DonationInfo 데이터를 가지고 온다.
    List<DonationInfo> donations = donationInfoRepository.findBygrantId(grantId);

    for(DonationInfo donation : donations){
      sum += Math.sqrt(donation.getDonationAmount());
    }

    return sum * sum;
  }

  @Override
  public Map<String, Double> calculateDistributionRate(List<String> grantIds) throws Exception {
    // grantId : rate
    Map<String, Double> fundingRate = new HashMap<>();
    
    double sum = 0.0;

    for(String grantId : grantIds){
      double rate = calculateSumOfSqrt(grantId);
      sum += rate;
      fundingRate.put(grantId, rate);
    }

    for(String grantId : grantIds){
      double rate = fundingRate.get(grantId) / sum;
      fundingRate.put(grantId, rate);
    }

    return fundingRate;
  }

  

  @Override
  public List<GrantInfo> distributeFunding(Map<String, Object> data, Map<String, Double> fundingRate) throws Exception {
    int totalMatchingPoolAmount = Integer.parseInt((String)data.get("totalMatchingPool"));

    // DB에서 GrantInfo에 있는 데이터를 모두 가지고 와야함.
    List<GrantInfo> grants = grantInfoRepository.findAll();
    
    // fundingRate에 저장되어 있는 grantId별 분배 비율을 totalMatchingPoolAmount 에 곱해서 grants에 업데이트 한 뒤 DB에 저장.
    for(int i=0; i< grants.size(); i++){
      String grantId = grants.get(i).getGrantId();
      double rate = fundingRate.get(grantId);
      grants.get(i).setMatchingPoolAmount((long)totalMatchingPoolAmount * (long)rate);
    }

    // 각 grant별 totalMatchingPoolAmount를 업데이트 하는 작업을 하였으므로 grants를 DB에 저장하는 작업이 필요
    grantInfoRepository.saveAll(grants);

    return grants;
  }
}
