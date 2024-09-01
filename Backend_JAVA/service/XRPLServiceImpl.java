package com.example.demo.domain.xrpl.service;

import java.math.BigDecimal;
import java.util.Arrays;

import org.springframework.stereotype.Service;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.client.transactions.TransactionRequestParams;
import org.xrpl.xrpl4j.model.client.transactions.TransactionResult;
import org.xrpl.xrpl4j.model.immutables.FluentCompareTo;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.ImmutableMemoWrapper;
import org.xrpl.xrpl4j.model.transactions.Memo;
import org.xrpl.xrpl4j.model.transactions.MemoWrapper;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;

import com.example.demo.domain.xrpl.dto.DonationInfo;
import com.example.demo.domain.xrpl.dto.GrantInfo;
import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

@Service
public class XRPLServiceImpl implements XRPLService{

  private static final int LAST_LEDGER_SEQUENCE_OFFSET = 4;

  private XrplClient xrplClient;

  public XRPLServiceImpl(XrplClient xrplClient){
    this.xrplClient = xrplClient;

  }

  private UnsignedInteger lastLedgerSequence;


  @Override
  public AccountSet createAccountSetTransaction(KeyPair randomKeyPair, GrantInfo grantInfo) throws Exception {
    try{
      // sequence를 얻기 위해 XRPL에 사용자 정보 요청
      AccountInfoRequestParams requestParams = AccountInfoRequestParams.builder()
      .account(randomKeyPair.publicKey().deriveAddress())
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();
      AccountInfoResult accountInfoResult = xrplClient.accountInfo(requestParams);
      UnsignedInteger sequence = accountInfoResult.accountData().sequence();

      // fee정보 받기
      FeeResult feeResult = xrplClient.fee();
      XrpCurrencyAmount openLedgerFee = feeResult.drops().openLedgerFee();

      // Get the latest validated ledger index
      LedgerIndex validatedLedger = xrplClient.ledger(
        LedgerRequestParams.builder()
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
      )
      .ledgerIndex() 
      .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));

      // LastLedgerSequence is the current ledger index + 4
      lastLedgerSequence = validatedLedger.plus(UnsignedInteger.valueOf(LAST_LEDGER_SEQUENCE_OFFSET)).unsignedIntegerValue();

      // AccountSet 트랜잭션 만들기
      Memo memo = Memo.builder().memoType("Grant").memoData(grantInfo.toString()).build();
      MemoWrapper memowrpper = ImmutableMemoWrapper.builder().memo(memo).build();
      
      AccountSet tx = AccountSet.builder()
      .account(randomKeyPair.publicKey().deriveAddress())
      .sequence(sequence)
      .fee(openLedgerFee)
      .memos(Arrays.asList(memowrpper,memowrpper))
      .signingPublicKey(randomKeyPair.publicKey().toString())
      .lastLedgerSequence(lastLedgerSequence)
      .build();

