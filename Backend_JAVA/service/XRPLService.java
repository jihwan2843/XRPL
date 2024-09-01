package com.example.demo.domain.xrpl.service;

import java.util.ArrayList;

import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.transactions.SubmitResult;
import org.xrpl.xrpl4j.model.transactions.AccountSet;
import org.xrpl.xrpl4j.model.transactions.EscrowCreate;
import org.xrpl.xrpl4j.model.transactions.EscrowFinish;
import org.xrpl.xrpl4j.model.transactions.Payment;

import com.example.demo.domain.xrpl.dto.DonationInfo;
import com.example.demo.domain.xrpl.dto.GrantInfo;

public interface XRPLService {
  AccountSet createAccountSetTransaction(KeyPair randomKeyPair, GrantInfo grant) throws Exception;
  EscrowCreate createEscrowCreateTransaction(KeyPair randomKeyPair, DonationInfo donation, GrantInfo grant) throws Exception;
  Payment createPaymentTransaction(KeyPair randomKeyPair, GrantInfo owner) throws Exception;
  EscrowFinish createEscrowFinishTransaction(KeyPair randomKeyPair, DonationInfo sponsor) throws Exception;


  SingleSignedTransaction<AccountSet> signTransaction(KeyPair randomKeyPair, AccountSet tx) throws Exception;
  SingleSignedTransaction<EscrowCreate> signEscrowCreateTransaction(KeyPair randomKeyPair, EscrowCreate tx) throws Exception;
  SingleSignedTransaction<Payment> signPaymentTransaction(KeyPair randomKeyPair, Payment paymentTx) throws Exception;
  SingleSignedTransaction<EscrowFinish> signEscrowFinishTransaction(KeyPair randomKeyPair, EscrowFinish tx) throws Exception;


  SubmitResult<AccountSet> submit(SingleSignedTransaction<AccountSet> signedTx) throws Exception;
  SubmitResult<EscrowCreate> submitEscrowCreate(SingleSignedTransaction<EscrowCreate> signedTx) throws Exception;
  SubmitResult<Payment> submitPayment(SingleSignedTransaction<Payment> signedTx) throws Exception;
  SubmitResult<EscrowFinish> submitEscrowFinish(SingleSignedTransaction<EscrowFinish> signedTx) throws Exception;


  void waitAndValidation(SingleSignedTransaction<AccountSet> signedTx) throws Exception;
  void paymentWaitAndValidation(SingleSignedTransaction<Payment> signedTx) throws Exception;

}
