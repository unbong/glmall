package com.atguigu.gulimall.thirdparty.confit;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Slf4j
@Configuration
public class StoreConfig {

    @Value("${cloud.aws.credentials.access-key}")
    private String key;
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${application.bucket.name}")
    private String bucketName;

    @Bean
    @Primary
    AmazonS3 as3config()
    {

        log.info("key: " + key + " skey: " + secretKey + " region " + region + " bucketName: " + bucketName);
        AWSCredentials credentials = new BasicAWSCredentials(key, secretKey);


        return AmazonS3ClientBuilder.standard().withCredentials(
                new AWSStaticCredentialsProvider(credentials)
        ).withRegion(region).build();

    }

}


