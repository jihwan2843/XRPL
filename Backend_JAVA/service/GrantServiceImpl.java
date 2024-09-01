package com.example.demo.domain.xrpl.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.xrpl.xrpl4j.model.transactions.Address;
import com.example.demo.domain.xrpl.dto.GrantInfo;
import com.example.demo.domain.xrpl.dto.GrantStatus;
import com.example.demo.domain.xrpl.repository.GrantInfoRepository;

@Service
public class GrantServiceImpl implements GrantService{


  private GrantInfoRepository grantInfoRespository;

  public GrantServiceImpl(GrantInfoRepository grantInfoRespository){
    this.grantInfoRespository = grantInfoRespository;
  }

  @Override
  public GrantInfo createGrant(Map<String, Object> data, Address classicAddress) throws Exception {
    
    // Grant 만들기
    String grantId = (String)data.get("grantId");
    String title = (String)data.get("title");
    String description = (String)data.get("description");
    long currentTime = System.currentTimeMillis()/1000;
    // DB에 grantId의 grant가 존재하는지 체크하기
    if (grantInfoRespository.existsById(grantId)) {
      throw new Exception("Grant with the given ID already exists.");
  }
    GrantInfo newGrant = new GrantInfo(grantId, classicAddress, title, description, currentTime);
    
    return newGrant;
  }

  @Override
  public GrantInfo updateGrant(Map<String, Object> data, GrantInfo grant) throws Exception {
    
    long amount = Long.parseLong((String)data.get("amount"));
    long prevAmount = grant.getTotalDonationAmount();
    grant.setTotalDonationAmount(prevAmount + amount);


    if(grant.getTotalDonationAmount() > 0){
      grant.setStatus(GrantStatus.ACTIVE);
    }

    return grant;
  }

  // 1시간 마다 grant의 상태를 자동적으로 업데이트
  @Override
  @Scheduled(fixedRate = 36000000)
  public void updateGrantStatus() {
    try{
      // DB에서 Grant의 모든 데이터를 가지고 와야함
      List<GrantInfo> grants = grantInfoRespository.findAll();

      long currentTime = System.currentTimeMillis() / 1000;

      for (GrantInfo grant : grants) {
        if (grant.getStatus() == GrantStatus.PENDING && currentTime >= grant.getGrantStart()) {
            grant.setStatus(GrantStatus.ACTIVE);
            
        }else if(grant.getStatus() == GrantStatus.ACTIVE && currentTime >= grant.getGrantDeadline()){
          grant.setStatus(GrantStatus.COMPLETED);
          
        }
        // DB에 grant 업데이트 하기
        grantInfoRespository.save(grant);
      }

    }catch(Exception e){
      System.err.println("An error occurred while updating grant statuses: " + e.getMessage());
    }
  }

  @Override
  public List<String> getGrantIdList(List<GrantInfo> grants) throws Exception{
    List<String> grantIds = new ArrayList<>();

    for(GrantInfo grant : grants){
      //Grant의 상태가 completed일때 자금을 분배 가능
      if(grant.getStatus() == GrantStatus.COMPLETED){
        grantIds.add(grant.getGrantId());
      }else{
         // 상태가 COMPLETED가 아닐 경우 에러 발생
        throw new Exception("Grant with ID " + grant.getGrantId() + " is not in COMPLETED status. Status: " + grant.getStatus());
      }
    }

    return grantIds;
  }
}
