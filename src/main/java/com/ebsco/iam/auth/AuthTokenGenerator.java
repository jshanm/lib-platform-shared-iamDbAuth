package com.ebsco.iam.auth;

import java.security.InvalidParameterException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.ContainerCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest;
import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator;

public class AuthTokenGenerator {

  private String rdsDatabaseUserName;
  private String rdsRegion;
  private String rdsInstanceHostName;
  private int rdsInstancePort;

  private RdsIamAuthTokenGenerator tokenGenerator;
  
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthTokenGenerator.class.getName());

  public AuthTokenGenerator(String rdsDatabaseUserName, String rdsRegion,
      String rdsInstanceHostName, int rdsInstancePort) {
    this.rdsDatabaseUserName = rdsDatabaseUserName;
    this.rdsRegion = rdsRegion;
    this.rdsInstanceHostName = rdsInstanceHostName;
    this.rdsInstancePort = rdsInstancePort;
  
    validateMembers();
    initTokenGenerator();
  }

  private void validateMembers() {
    StringBuilder messageBuilder = new StringBuilder();

    if (StringUtils.isEmpty(rdsDatabaseUserName)) {
      messageBuilder.append("Database user can not be blank.\n");
    }

    if (StringUtils.isEmpty(rdsRegion)) {
      messageBuilder.append("Database region can not be blank.\n");
    }

    if (StringUtils.isEmpty(rdsInstanceHostName)) {
      messageBuilder.append("Database instance host name can not be blank.\n");
    }

    if (rdsInstancePort < 0) {
      messageBuilder.append("Database instance port can not be blank.\n");
    }

    String message = messageBuilder.toString();

    if (!StringUtils.isEmpty(message)) {
      throw new InvalidParameterException(message);
    }
  }

  private void initTokenGenerator() {
    AWSCredentialsProvider credentialsProvider = new AWSCredentialsProviderChain(new EnvironmentVariableCredentialsProvider(), new ProfileCredentialsProvider(), new ContainerCredentialsProvider());
   
    this.tokenGenerator = RdsIamAuthTokenGenerator.builder()
        .credentials(credentialsProvider)
        .region(Region.getRegion(Regions.US_EAST_1)).build();
  }

  public String generateAuthToken() {
    String authToken = null;
    
    try{
      authToken = tokenGenerator.getAuthToken(GetIamAuthTokenRequest.builder()
          .hostname(rdsInstanceHostName).port(rdsInstancePort).userName(rdsDatabaseUserName).build());
    } catch(Exception ex){
      LOGGER.error("Exception in generating Token: " + ex.getMessage());
    }
    return authToken;
  }

  public void setTokenGenerator(RdsIamAuthTokenGenerator tokenGenerator) {
    this.tokenGenerator = tokenGenerator;
  }

}
