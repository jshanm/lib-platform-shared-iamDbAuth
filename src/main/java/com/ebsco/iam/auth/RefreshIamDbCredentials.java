package com.ebsco.iam.auth;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ebsco.iam.auth.utils.IAMAuthDBUtil;

@Component
@ConditionalOnProperty(value="rds.iamAuth.enabled", havingValue = "true")
public class RefreshIamDbCredentials{
  
  private static final Logger LOGGER = LoggerFactory.getLogger(RefreshIamDbCredentials.class.getName());
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
  
  private String rdsDatabaseUser;
  private String rdsRegionName;
  private URI jdbcUri;
  
  @Autowired
  public RefreshIamDbCredentials(@Value("${rds.iamAuth.user}") String rdsDatabaseUser,
      @Value("${rds.iamAuth.region}") String rdsRegionName,
      @Value("${spring.datasource.url}") String jdbcUrl) {
    
    this.rdsDatabaseUser = rdsDatabaseUser;
    this.rdsRegionName = rdsRegionName;
    this.jdbcUri = IAMAuthDBUtil.parseURL(jdbcUrl);
  }
  
  @Scheduled(fixedRate = 840000)
  @ConditionalOnProperty(value="rds.iamAuth.enabled", havingValue = "true")
  public void refreshPassword() {
      LOGGER.info("Refreshing Database password token now {}", dateFormat.format(new Date()));
      System.setProperty("spring.datasource.password", rdsAuthTokenGenerator().generateAuthToken());
  }
  
  @Bean
  @ConditionalOnProperty(value="rds.iamAuth.enabled", havingValue = "true")
  public AuthTokenGenerator rdsAuthTokenGenerator() {
    LOGGER.info("IAM Auth DB: Initializing Bean rdsAuthTokenGenerator");
    return new AuthTokenGenerator(rdsDatabaseUser, rdsRegionName, jdbcUri.getHost(),
        Integer.valueOf(jdbcUri.getPort()));
  }

}