      return tx;

    }catch(Exception e){
      throw new RuntimeException("Failed to create AccountSet transaction",e);
    }
  }

  @Override
  public EscrowCreate createEscrowCreateTransaction(KeyPair randomKeyPair, DonationInfo donation, GrantInfo grant) throws Exception {
    try{
      // EscrowCreate 트랜잭션 만들기
      EscrowCreate tx = EscrowCreate.builder()
      .account(randomKeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(donation.getDonationAmount())))
      .destination(grant.getOwner())
      .finishAfter(UnsignedLong.valueOf(grant.getGrantDeadline()))
      .build();
      return tx;
    } catch (Exception e) {
      throw new RuntimeException("Failed to create EscrowCreate transaction", e);
    }
  }

  @Override
  public Payment createPaymentTransaction(KeyPair randomKeyPair, GrantInfo owner) throws Exception {
    try{
      // sequence를 얻기 위해 XRPL에 사용자 정보 요청
      AccountInfoRequestParams requestParams = AccountInfoRequestParams.builder()
      .account(randomKeyPair.publicKey().deriveAddress())
      .ledgerSpecifier(LedgerSpecifier.VALIDATED)
      .build();
      AccountInfoResult accountInfoResult = xrplClient.accountInfo(requestParams);
      UnsignedInteger sequence = accountInfoResult.accountData().sequence();

      // fee정보 받기
      FeeResult feeResult = xrplClient.fee();
      XrpCurrencyAmount openLedgerFee = feeResult.drops().openLedgerFee();

      // Get the latest validated ledger index
      LedgerIndex validatedLedger = xrplClient.ledger(
        LedgerRequestParams.builder()
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
      )
      .ledgerIndex() 
      .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));

      // LastLedgerSequence is the current ledger index + 4
      lastLedgerSequence = validatedLedger.plus(UnsignedInteger.valueOf(LAST_LEDGER_SEQUENCE_OFFSET)).unsignedIntegerValue();

      // Payment 트랜잭션 만들기
      
      Payment paymentTx = Payment.builder()
      .account(randomKeyPair.publicKey().deriveAddress())
      .amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(owner.getMatchingPoolAmount())))
      .destination(owner.getOwner())
      .sequence(sequence)
      .fee(openLedgerFee)
      .signingPublicKey(randomKeyPair.publicKey().toString())
      .lastLedgerSequence(lastLedgerSequence)
      .build();

      return paymentTx;

    }catch(Exception e){
      throw new RuntimeException("Failed to submit EscrowCreate transaction", e);
    }
  }

  @Override
  public EscrowFinish createEscrowFinishTransaction(KeyPair randomKeyPair, DonationInfo sponsor) throws Exception {
    try{
      EscrowFinish tx = EscrowFinish.builder()
      .account(randomKeyPair.publicKey().deriveAddress())
      .owner(sponsor.getSponsor())
      .offerSequence(sponsor.getEscrowCreateTxSequence())
      .build();
      return tx;
    }catch(Exception e){
      throw new RuntimeException("Failed to create EscrowCreate transaction", e);
    }
  
  }


  @Override
  public SingleSignedTransaction<AccountSet> signTransaction(KeyPair randomKeyPair, AccountSet tx) throws Exception {
    try{
      // 서명 만들기
      SignatureService<PrivateKey> signatureService = new BcSignatureService();

      // 트랜잭션에 서명하기
      SingleSignedTransaction<AccountSet> signedAccountSetTransaction = signatureService.sign(randomKeyPair.privateKey(), tx);
      System.out.println(signedAccountSetTransaction.signedTransaction());
      
      return signedAccountSetTransaction;

    } catch (Exception e) {
      throw new RuntimeException("Failed to sign AccountSet transaction", e);
    }
  }

  @Override
  public SingleSignedTransaction<EscrowCreate> signEscrowCreateTransaction(KeyPair randomKeyPair, EscrowCreate tx)
      throws Exception {
        try{
          // 서명 만들기
          SignatureService<PrivateKey> signatureService = new BcSignatureService();

          // 트랜잭션에 서명하기
          SingleSignedTransaction<EscrowCreate> signedEscrowCreateTransaction = signatureService.sign(randomKeyPair.privateKey(), tx);
          System.out.println(signedEscrowCreateTransaction.signedTransaction());
      
          return signedEscrowCreateTransaction;
        } catch (Exception e) {
          throw new RuntimeException("Failed to sign EscrowCreate transaction", e);
        }
  }

  @Override
  public SingleSignedTransaction<Payment> signPaymentTransaction(KeyPair randomKeyPair, Payment paymentTx)
      throws Exception {
        try{
          // 서명 만들기
          SignatureService<PrivateKey> signatureService = new BcSignatureService();

          // 트랜잭션에 서명하기
          SingleSignedTransaction<Payment> signedPaymentTransaction = signatureService.sign(randomKeyPair.privateKey(), paymentTx);
          System.out.println(signedPaymentTransaction.signedTransaction());
      
          return signedPaymentTransaction;
          

        }catch(Exception e){

          throw new RuntimeException("Failed to submit EscrowCreate transaction", e);
        }
  }

  @Override
  public SingleSignedTransaction<EscrowFinish> signEscrowFinishTransaction(KeyPair randomKeyPair, EscrowFinish tx)
      throws Exception {
        try{
          // 서명 만들기
          SignatureService<PrivateKey> signatureService = new BcSignatureService();

          // 트랜잭션에 서명하기
          SingleSignedTransaction<EscrowFinish> signedEscrowFinishTransaction = signatureService.sign(randomKeyPair.privateKey(), tx);
          System.out.println(signedEscrowFinishTransaction.signedTransaction());
      
          return signedEscrowFinishTransaction;

        }catch(Exception e){
          throw new RuntimeException("Failed to submit EscrowCreate transaction", e);
        }
  }


  @Override
  public SubmitResult<AccountSet> submit(SingleSignedTransaction<AccountSet> signedTx) throws Exception {
    try{
      return xrplClient.submit(signedTx);
    } catch (Exception e) {
      throw new RuntimeException("Failed to submit AccountSet transaction", e);
    }
  }

  @Override
  public SubmitResult<EscrowCreate> submitEscrowCreate(SingleSignedTransaction<EscrowCreate> signedTx)
      throws Exception {
        try{
          return xrplClient.submit(signedTx);
        } catch (Exception e) {
          throw new RuntimeException("Failed to submit EscrowCreate transaction", e);
        }   
  }

  @Override
  public SubmitResult<Payment> submitPayment(SingleSignedTransaction<Payment> signedTx) throws Exception {
    try{
      return xrplClient.submit(signedTx);
    } catch (Exception e) {
      throw new RuntimeException("Failed to submit EscrowCreate transaction", e);
    }  
  }
  
  @Override
  public SubmitResult<EscrowFinish> submitEscrowFinish(SingleSignedTransaction<EscrowFinish> signedTx)
      throws Exception {
        try{
          return xrplClient.submit(signedTx);
        }catch(Exception e){
          throw new RuntimeException("Failed to submit EscrowCreate transaction", e);
        }
  }



  @Override
  public void waitAndValidation(SingleSignedTransaction<AccountSet> signedTx) throws Exception {
    // Wait for validation --------------------------------------------------------
    TransactionResult<AccountSet> transactionResult = null;

    boolean transactionValidated = false;
    boolean transactionExpired = false;
    while (!transactionValidated && !transactionExpired) {
      Thread.sleep(4 * 1000);

      LedgerIndex latestValidatedLedgerIndex = xrplClient.ledger(
        LedgerRequestParams.builder()
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
      )
        .ledgerIndex()
        .orElseThrow(() -> new RuntimeException("Ledger response did not contain a LedgerIndex."));

      transactionResult = xrplClient.transaction(TransactionRequestParams.of(signedTx.hash()), AccountSet.class);

      if (transactionResult.validated()) {
        System.out.println("Payment was validated with result code " + transactionResult.metadata().get().transactionResult());
        transactionValidated = true;
      } else {
        boolean lastLedgerSequenceHasPassed = FluentCompareTo.is(latestValidatedLedgerIndex.unsignedIntegerValue())
          .greaterThan(UnsignedInteger.valueOf(lastLedgerSequence.intValue()));
        if (lastLedgerSequenceHasPassed) {
          System.out.println("LastLedgerSequence has passed. Last tx response: " + transactionResult);
          transactionExpired = true;
        } else {
          System.out.println("Payment not yet validated.");
        }
      }
    }
    // 결과 보기
    if(transactionResult != null){
      System.out.println(transactionResult);
      System.out.println("Explorer link: https://testnet.xrpl.org/transactions/" + signedTx.hash());
      transactionResult.metadata().ifPresent(metadata -> {
        System.out.println("Result code: " + metadata.transactionResult());
        metadata.deliveredAmount().ifPresent(deliveredAmount ->
        System.out.println("XRP Delivered: " + ((XrpCurrencyAmount) deliveredAmount).toXrp()));
        }
      );

    }else{
      System.out.println("Transaction result is null");
    }
  }

  @Override
  public void paymentWaitAndValidation(SingleSignedTransaction<Payment> signedTx) throws Exception {
    // Wait for validation --------------------------------------------------------
    TransactionResult<Payment> transactionResult = null;

    boolean transactionValidated = false;
    boolean transactionExpired = false;
    while (!transactionValidated && !transactionExpired) {
      Thread.sleep(4 * 1000);

      LedgerIndex latestValidatedLedgerIndex = xrplClient.ledger(
        LedgerRequestParams.builder()
        .ledgerSpecifier(LedgerSpecifier.VALIDATED)
        .build()
      )
        .ledgerIndex()
        .orElseThrow(() -> new RuntimeException("Ledger response did not contain a LedgerIndex."));

      transactionResult = xrplClient.transaction(TransactionRequestParams.of(signedTx.hash()), Payment.class);

      if (transactionResult.validated()) {
        System.out.println("Payment was validated with result code " + transactionResult.metadata().get().transactionResult());
        transactionValidated = true;
      } else {
        boolean lastLedgerSequenceHasPassed = FluentCompareTo.is(latestValidatedLedgerIndex.unsignedIntegerValue())
          .greaterThan(UnsignedInteger.valueOf(lastLedgerSequence.intValue()));
        if (lastLedgerSequenceHasPassed) {
          System.out.println("LastLedgerSequence has passed. Last tx response: " + transactionResult);
          transactionExpired = true;
        } else {
          System.out.println("Payment not yet validated.");
        }
      }
    }
    // 결과 보기
    if(transactionResult != null){
      System.out.println(transactionResult);
      System.out.println("Explorer link: https://testnet.xrpl.org/transactions/" + signedTx.hash());
      transactionResult.metadata().ifPresent(metadata -> {
        System.out.println("Result code: " + metadata.transactionResult());
        metadata.deliveredAmount().ifPresent(deliveredAmount ->
        System.out.println("XRP Delivered: " + ((XrpCurrencyAmount) deliveredAmount).toXrp()));
        }
      );

    }else{
      System.out.println("Transaction result is null");
    }
  }

}
