package com.ebsco.iam.auth.utils;

import java.net.URI;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IAMAuthDBUtil {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(IAMAuthDBUtil.class.getName());
  
  public static URI parseURL(String url){
    URI uri = null;
   try{
     String cleanURI = url.substring(5);
     uri = URI.create(cleanURI);
   } catch(Exception e){
     LOGGER.error("Exception in parsing JDBC url: {}", e.getLocalizedMessage());
   }
    return uri;
  }

  public static String formJdbcUrl(URI jdbcUri) {
    URIBuilder uri = new URIBuilder(jdbcUri).addParameter("verifyServerCertificate", "true")
        .addParameter("useSSL", "true")
        .addParameter("requireSSL", "true");
    return "jdbc:" + uri.toString();
  }

}
