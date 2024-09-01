package com.example.demo.domain.xrpl.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.Payment;

import com.example.demo.domain.xrpl.dto.DonationInfo;
import com.example.demo.domain.xrpl.dto.GrantInfo;
import com.example.demo.domain.xrpl.dto.GrantStatus;
import com.example.demo.domain.xrpl.repository.DonationInfoRepository;
import com.example.demo.domain.xrpl.repository.GrantInfoRepository;
import com.example.demo.domain.xrpl.service.DonationService;
import com.example.demo.domain.xrpl.service.GrantService;
import com.example.demo.domain.xrpl.service.QuadraticFundingService;
import com.example.demo.domain.xrpl.service.XRPLService;
import com.google.common.primitives.UnsignedInteger;

@RestController
@RequestMapping("/api")
public class XRPLController {

  private GrantService grantService;
  private XRPLService xrplService;
  private DonationService donationService;
  private QuadraticFundingService quadraticFundingService;
  private GrantInfoRepository grantInfoRespository;
  private DonationInfoRepository donationInfoRepository;

  public XRPLController(DonationInfoRepository donationInfoRepository, GrantInfoRepository grantInfoRespository, GrantService grantService, XRPLService xrplService, DonationService donationService, QuadraticFundingService quadraticFundingService){
    this.grantService = grantService;
    this.xrplService = xrplService;
    this.donationService = donationService;
    this.quadraticFundingService = quadraticFundingService;
    this.grantInfoRespository = grantInfoRespository;
    this.donationInfoRepository = donationInfoRepository;
  }

  @PostMapping("/createGrant")// 프론트엔드에서 title, desc, grantId를 보내줌
  public ResponseEntity<String> createGrant(@RequestBody Map<String, Object> data){
    
    // seed로 복구된 주소?
    //KeyPair wallet = Seed.ed25519SeedFromPassphrase(Passphrase.of("sEdSRDZL4oGuFqvuqaaUgCmbiKZkT7Z")).deriveKeyPair();
    try{
      // 새로운 지갑 생성
      KeyPair randomKeyPair = Seed.ed25519Seed().deriveKeyPair();
      // 지갑주소
      Address classicAddress = randomKeyPair.publicKey().deriveAddress();
      
      // Grant 생성
      GrantInfo newGrant = grantService.createGrant(data, classicAddress);
      
      if(newGrant != null){
        // 트랜잭션 만들기
        AccountSet tx = xrplService.createAccountSetTransaction(randomKeyPair,newGrant);
        // 트랜잭션에 서명하기
        SingleSignedTransaction<AccountSet> signedTx = xrplService.signTransaction(randomKeyPair, tx);
        // 트랜잭션 전송
        xrplService.submit(signedTx);
        // 트랜잭션 결과 확인
        xrplService.waitAndValidation(signedTx);
      
        // 생성한 Grant를 DB에 저장하는 작업이 필요항
        grantInfoRespository.save(newGrant);

        return ResponseEntity.ok("Grant created successfully");
      }else{
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Grant creation failed");
      }
      
    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body("An error occurred while creating the grant: " + e.getMessage());
    }
  }

  @PostMapping("/cancel")
  public ResponseEntity<String> cancelGrant(@RequestBody Map<String, Object> data){
    String grantId = (String)data.get("grantId");
    
    // DB에서 grantId인 GrantInfo 객체를 가지고 오기
    GrantInfo grant = grantInfoRespository.findBygrantId(grantId);

    try {
      long currentTime = System.currentTimeMillis() / 1000;
      
      // 새로운 지갑 생성
      KeyPair randomKeyPair = Seed.ed25519Seed().deriveKeyPair();
      // 지갑주소
      Address classicAddress = randomKeyPair.publicKey().deriveAddress();

      // grant가 존재하고 grant 시작전에 취소 가능
      if(currentTime < grant.getGrantStart() && grant.getStatus() == GrantStatus.PENDING && grant != null){
        // 트랜잭션 만들기
        AccountSet tx = xrplService.createAccountSetTransaction(randomKeyPair,grant);
        // 트랜잭션에 서명하기
        SingleSignedTransaction<AccountSet> signedTx = xrplService.signTransaction(randomKeyPair, tx);
        // 트랜잭션 전송
        xrplService.submit(signedTx);
        // 트랜잭션 결과 확인
        xrplService.waitAndValidation(signedTx);

        
      }else{
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Grant cannot be canceled");
      }

    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body("An error occurred while canceling the grant: " + e.getMessage());
    }

    grant.setStatus(GrantStatus.CANCELED);

    // 업데이트 된 grant를 DB에 저장하기
    grantInfoRespository.save(grant);
    
    return ResponseEntity.ok("Grant canceled successfully");
  }

