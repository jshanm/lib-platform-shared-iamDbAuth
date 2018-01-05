package com.ebsco.iam.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ebsco.iam.auth.utils.IAMAuthDBUtil;
import java.net.URI;

import javax.sql.DataSource;

@Configuration
public class IAMAuthDBConfiguration {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(IAMAuthDBConfiguration.class.getName());

  private String rdsDatabaseUser;
  private String rdsRegionName;
  private String driverClassName;
  private URI jdbcUri;

  @Autowired
  public IAMAuthDBConfiguration(@Value("${rds.iamAuth.user}") String rdsDatabaseUser,
      @Value("${rds.iamAuth.region}") String rdsRegionName,
      @Value("${spring.datasource.url}") String jdbcUrl,
      @Value("${spring.datasource.driverClassName}") String driverClassName) {

    this.rdsDatabaseUser = rdsDatabaseUser;
    this.rdsRegionName = rdsRegionName;
    this.driverClassName = driverClassName;
    this.jdbcUri = IAMAuthDBUtil.parseURL(jdbcUrl);
  }

  @Bean
  @ConditionalOnProperty(value = "rds.iamAuth.enabled", havingValue = "true")
  public DataSource dataSource() {

    LOGGER.info("IAM Auth DB: Initializing dataSource bean");
    String authToken = rdsAuthTokenGenerator().generateAuthToken();
    DataSource dataSource = null;
    try {
      dataSource = DataSourceBuilder.create().username(rdsDatabaseUser)
          .url(IAMAuthDBUtil.formJdbcUrl(jdbcUri)).password(authToken)
          .driverClassName(driverClassName).build();
      System.setProperty("spring.datasource.url",
          IAMAuthDBUtil.formJdbcUrl(jdbcUri));
      System.setProperty("spring.datasource.password", authToken);
    } catch (Exception e) {
      LOGGER.error("Problem in creating Datasource" + e.getLocalizedMessage());
    }
    LOGGER.info("Datasource Created successfully");
    return dataSource;
  }

  @Bean
  @ConditionalOnProperty(value = "rds.iamAuth.enabled", havingValue = "true")
  public AuthTokenGenerator rdsAuthTokenGenerator() {
    LOGGER.info("IAM Auth DB: Initializing Bean rdsAuthTokenGenerator");
    return new AuthTokenGenerator(rdsDatabaseUser, rdsRegionName, jdbcUri.getHost(),
        Integer.valueOf(jdbcUri.getPort()));
  }

}
