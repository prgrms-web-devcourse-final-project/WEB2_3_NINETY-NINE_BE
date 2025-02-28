package com.example.onculture.global.config;

import com.amazonaws.auth.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonS3Config {

    @Value( "${cloud.aws.region.static}" )
    private String region;
    @Value( "${cloud.aws.credentials.access-key}")
    private String accessKey;
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    // Amazon S3에 접근할 수 있는 클라이언트 객체 생성 및 반환
    @Bean
    public AmazonS3 amazonS3Client() {
        AWSCredentialsProvider credentialsProvider;

        // 인증 정보 생성 ( 환경 변수 기반 인증이 가능하면 기본 AWS 인증 체계를 따름 )
        if (accessKey.isEmpty() || secretKey.isEmpty()) {
            credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
        } else {
            credentialsProvider = new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(accessKey, secretKey)
            );
        }

        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(credentialsProvider)
                .build();
    }
}
