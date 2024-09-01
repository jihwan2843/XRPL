package com.example.demo.domain.xrpl.service;

import java.util.List;
import java.util.Map;

import org.xrpl.xrpl4j.model.transactions.Address;

import com.example.demo.domain.xrpl.dto.GrantInfo;


public interface GrantService {
  GrantInfo createGrant(Map<String, Object> data, Address classicAddress) throws Exception;
  GrantInfo updateGrant(Map<String, Object> data, GrantInfo grant) throws Exception;
  void updateGrantStatus();
  List<String> getGrantIdList(List<GrantInfo> grants) throws Exception;
}
