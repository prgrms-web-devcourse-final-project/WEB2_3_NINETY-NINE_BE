package com.example.onculture.global.utils.S3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.imageio.ImageIO;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

	private final AmazonS3 amazonS3;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	// 단일 파일 업로드 (프로필 사진, 후기)
	public String uploadFile(MultipartFile file, String folder, String fileName) {
		String fullPath = folder + "/" + fileName;

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());

		try {
			amazonS3.putObject(bucket, fullPath, file.getInputStream(), metadata);
		} catch (IOException e) {
			throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
		}

		return amazonS3.getUrl(bucket, fullPath).toString(); // 업로드된 파일의 URL 반환
	}

	public List<String> uploadFiles(List<MultipartFile> files, String folder) {
		List<String> fileUrls = new ArrayList<>();
		List<String> uploadedFileNames = new ArrayList<>();

		try {
			for (MultipartFile file : files) {
				if (file != null && !file.isEmpty()) {
					String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
					String fileUrl = uploadFile(file, folder, fileName);
					fileUrls.add(fileUrl);
					uploadedFileNames.add(folder + "/" + fileName);

					log.info("✅ 업로드된 이미지 URL: {}", fileUrl);
				}
			}
		} catch (Exception e) {
			log.error("❌ 파일 업로드 실패: {}", e.getMessage());
			for (String fileName : uploadedFileNames) {
				amazonS3.deleteObject(bucket, fileName);
				log.info("🗑 업로드 실패로 삭제된 파일: {}", fileName);
			}
			throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
		}

		log.info("🚀 최종 반환되는 이미지 URL 리스트: {}", fileUrls);
		return fileUrls;
	}

	public String uploadFileFromUrl(String imageUrl, String folder, String fileName) {
		try {
			BufferedImage image = ImageIO.read(new URL(imageUrl));
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(image, "jpg", os);
			byte[] byteArray = os.toByteArray();

			InputStream inputStream = new ByteArrayInputStream(byteArray);

			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(byteArray.length);
			metadata.setContentType("image/jpeg");

			String fullPath = folder + "/" + fileName;
			amazonS3.putObject(bucket, fullPath, inputStream, metadata);

			return amazonS3.getUrl(bucket, fullPath).toString();
		} catch (Exception e) {
			throw new CustomException(ErrorCode.S3_UPLOAD_FAILED);
		}
	}



	// S3에 있는 파일 주소 조회
	public String readFile(String folder, String fileName) {
		String fullPath = folder + "/" + fileName;

		if (!amazonS3.doesObjectExist(bucket, fullPath)) {
			log.warn("⚠️ AWS S3에 해당 파일이 존재하지 않음: {}", fullPath);
			throw new CustomException(ErrorCode.S3_FILE_NOT_FOUND);
		}

		return amazonS3.getUrl(bucket, fullPath).toString();
	}


	public boolean deleteFile(String folder, String fileName) {
		String fullPath = folder + "/" + fileName;

		if (!amazonS3.doesObjectExist(bucket, fullPath)) {
			throw new CustomException(ErrorCode.S3_FILE_NOT_FOUND);
		}

		try {
			amazonS3.deleteObject(bucket, fullPath);
			return true;
		} catch (AmazonServiceException e) {
			throw new CustomException(ErrorCode.S3_DELETE_FAILED);
		}
	}

	public boolean deleteFolder(String folder) {
		ObjectListing objectListing = amazonS3.listObjects(bucket, folder);

		if (objectListing.getObjectSummaries().isEmpty()) {
			return true; // 폴더가 이미 비어있다면 그냥 true 반환
		}

		try {
			for (S3ObjectSummary file : objectListing.getObjectSummaries()) {
				amazonS3.deleteObject(bucket, file.getKey());
			}
			return true;
		} catch (AmazonServiceException e) {
			throw new CustomException(ErrorCode.S3_DELETE_FAILED);
		}
	}

	public boolean doesFileExist(String folder, String fileName) {
		String fullPath = folder + "/" + fileName;
		return amazonS3.doesObjectExist(bucket, fullPath);
	}

	public String getFileUrl(String folder, String fileName) {
		String fullPath = folder + "/" + fileName;
		return amazonS3.getUrl(bucket, fullPath).toString();
	}





}

