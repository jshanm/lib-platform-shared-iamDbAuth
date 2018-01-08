# lib-platform-shared-iamDbAuth

lib-platform-shared-iamDbAuth library is added as a dependency to any Spring Boot application runs on AWS EC2/ECS which enables it to connect to AWS RDS database without a static database password.
You can able to toggle on/off to use IAM Auth DB authentication


Architecture 
![alt text](https://github.com/jshanm/lib-platform-shared-iamDbAuth/blob/master/src/main/resources/META-INF/Architecture.png "Architecture")


Before we start, let’s talk about the restrictions when using IAM database authentication:

* Using the MySQL or Aurora RDS engine is required (MySQL >=5.6.34, MySQL >=5.7.16, Aurora >1.10).
* A Secure Sockets Layer (SSL) database connection is needed.
* Smallest database instance types do not support IAM database authentication. db.t1.micro and db.m1.small instance types are excluded for MySQL. * The db.t2.small instance type is excluded for Aurora.
* AWS recommends creating no more than 20 database connections per second when using IAM database authentication.

Please read this [AWS document](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.IAMDBAuth.html#UsingWithRDS.IAMDBAuth.ConnectionsPerSecond) before implementing this library.


## How to build
### Standard gradle build
```
gradle clean build
```
## Steps to utilize the library
#### Spring Application
1. Add this dependency in the build.gradle or maven
```
dependency "com.ebsco.platform.shared:lib-platform-shared-iamDbAuth-core:0.0.1.SNAPSHOT"
```
2. If your application has no SSL trustore setup already, do the following.

    2.1 Change the Main method implementation by adding Initializer which will automaticallyb set SSL properties in the System environment.
        
        SpringApplication app = new SpringApplication(RefUrlAuthApplication.class);
            app.addInitializers(new EnvironmentPropertyInitializer());
            app.run(args);
        
    2.2. Copy the rdsKeyStore.jks file from this repositoryAdd  and paste in the Spring boot application at /src/main/resources/keystore/rdsKeyStore.jks and add the below statements in the Dockerfile *(before start.sh)* which will automatically place the JKS file in the required path in the EC2 server installed.

        ADD ./src/main/resources/keystore/rdsKeyStore.jks /usr/local/rdsKeyStore.jks
        RUN chmod 0755 /usr/local/rdsKeyStore.jks

3. If your application has already ssl keystore setup, just add certificate which you can download from this [AWS page](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.SSL.html#UsingWithRDS.SSL.IntermediateCertificates)
4. Add the below properties in application.yml
    ```
     rds:
        iamAuth:
            enabled: true
            user: refurlapp
            region: us-east-1
    ```

#### RDS Database cluster
1. Enable the IAM Auth DB for the RDS database cluster/instance. Please follow this [document](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.IAMDBAuth.Enabling.html)
2. Allow the application role to connect to the RDS cluster by creating and attaching an IAM policy to the role. Please follow the [document](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/UsingWithRDS.IAMDBAuth.IAMPolicy.html)

#### Mysql Database
1. Create a user with the username you want to access the database with.

```
CREATE USER 'refurlapp' IDENTIFIED WITH AWSAuthenticationPlugin as 'RDS';
GRANT ALL PRIVILEGES ON <DB_NAME>.* TO 'refurlapp'@'%';
FLUSH PRIVILEGES;
```

As these passwords expire every 15 minutes, the password will be refreshed every 14 minutes.
