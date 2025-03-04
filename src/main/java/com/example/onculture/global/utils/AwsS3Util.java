package com.example.onculture.global.utils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
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

    // S3에 파일 저장
    public void uploadFile(MultipartFile file, String fileName) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        try {
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), metadata);
        } catch (IOException e) {
            System.out.println("[에러] 파일 업로드 실패: " + e.getMessage());
            throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
        }
    }

    // S3에 있는 파일 주소 조회
    public String readFile(String fileName) {
        if (!amazonS3Client.doesObjectExist(bucket, fileName)) {
            System.err.println("[에러] AWS S3에 해당 파일이 존재하지 않습니다: " + fileName);
            throw new CustomException(ErrorCode.S3_FILE_NOT_FOUND);
        }
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // S3에 있는 파일 삭제
    public boolean deleteFile(String fileName) {
        if (!amazonS3Client.doesObjectExist(bucket, fileName)) {
            System.err.println("[에러] AWS S3에 해당 파일이 존재하지 않습니다: " + fileName);
            throw new CustomException(ErrorCode.S3_FILE_NOT_FOUND);
        }

        try {
            amazonS3Client.deleteObject(bucket, fileName);
            return true;
        } catch (AmazonServiceException e) {
            System.err.println("[에러] AWS S3 파일 삭제 실패: " + e.getMessage());
            throw new CustomException(ErrorCode.S3_DELETE_FAILED);
        }
    }
}
