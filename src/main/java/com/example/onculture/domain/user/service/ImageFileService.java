package com.example.onculture.domain.user.service;

import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageFileService {

    // 허용된 확장자 목록
    private static final Set<String> ALLOW_IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".svg");
    // 허용된 Mime 타입 목록
    private static final Set<String> ALLOW_IMAGE_MIME_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml");

    // 이미지 파일 유효성 검사 및 파일명 변경
    public String checkFileExtensionAndRename(MultipartFile file, String email) {
        String fileName = file.getOriginalFilename();

        String ext = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        String mimeType = file.getContentType();

        // 업로드된 파일의 확장자가 허용된 확장자가 아니면 예외 발생
        if( !ALLOW_IMAGE_EXTENSIONS.contains(ext) ) throw new CustomException(ErrorCode.FILE_EXTENSION_NOT_ALLOWED);
        if( !ALLOW_IMAGE_MIME_TYPES.contains(mimeType) ) throw new CustomException(ErrorCode.FILE_MIME_TYPE_NOT_ALLOWED);

        // 유니크 속성인 이메일을 추가
//        String dataPath = new SimpleDateFormat("yyyyMMdd").format(new Date());
        fileName = email + "/" + UUID.randomUUID() + ext;

        return fileName;
    }
}
