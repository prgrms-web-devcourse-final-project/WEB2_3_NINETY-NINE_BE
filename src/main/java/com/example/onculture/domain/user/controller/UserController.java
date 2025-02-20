package com.example.onculture.domain.user.controller;

import com.example.onculture.domain.user.domain.Interest;
import com.example.onculture.domain.user.domain.Role;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.ModifyRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.request.TokenRequestDTO;
import com.example.onculture.domain.user.dto.response.UserResponse;
import com.example.onculture.domain.user.dto.response.UserSimpleResponse;
import com.example.onculture.domain.user.service.UserService;
import com.example.onculture.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
@Tag(name = "유저 API", description = "사용자 로그인 및 정보 관리")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    // 성공 응답 생성
    public Map<String, Object> successResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", 200);
        response.put("message", message);
        response.put("data", data);

        return response;
    }

    // 회원가입 API
    @Operation( summary = "회원가입 API", description = "로컬 회원가입 API" )
    @PostMapping("/signup")
    public ResponseEntity<SuccessResponse<Void>> signup(@RequestBody SignupRequestDTO request) {

        Long userId = userService.save(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.success(HttpStatus.CREATED, "회원가입에 성공하였습니다.", null));
    }

    // 로그인 API
    @Operation( summary = "로그인 API", description = "로컬 로그인 API" )
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<String>> login(@RequestBody LoginRequestDTO dto) {

        // 사용자 인증 메서드 실행
        Authentication authentication = userService.authenticate(dto);
        // 인증 데이터 확인용
        System.out.println("Authentication: " + authentication);

        // 인증 성공하면 임시 토큰 반환 (JWT 적용 전)
        String accessToken = UUID.randomUUID().toString();

        return ResponseEntity.ok(SuccessResponse.success("로그인에 성공하였습니다.", accessToken));
    }

    // 로그아웃 API
    @Operation( summary = "로그아웃 API" )
    @GetMapping( "/logout" )
    public ResponseEntity<SuccessResponse<String>> logout(HttpServletRequest request, HttpServletResponse response) {

        // 추후 RefreshToke 삭제 로직 추가 예정

        return ResponseEntity.status(HttpStatus.OK)
                .body(SuccessResponse.success(HttpStatus.OK, "로그아웃 성공", null));
    }

    // 토큰 재발급 Mock API
    @Operation( summary = "토큰 재발급 Mock API", description = "AccessToken 토큰 만료 시, 재발급하는 API" )
    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@RequestBody TokenRequestDTO dto) {
        // 리프레시 토큰이 "valid-refresh-token"이면 새로운 액세스 토큰을 반환
        if ("valid-refresh-token".equals(dto.getRefreshToken())) {
            String newAccessToken = "new-access-token-12345"; // 실제 토큰 생성 로직 대신 임시 토큰
            return ResponseEntity.ok(newAccessToken);
        } else {
            throw new IllegalArgumentException("유효한 재인증 토큰이 아닙니다.");
        }
    }

    // 소셜 로그인 처리 Mock API
    @Operation( summary = "소셜 로그인 처리 Mock API", description = "소셜 API에서 받은 인증 코드 활용하여 사용자 정보 조회" )
    @GetMapping("/social-login")
    public ResponseEntity<Map<String, String>> socialLogin(
            @RequestParam(defaultValue = "default-code") String code,
            HttpServletRequest request, Model model) {

        // 1. 소셜 액세스 토큰 가져오기 (Mock)
        String accessToken = "mock-access-token-12345";  // 실제로는 code를 이용해 액세스 토큰을 발급받아야 합니다.

        // 2. 사용자 정보 가져오기 (Mock)
        String email = "testuser@gmail.com";  // 실제로는 소셜 API에서 사용자 정보를 가져와야 합니다.
        String nickname = "Test User";

        // 3. JWT 토큰 생성 (Mock)
        String jwtToken = "mock-jwt-token-67890";  // 실제 JWT 토큰 발급 로직을 대체

        // 4. 클라이언트에 JWT 토큰 반환
        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("email", email);

        // 헤드에 Location 설정 (리다이렉션을 피하고 상태코드를 OK로 변경)
        return ResponseEntity.ok(response);  // 200 OK 상태로 응답 본문에 토큰과 이메일 포함
    }

    // 소셜 로그인 처리
    /*
    @GetMapping("/social-login")
    public ResponseEntity<Map<String, String>> kakaoLogin(
            @RequestParam String code,
            HttpServletRequest request, Model model) {

        // 1. 소셜 액세스 토큰 가져오기 (Mock)
        String accessToken = "mock-access-token-12345";  // 실제로는 code를 이용해 액세스 토큰을 발급받아야 합니다.

        // 2. 사용자 정보 가져오기 (Mock)
        String email = "testuser@gmail.com";  // 실제로는 소셜 API에서 사용자 정보를 가져와야 합니다.
        String nickname = "Test User";

        // 3. JWT 토큰 생성 (Mock)
        String jwtToken = "mock-jwt-token-67890";  // 실제 JWT 토큰 발급 로직을 대체

        // 4. 클라이언트에 JWT 토큰 반환
        Map<String, String> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("email", email);

        // 헤드에 Location 설정
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "/social-login?token=" + jwtToken)
                .body(response);  // 바디에는 JWT 토큰과 이메일을 포함
    }
     */

    // 로그인한 사용자 정보 반환 Mock API
    @Operation( summary = "유저 전체 정보 조회 Mock API", description = "현재 로그인한 유저의 모든 정보를 반환하는 API" )
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> user(HttpServletRequest request) {

        UserResponse userResponse = UserResponse.builder()
                .email("testuser@gmail.com")
                .nickname("Test User")
                .description("안녕하세요. 테스트 유저입니다.")
                .role(Role.USER)  // Role enum 값 설정
                .createdAt(LocalDateTime.now())
                .interests(List.of(Interest.M, Interest.D, Interest.F, Interest.C))  // Interest enum 값 설정
                .build();

        return ResponseEntity.ok(successResponse("회원정보를 가져오기에 성공했습니다.", userResponse));
    }

    // 로그인한 사용자 일부 정보 반환 Mock API
    @Operation( summary = "유저 일부 정보 조회 Mock API", description = "현재 로그인한 유저의 일부 정보를 반환하는 API" )
    @GetMapping("/user-simple")
    public ResponseEntity<Map<String, Object>> userSimple(HttpServletRequest request) {

        UserSimpleResponse userResponse = UserSimpleResponse.builder()
                .email("testuser@gmail.com")
                .nickname("Test User")
                .build();

        return ResponseEntity.ok(successResponse("회원정보를 가져오기에 성공했습니다.", userResponse));
    }

    // 프로필 이미지 반환 Mock API
    @Operation( summary = "프로필 이미지 조회 Mock API", description = "현재 로그인한 유저의 프로필 이미지를 반환하는 API" )
    @GetMapping("/profile-image")
    public ResponseEntity<Map<String, Object>> getProfileImage() {
        // 프로필 이미지 URL (S3에서 서명된 URL을 반환하는 Mock)
        String profileImageUrl = "https://your-bucket.s3.amazonaws.com/profile/12345.jpg";

        // 응답 데이터 구성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("profileImageUrl", profileImageUrl);

        // S3링크에서 이미지 반환하는 로직 포함

        // 응답 반환 ( 나중에 이미지 자체를 반환하는 응답으로 변경 )
        return ResponseEntity.ok(successResponse("사용자 프로필 이미지를 반환합니다.", responseData));
    }


    // 프로필 이미지 삽입 Mock API
    @Operation( summary = "프로필 이미지 생성 Mock API", description = "현재 로그인한 유저의 프로필 이미지를 삽입하는 API" )
    @PostMapping("/upload-profile-image")
    public ResponseEntity<Map<String, Object>> uploadProfileImage(
            @RequestParam("profileImage") MultipartFile profileImage,
            HttpServletRequest request ) throws IOException {

        // 이미지 파일이 정상적으로 전송되었는지 확인
        if (profileImage.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", 400,
                    "message", "이미지가 제공되지 않았습니다."
            ));
        }

        // 프로필 이미지 파일을 S3에 업로드하는 작업 (Mock)
        // 실제로는 S3 API를 호출하여 파일을 업로드하는 로직을 추가해야 합니다.
        String profileImageUrl = "https://your-bucket.s3.amazonaws.com/profile/" + UUID.randomUUID() + ".jpg";

        // 응답 데이터 구성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("profileImageUrl", profileImageUrl);

        // 응답 반환
        return ResponseEntity.ok(successResponse("사용자 프로필 이미지를 추가했습니다.", responseData));
    }

    // 프로필 이미지 삭제 Mock API
    @Operation( summary = "프로필 이미지 삭제 Mock API", description = "현재 로그인한 유저의 프로필 이미지를 삭제하는 API" )
    @DeleteMapping( "/profile-image" )
    public ResponseEntity<Map<String, Object>> deleteProfileImage( HttpServletRequest request ) {

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "사용자 프로필 이미지를 삭제했습니다."));
    }

    // 비밀번호 수정 Mock API
    @Operation( summary = "비밀번호 수정 Mock API", description = "현재 로그인한 유저의 비밀번호를 수정하는 API" )
    @PostMapping("/update-password")
    public ResponseEntity<Map<String, Object>> updatePassword(@RequestBody Map<String, String> requestBody, HttpServletRequest request ) {
        // 요청 바디에서 비밀번호 추출
        String password = requestBody.get("password");

        // 비밀번호가 존재하는지 확인
        if (password == null || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "status", 400,
                    "message", "비밀번호가 제공되지 않았습니다."
            ));
        }

        // 비밀번호 수정 로직 (Mock)
        // 실제로는 비밀번호를 데이터베이스에 저장하는 로직이 추가

        // 응답 반환
        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "비밀번호가 성공적으로 수정되었습니다."));
    }

    // 사용자 정보 수정 Mock API
    @Operation( summary = "사용자 정보 수정 Mock API", description = "현재 로그인한 유저의 정보를 수정하는 API" )
    @PutMapping( "/user" )
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody ModifyRequestDTO dto, HttpServletRequest request ) {

        // 사용자 데이터 업데이트 로직 실행

        // 응답 반환
        return ResponseEntity.ok(successResponse("사용자 정보가 수정되었습니다.", dto));
    }

    // 사용자 정보 삭제 Mock API
    @Operation( summary = "사용자 정보 삭제 Mock API", description = "현재 로그인한 유저의 정보를 수정하는 API" )
    @DeleteMapping( "/user" )
    public ResponseEntity<Map<String, Object>> deleteUser( HttpServletRequest request ) {

        return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "회원탈퇴 완료"));
    }
}
