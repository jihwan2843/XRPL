package com.example.demo.domain.xrpl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.xrpl.xrpl4j.client.XrplClient;

import okhttp3.HttpUrl;

@Configuration
public class XRPLConfig {

  @Bean
  public XrplClient xrplClient(){
    HttpUrl rippleTestnet = HttpUrl.get("https://s.altnet.rippletest.net:51234/");
    return new XrplClient(rippleTestnet);
  }
  
}
