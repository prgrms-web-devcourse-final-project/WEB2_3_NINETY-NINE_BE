package com.example.onculture.domain.user.service;

import com.example.onculture.domain.socialPost.dto.PostResponseDTO;
import com.example.onculture.domain.socialPost.dto.UserPostListResponseDTO;
import com.example.onculture.domain.socialPost.repository.SocialPostLikeRepository;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.domain.user.domain.Profile;
import com.example.onculture.domain.user.dto.request.ModifyRequestDTO;
import com.example.onculture.domain.user.dto.response.LikedSocialPostIdsResponseDto;
import com.example.onculture.domain.user.dto.response.TokenResponse;
import com.example.onculture.domain.user.dto.response.UserProfileResponse;
import com.example.onculture.domain.user.model.Interest;
import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.model.Social;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.dto.response.UserSimpleResponse;
import com.example.onculture.domain.user.repository.ProfileRepository;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.example.onculture.global.utils.AwsS3Util;
import com.example.onculture.global.utils.CookieUtil;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.example.onculture.global.utils.CookieUtil.*;

@Slf4j
@RequiredArgsConstructor    // final 필드나 @NonNull이 붙은 필드를 파라미터로 받는 생성자를 자동으로 생성
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final ImageFileService imageFileService;
    private final ModelMapper modelMapper;
    private final ProfileRepository profileRepository;
    private final AwsS3Util awsS3Util;
    private final SocialPostLikeRepository socialPostLikeRepository;
    private final SocialPostRepository socialPostRepository;

    // 회원가입 메서드
    @Transactional
    public void save(SignupRequestDTO dto) {
        System.out.println("dto: " + dto);

        String email = dto.getEmail();
        String nickname = dto.getNickname();

        // 중복 이메일 검증 로직 추가
        // isPresent() : Optional 객체에서 제공하는 메서드로, 해당 Optional 객체가 값을 가지고 있는지 여부를 확인하는 메서드 ( 값이 있을 경우 True )
        if (userRepository.findByEmail(email).isPresent())
            throw new CustomException.DuplicateEmailException();     // 커스텀한 중복 이메일 예외
        // 중복 닉네임 검증 로직 추가
        if (userRepository.findByNickname(nickname).isPresent())
            throw new CustomException.DuplicateNicknameException();        // 커스텀한 중복 닉네임 예외
        // 동일한 이메일의 소셜 가입자가 있을 경우 고려 ( 보류 )
            /*
            // 이미 등록된 사용자 확인
            Optional<User> existingUser = userRepository.findByEmail(email);

            // 기존에 해당 이메일이 등록된 경우
            if (existingUser.isPresent()) {
                // 기존 사용자에 대해 socialUpdate 호출
                User user = existingUser.get();
                user.socialUpdate(Social.LOCAL, user.getSocials());  // 소셜 로그인 정보 업데이트
                return userRepository.save(user);
            }
             */

        try {
            // DTO -> Entity 변환
            User user = modelMapper.map(dto, User.class);
            // 비밀번호 암호화
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            // 회원가입 처리
            userRepository.save(user);

        } catch (Exception e) {
            log.error("사용자 저장 실패: {}", e.getMessage());
            throw new CustomException.CustomJpaSaveException(ErrorCode.USER_SAVE_FAILED);
        }
    }

    // 로컬 로그인 메서드
    public TokenResponse login(LoginRequestDTO dto, HttpServletRequest request, HttpServletResponse response) {
        // 사용자 인증 메서드 실행 및 인증 객체 반환
        Authentication authentication = authenticate(dto);
        // 인증 객체에서 사용자 데이터 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();
        String email = userDetails.getEmail();
        Role role = userDetails.getRole();

        // 액세스 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(userId, email, role);
        // 리프레시 토큰 생성 및 DB 저장
        String refreshToken = tokenService.createRefreshToken(userId);
        // 액세스 토큰 및 리프레시 토큰을 쿠키에 저장
        TokenService.addAllTokenToCookie(request, response, accessToken, refreshToken);

        // 테스트용 반환 DTO 및 응답 형식
        return new TokenResponse(accessToken, refreshToken);
    }

    // 로그아웃 메서드
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // request를 통해 쿠키에서 refreshToken 가져오기
        String refreshToken = extractRefreshToken(request);
        // refreshToken 쿠키가 있을 경우, DB에서 삭제
        if (refreshToken != null) tokenService.deleteRefreshToken(refreshToken);
        // 클라이언트 쿠키에서 RefreshToken 삭제 (Set-Cookie 헤더 사용)
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
    }

    // 재발급 메서드
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        // request를 통해 쿠키에서 refreshToken 가져오기
        String refreshToken = extractRefreshToken(request);
        // 리프레시 토큰 만료로 없을 경우, 재로그인 요청 메세지 반환
        if (refreshToken == null)
            throw new CustomException.CustomInvalidTokenException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        // 리프레시 토큰으로 액세스 토큰 재발급
        String accessToken = tokenService.createAccessTokenFromRefreshToken(refreshToken);
        log.info("재발급된 액세스 토큰 : " + accessToken);
        // 쿠키에 액세스 토큰 저장
        CookieUtil.addCookie(response, ACCESS_TOKEN_COOKIE_NAME, accessToken, ACCESS_TOKEN_COOKIE_DURATION);
    }

    // 닉네임 중복 여부 메서드
    public Boolean checkNickname(String nickname) {
        return userRepository.findByNickname(nickname).isPresent();
    }

    // 현재 사용자 정보 조회 메서드
    @Transactional
    public UserProfileResponse getUserProfile(Long userId) {

        User user = findUserAndProfileByuserId(userId);
        Profile profile = user.getProfile();

        String s3ImageFileUrl = "";
        if (user.getProfile().getProfileImage() != null && !user.getProfile().getProfileImage().isEmpty()) {
            // S3에서 실제 프로필 이미지 URL 가져오기 (예: S3 프리사인 URL 생성 방식)
            s3ImageFileUrl = awsS3Util.readFile(user.getProfile().getProfileImage());
        }

        return UserProfileResponse.builder()
                .nickname(user.getNickname())
                .loginType(user.getLoginType())
                .description(profile.getDescription() != null ? profile.getDescription() : "")
                .interests(profile.getInterests() != null ? profile.getInterests() : new HashSet<>())
                .profileImage(profile.getProfileImage() != null ? profile.getProfileImage() : "")
                .s3Bucket(s3ImageFileUrl) // 필요하면 S3 버킷 이름 설정
                .build();
    }

    // 현재 사용자 정보 수정 메서드
    @Transactional
    public void modifyUserProfile(Long userId, ModifyRequestDTO dto, MultipartFile imageData) {
        User user = findUserAndProfileByuserId(userId);

        // 필수 입력값이 닉네임이 없을 경우
        if (dto.getNickname() == null || dto.getNickname().trim().isEmpty()) throw new CustomException(ErrorCode.NICKNAME_CANNOT_BE_NULL);
        // 닉네임 업데이트
        user.setNickname(dto.getNickname().trim());

        // 비밀번호 업데이트 (필요 시)
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) user.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        // 소개글 업데이트 (필요 시)
        if (dto.getDescription() != null && !dto.getDescription().trim().isEmpty()) user.getProfile().setDescription(dto.getDescription().trim());
        // 관심사 업데이트 (필요 시)
        if (dto.getInterests() != null && !dto.getInterests().isEmpty()) user.getProfile().setInterests(dto.getInterests());
        // 이미지 파일명 업데이트 (필요 시)
        if (imageData != null && !imageData.isEmpty()) {
            // 이미지 파일 유효성 검사 및 파일명 변경
            String newImageFileName = imageFileService.checkFileExtensionAndRename(imageData, user.getEmail());
            // DB에 이미지 파일명 저장
            user.getProfile().setProfileImage(newImageFileName);
            awsS3Util.uploadFile(imageData, newImageFileName);

        } else {
            if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
                // 기존 이미지 유지 시, 기존 이미지 파일명 저장
                user.getProfile().setProfileImage(dto.getProfileImage());
            } else {
                // 프로필 이미지 삭제 시, DB에 빈값 처리
                user.getProfile().setProfileImage("");
            }
        }

        // 변경사항 저장
        userRepository.save(user);
    }

    // 이메일 기반 User 조회 및 Profile 존재 여부에 따른 처리
    @Transactional
    public User findUserAndProfileByuserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException.CustomJpaReadException(ErrorCode.USER_NOT_FOUND));

        Profile profile = user.getProfile();

        if (profile == null) {
            profile = Profile.builder()
                    .user(user)
                    .description("")
                    .profileImage("")
                    .interests(new HashSet<>())
                    .build();
            profileRepository.save(profile);
            user.setProfile(profile);
        }

        return user;
    }

    // 로그인 인증 후 인증 객체 반환 메서드
    public Authentication authenticate(LoginRequestDTO dto) {

        // 인증 객체 생성 ( 아직 인증된 객체는 아님 )
        // authenticate 내부 구조
        /*
        // principal (사용자 정보) = 사용자 이메일
        // credentials (비밀번호) = 입력한 비밀번호
        // authorities (권한 정보) = 권한 정보
        // authenticated (인증 여부) = false
         */
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());

        // 사용자를 인증 ( 비밀번호 검증 포함 / 내부적으로 UserDetailsService의 loadUserByUsername()을 호출 )
        // authenticate 내부 구조
        /*
         principal (사용자 정보) = DB에서 가져온 사용자 객체
         credentials (비밀번호) = 보안상 제거됨 ( Null )
         authorities (권한 정보) = [ROLE_USER] ( 사용자의 권한 목록 )
         authenticated (인증 여부) = true
         */
        return authenticationManager.authenticate(authenticationToken);
    }

    // request 에 있는 쿠키에서 refreshToken 값 가져오기
    public String extractRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refreshToken")) {
                    String value = cookie.getValue();
                    // 쿠키에 있던 refreshToken가 null 및 빈값일 경우 null 로 반환
                    return (value != null && !value.trim().isEmpty()) ? value : null;
                }
            }
        }
        return null;
    }

    // 사용자가 좋아요를 누른 소셜 게시판 ID 목록 조회
    public LikedSocialPostIdsResponseDto getLikedSocialPosts(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        List<Long> ids = socialPostLikeRepository.findSocialPostIdByUserId(userId);

        return new LikedSocialPostIdsResponseDto(ids);
    }

    // 사용자가 작성한 소셜 게시판 목록 조회
    public UserPostListResponseDTO getSocialPostsByUser(Long userId, int pageNum, int pageSize) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if (pageNum < 0 || pageSize < 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }


        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdAt").descending());
        Page<PostResponseDTO> posts = socialPostRepository.findByUserId(userId, pageable).map(PostResponseDTO::new);

        return UserPostListResponseDTO.builder()
                .posts(posts.getContent())
                .totalPages(posts.getTotalPages())
                .pageNum(posts.getNumber())
                .pageSize(posts.getSize())
                .totalElements(posts.getTotalElements())
                .numberOfElements(posts.getNumberOfElements())
                .build();
    }
}
