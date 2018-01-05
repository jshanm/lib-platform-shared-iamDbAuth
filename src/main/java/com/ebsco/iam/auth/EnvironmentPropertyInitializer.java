package com.ebsco.iam.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public class EnvironmentPropertyInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentPropertyInitializer.class.getName());

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    LOGGER.info("Initializing SSL Environment");
    System.setProperty("javax.net.ssl.trustStore", "/usr/local/rdsKeyStore.jks");
    System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    System.setProperty("javax.net.ssl.trustStorePassword", "changeit");
  }

}
