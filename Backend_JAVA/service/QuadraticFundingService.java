package com.example.demo.domain.xrpl.service;

import java.util.List;
import java.util.Map;

import com.example.demo.domain.xrpl.dto.GrantInfo;


public interface QuadraticFundingService {
  Map<String, Double> calculateDistributionRate(List<String> grantIds) throws Exception;
  double calculateSumOfSqrt(String grantid) throws Exception;
  List<GrantInfo> distributeFunding(Map<String, Object> data, Map<String, Double> fundingRate) throws Exception;

}
