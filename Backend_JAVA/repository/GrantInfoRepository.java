package com.example.demo.domain.xrpl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.domain.xrpl.dto.GrantInfo;

public interface GrantInfoRepository extends JpaRepository<GrantInfo, String>{
  
  boolean existsById(String grantId);
  GrantInfo findBygrantId(String grantId);
}