  @PostMapping("/donateGrant")
  public ResponseEntity<String> donateGrant(@RequestBody Map<String, Object> data){
    DonationInfo donation;
    
    String grantId = (String)data.get("grantId");
    
    // DB에 조회를 해서 grantId에 해당하는 grant가 있는지 확인해야함
    GrantInfo grant = grantInfoRespository.findBygrantId(grantId);

    if(grant == null){
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Grant with ID " + grantId + " not found.");
    }
    
    // 새로운 지갑 생성
    KeyPair randomKeyPair = Seed.ed25519Seed().deriveKeyPair();
    // 지갑주소
    Address classicAddress = randomKeyPair.publicKey().deriveAddress();

    long currentTime = System.currentTimeMillis() / 1000;
    
    // 현재 시간이 Grant시작 시간 이후여야 함
    if(currentTime >= grant.getGrantStart() && grant.getStatus() == GrantStatus.ACTIVE){
      try{
        // Grant Update
        grant = grantService.updateGrant(data, grant);
        
        // Donation 생성
        donation = donationService.createDonation(data, classicAddress, grantId);
  
        // Escrow트랜잭션 만들기
        EscrowCreate escroTx = xrplService.createEscrowCreateTransaction(randomKeyPair, donation, grant);
        // 트랜잭션에 서명하기
        SingleSignedTransaction<EscrowCreate> signedEscroTx = xrplService.signEscrowCreateTransaction(randomKeyPair, escroTx);
        // 트랜잭션 전송
        SubmitResult<EscrowCreate> result = xrplService.submitEscrowCreate(signedEscroTx);
        // EscrowFinsih를 만들기 위한 sequence 저장
        UnsignedInteger EscrowCreateTxSequence = result.transactionResult().transaction().sequence();
        
        // 트랜잭션 만들기
        AccountSet tx = xrplService.createAccountSetTransaction(randomKeyPair,grant);
        // 트랜잭션에 서명하기
        SingleSignedTransaction<AccountSet> signedTx = xrplService.signTransaction(randomKeyPair, tx);
        // 트랜잭션 전송
        xrplService.submit(signedTx);
        // 트랜잭션 결과 확인
        xrplService.waitAndValidation(signedTx);

        // 업데이트 된 grant DB, donation DB에 저장하기
        grantInfoRespository.save(grant);

        donation.setEscrowCreateTxSequence(EscrowCreateTxSequence);
        donationInfoRepository.save(donation);
        
        return ResponseEntity.ok("Donation successful");
      }catch(Exception e){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("An error occurred while donating to the grant: " + e.getMessage());      }

    }else{
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Donation failed: Grant has not started yet");

    }

  }
  
  // 관리자가 총 매칭 풀의 금액을 보내준다
  @PostMapping("/distributeFunding")
  public ResponseEntity<String> distributeFunding(@RequestBody Map<String, Object> data){
    try{
      // 관리자 지갑으로 가정
      KeyPair randomKeyPair = Seed.ed25519Seed().deriveKeyPair();
      // 관리자 지갑주소
      Address classicAddress = randomKeyPair.publicKey().deriveAddress();

      // DB에서 모든 GrantInfo 데이터들을 가지고 온다
      List<GrantInfo> grants = grantInfoRespository.findAll();

      // grantId들을 리스트로 만들어서 반환
      List<String> grantIds = grantService.getGrantIdList(grants);

      // grantId : 퍼센트(실수형으로 저장 되어 있음)
      Map<String, Double> fundingRate = quadraticFundingService.calculateDistributionRate(grantIds);

      List<GrantInfo> grant = quadraticFundingService.distributeFunding(data, fundingRate);

      // 관리자가 각 그랜트의 제안자에게 자금을 배분하는 트랜잭션
      // 모든 그랜트에게 자금을 분배해야하기 때문에 반복문 사용
      for(GrantInfo owner : grant){
        Payment paymentTx = xrplService.createPaymentTransaction(randomKeyPair, owner);
        SingleSignedTransaction<Payment> signedPaymentTransaction = xrplService.signPaymentTransaction(randomKeyPair, paymentTx);
        xrplService.submitPayment(signedPaymentTransaction);
        xrplService.paymentWaitAndValidation(signedPaymentTransaction);
      }

      // Escrow에 잠긴 사용자가 기부한 금액을 EscrowFinsih 트랜잭션을 통해 grant 제안자에게 전달하기
      // EscrowFinish 트랜잭션을 생성해야 함 
      // 사용자가 기부할 때 EscrowCreate 트랜잭션을 만들었으므로 그 수만큼 EscrowFinish 트랜잭션을 만들어야 함
      // DB에서 DonationInfo의 데이터를 모두 가지고 오기
      List<DonationInfo> donations = donationInfoRepository.findAll();

      for(DonationInfo donation : donations){
        EscrowFinish tx = xrplService.createEscrowFinishTransaction(randomKeyPair, donation);
        SingleSignedTransaction<EscrowFinish> sigedTx = xrplService.signEscrowFinishTransaction(randomKeyPair, tx);
        xrplService.submitEscrowFinish(sigedTx);
        
      }

      // 모든 과정이 완료되었으므로 AccountSet 트랜잭션을 만들어서 보내기.
      for(GrantInfo g : grant){
        // 트랜잭션 만들기
        AccountSet tx = xrplService.createAccountSetTransaction(randomKeyPair, g);
        // 트랜잭션에 서명하기
        SingleSignedTransaction<AccountSet> signedTx = xrplService.signTransaction(randomKeyPair, tx);
        // 트랜잭션 전송
        xrplService.submit(signedTx);
        // 트랜잭션 결과 확인
        xrplService.waitAndValidation(signedTx);
      }

      // GrantInfo distributed로 상태 변경하기
      for(GrantInfo g : grant){
        g.setStatus(GrantStatus.DISTRIBUTED);
      }
      // DB에 저장하기
      grantInfoRespository.saveAll(grant);
      
      return ResponseEntity.ok("Funding distributed successfully");
    
    }catch(Exception e){
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred while distributing the funding: " + e.getMessage());
    }
  }
}
