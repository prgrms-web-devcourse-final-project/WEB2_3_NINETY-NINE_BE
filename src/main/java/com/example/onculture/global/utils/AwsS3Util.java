package com.example.onculture.global.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AwsS3Util {

    private final AmazonS3 amazonS3Client;
    public static final String bucket = "ninetynine-profile-image";

    public void uploadFile(MultipartFile file, String fileName) {

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try {
            amazonS3Client.putObject( bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            System.out.println("[에러] 파일 업로드 실패: " + e.getMessage());
        }
    }

    public String readFile(String fileName) {
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }
}
