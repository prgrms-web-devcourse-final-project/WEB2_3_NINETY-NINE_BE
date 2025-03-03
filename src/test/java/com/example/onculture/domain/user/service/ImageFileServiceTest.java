package com.example.onculture.domain.user.service;

import com.example.onculture.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
public class ImageFileServiceTest {

    @InjectMocks
    private ImageFileService imageFileService;

    private MultipartFile validImage;
    private MultipartFile invalidExtensionImage;
    private MultipartFile invalidMimeTypeImage;

    private static final String TEST_EMAIL = "tester2@gmail.com";

    @BeforeEach
    public void setUp() {
        validImage = new MockMultipartFile(
                "profileImage", "testImage.jpg", "image/jpeg", "fake image content".getBytes());
        invalidExtensionImage = new MockMultipartFile(
                "profileImage", "testImage.exe", "image/jpeg", "fake image content".getBytes());
        invalidMimeTypeImage = new MockMultipartFile(
                "profileImage", "testImage.jpg", "application/pdf", "fake image content".getBytes());
    }

    @Test
    @DisplayName("이미지 파일 유효성 검사 및 파일명 변경 - 정상적인 경우")
    void checkFileExtensionAndRename() {
        // When
        String newFileName = imageFileService.checkFileExtensionAndRename(validImage, TEST_EMAIL);

        // Then
        assertNotNull(newFileName);
        assertTrue(newFileName.startsWith(TEST_EMAIL + "/"));
        assertTrue(newFileName.endsWith(".jpg"));

        // 파일명이 UUID 형식인지 확인
        String uuidPart = newFileName.replace(TEST_EMAIL + "/", "").replace(".jpg", "");
        assertDoesNotThrow(() -> UUID.fromString(uuidPart));
    }

    @Test
    @DisplayName("이미지 파일 유효성 검사 - 허용되지 않은 확장자")
    void checkFileExtensionAndRename_invalidExtension() {
        // When & Then
        assertThrows(CustomException.class, () ->
                imageFileService.checkFileExtensionAndRename(invalidExtensionImage, TEST_EMAIL));
    }

    @Test
    @DisplayName("이미지 파일 유효성 검사 - 허용되지 않은 MIME 타입")
    void checkFileExtensionAndRename_invalidMimeType() {
        // When & Then
        assertThrows(CustomException.class, () ->
                imageFileService.checkFileExtensionAndRename(invalidMimeTypeImage, TEST_EMAIL));
    }
}
