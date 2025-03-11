package com.example.onculture.domain.user.service;

import com.example.onculture.domain.event.domain.Bookmark;
import com.example.onculture.domain.event.dto.BookmarkEventListDTO;
import com.example.onculture.domain.event.dto.EventResponseDTO;
import com.example.onculture.domain.event.repository.BookmarkRepository;
import com.example.onculture.domain.socialPost.domain.SocialPost;
import com.example.onculture.domain.socialPost.dto.PostResponseDTO;
import com.example.onculture.domain.socialPost.dto.PostWithLikeResponseDTO;
import com.example.onculture.domain.socialPost.dto.UserPostListResponseDTO;
import com.example.onculture.domain.socialPost.repository.SocialPostLikeRepository;
import com.example.onculture.domain.socialPost.repository.SocialPostRepository;
import com.example.onculture.domain.user.domain.Profile;
import com.example.onculture.domain.user.dto.request.ModifyRequestDTO;
import com.example.onculture.domain.user.dto.response.LikedSocialPostIdsResponseDto;
import com.example.onculture.domain.user.dto.response.TokenResponse;
import com.example.onculture.domain.user.dto.response.UserListResponse;
import com.example.onculture.domain.user.dto.response.UserProfileResponse;
import com.example.onculture.domain.user.model.Interest;
import com.example.onculture.domain.user.model.LoginType;
import com.example.onculture.domain.user.model.Role;
import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.request.LoginRequestDTO;
import com.example.onculture.domain.user.dto.request.SignupRequestDTO;
import com.example.onculture.domain.user.repository.ProfileRepository;
import com.example.onculture.domain.user.repository.UserRepository;
import com.example.onculture.global.exception.CustomException;
import com.example.onculture.global.exception.ErrorCode;
import com.example.onculture.global.utils.S3.S3Service;
import com.example.onculture.global.utils.CookieUtil;
import com.example.onculture.global.utils.S3.S3Service;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import com.example.onculture.global.utils.jwt.JwtTokenProvider;
import jakarta.persistence.Tuple;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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
    private final S3Service s3Service; // 수정
    private final SocialPostLikeRepository socialPostLikeRepository;
    private final SocialPostRepository socialPostRepository;
    private final BookmarkRepository bookmarkRepository;

    // 회원가입 메서드
    @Transactional
    public void save(SignupRequestDTO dto) {
        String email = dto.getEmail().trim();
        String nickname = dto.getNickname().trim();

        // 중복 이메일 검증 로직 추가
        if (userRepository.existsByEmail(email)) throw new CustomException.DuplicateEmailException();     // 커스텀한 중복 이메일 예외
        // 중복 닉네임 검증 로직 추가
        if (userRepository.existsByNickname(nickname)) throw new CustomException.DuplicateNicknameException();      // 커스텀한 중복 닉네임 예외

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
        tokenService.addAllTokenToCookie(request, response, accessToken, refreshToken);

        // 테스트용 반환 DTO 및 응답 형식
        return new TokenResponse(accessToken, refreshToken);
    }

    // 로그아웃 메서드
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // request를 통해 쿠키에서 refreshToken 가져오기
        String refreshToken = extractRefreshToken(request);
        // refreshToken 쿠키가 있을 경우, DB에서 삭제
        if (refreshToken != null) tokenService.deleteRefreshToken(refreshToken);
        // 클라이언트 쿠키에서 모든 토큰 삭제 (Set-Cookie 헤더 사용)
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.deleteCookie(request, response, ACCESS_TOKEN_COOKIE_NAME);
    }

    // 재발급 메서드
    public String refreshToken(HttpServletRequest request, HttpServletResponse response) {
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

        return accessToken;
    }

    // 닉네임 중복 여부 메서드
    public boolean checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // UserId 기반 사용자 프로필 정보 조회 메서드
    @Transactional
    public UserProfileResponse getUserProfile(Long userId) {

        User user = findUserAndProfileByuserId(userId);
        Profile profile = user.getProfile();

        Set<String> interests = new HashSet<>();
        if (profile.getInterests() != null) {
            interests = profile.getInterests().stream()
                    .map(Interest::getKor)
                    .collect(Collectors.toSet());
        }

        String s3ImageFileUrl = "";
        if (profile.getProfileImage() != null && !profile.getProfileImage().isEmpty()) {
            // S3에서 실제 프로필 이미지 URL 가져오기 (예: S3 프리사인 URL 생성 방식 / 사용 보류)
//            s3ImageFileUrl = s3Service.readFile("profiles",user.getProfile().getProfileImage());
            // S3 이미지 파일 주소를 DB에 저장함에 따라 profileImage 필드 값을 그대로 반환
            s3ImageFileUrl = profile.getProfileImage();
        }

        return UserProfileResponse.builder()
                .id(userId)
                .nickname(user.getNickname())
                .loginType(user.getLoginType())
                .description(profile.getDescription() != null ? profile.getDescription() : "")
                .interests(interests)
                .profileImage(profile.getProfileImage() != null ? profile.getProfileImage() : "")
                .s3Bucket(s3ImageFileUrl) // 필요하면 S3 버킷 이름 설정
                .build();
    }

    // 현재 사용자 정보 수정 메서드
    @Transactional
    public void modifyUserProfile(Long userId, ModifyRequestDTO dto, MultipartFile imageData) {
        User user = findUserAndProfileByuserId(userId);

        if (dto.getNickname() != null && !dto.getNickname().trim().isEmpty()) {
            if (userRepository.existsByNickname(dto.getNickname().trim())) {
                throw new CustomException.DuplicateNicknameException();
            } else {
                user.setNickname(dto.getNickname().trim());
            }
        }

        // 비밀번호 업데이트 (로컬 회원가입 사용자이고 비밀번호 입력했으면 비밀번호 수정)
        if (!user.getLoginType().equals(LoginType.SOCIAL_ONLY) && dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword().trim()));
        }

        // 소개글 업데이트 (필요 시)
        if (dto.getDescription() != null && !dto.getDescription().trim().isEmpty()) user.getProfile().setDescription(dto.getDescription().trim());
        // 관심사 업데이트 (필요 시)
        if (dto.getInterests() != null && !dto.getInterests().isEmpty()) {
            // Set<한글>을 Set<ENUM>으로 변환 (stream 방식)
            user.getProfile().setInterests(
                    dto.getInterests().stream()
                            .map(Interest::getInterestByKor)
                            .collect(Collectors.toSet())
            );
        }

        // 이미지 파일명 업데이트 (필요 시)
        user.getProfile().setProfileImage(profileImageSave(dto, imageData, user));

        // 변경사항 저장
        userRepository.save(user);
    }

    // 프로필 이미지 파일 S3 저장 및 파일명 반환 메서드 ( profileImage 필드가 s3 Url를 갖는 방식으로 변환 )
    public String profileImageSave(ModifyRequestDTO dto, MultipartFile imageData, User user) {
        String dbUserProfileImageName = user.getProfile().getProfileImage();
        String folder = "profiles";

        // 수정
        // 새로운 이미지가 업로드된 경우
        if (imageData != null && !imageData.isEmpty()) {
            deleteOldProfileImage(folder, dbUserProfileImageName);
            String imageFileName = imageFileService.checkFileExtensionAndRename(imageData, user.getEmail());
            imageFileName = s3Service.uploadFile(imageData, folder, imageFileName);     // 생성한 s3 URL를 profileImage 필드에 그대로 저장
            return imageFileName;
        }

        // 기존 프로필 이미지 유지하는 경우
        if (dto.getProfileImage() != null && !dto.getProfileImage().trim().isEmpty()) {

            if (!dto.getProfileImage().equals(dbUserProfileImageName)) {
                System.out.println("[에러] 기존 프로필 이미지 유지 실패");
                throw new CustomException(ErrorCode.S3_FILE_NOT_FOUND);
            }

            return dto.getProfileImage();
        }

        // 수정
        // 프로필 이미지 삭제하는 경우
        deleteOldProfileImage(folder, dbUserProfileImageName);
        return "";
    }

    // 프로필 이미지 파일 삭제 메서드
    private void deleteOldProfileImage(String folder, String profileImageName) {
        if (profileImageName != null && !profileImageName.trim().isEmpty()) {

            String imageFileName = extractTargetPath(profileImageName);
            System.out.println("imageFileName  : " + imageFileName);

            s3Service.deleteFile(folder, imageFileName);
        }
    }

    // 프로필 이미지 파일 S3 URL에서 파일명 추출
    private String extractTargetPath(String urlString) {
        try {
            URL url = new URL(urlString);
            String path = url.getPath(); // URL의 경로 추출

            // "profiles/" 이후의 부분만 추출
            int index = path.indexOf("profiles/");
            if (index != -1) {
                return path.substring(index + 9); // "profiles/" 길이만큼 제외
            }
        } catch (Exception e) {
            System.out.println("[에러]" + e.getMessage());
        }
        return "";
    }

    // userId 기반 User 조회 및 Profile 존재 여부에 따른 처리
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
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());

        // 사용자를 인증 ( 비밀번호 검증 포함 / 내부적으로 UserDetailsService의 loadUserByUsername()을 호출 )
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

    // 회원 삭제 메서드
    @Transactional
    public void deleteUser(Long userId) {
        // 사용자 정보 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException.CustomJpaReadException(ErrorCode.USER_NOT_FOUND));

        // S3 삭제용 프로필 이미지 파일명 가져오기 (프로필이 존재하지 않을 경우도 고려)
        String profileImageName = user.getProfile() != null ? user.getProfile().getProfileImage() : null;

        // 사용자 삭제
        userRepository.delete(user);
        userRepository.flush();  // 즉시 DELETE SQL 실행

        // 회원 삭제 성공 후, S3 이미지 삭제 실행
        deleteOldProfileImage("profiles", profileImageName);
    }

    // 모든 사용자 조회 메서드 (모든 사용자의 userID와 Email만 반환)
    public List<UserListResponse> findUserList(int count) {
        Pageable pageable = PageRequest.of(0, count);
        List<Tuple> list = userRepository.findUserList(pageable);

        System.out.println("list: " + list);

        return list.stream()
                .map(tuple -> {
                    Long id = tuple.get("id", Long.class);
                    String email = tuple.get("email", String.class);
                    // UserListResponse 객체를 직접 생성하고 값 할당
                    return new UserListResponse(id, email);
                })
                .collect(Collectors.toList());
    }

    // 지정된 사용자의 profileImage 필드 초기화(빈값) 처리 메서드 (관리자용)
    @Transactional
    public void deleteProfileImageByEmail(String email) {

        // 사용자 정보 여부 확인
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException.CustomJpaReadException(ErrorCode.USER_NOT_FOUND));

        Profile profile = user.getProfile();
        if (profile == null) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        profile.setProfileImage(""); // 변경 감지로 자동 반영됨
        log.info("사용자 {}의 프로필 이미지가 초기화되었습니다.", email);
    }

//    // 사용자가 좋아요를 누른 소셜 게시판 ID 목록 조회
//    public LikedSocialPostIdsResponseDto getLikedSocialPosts(Long userId) {
//        if (!userRepository.existsById(userId)) {
//            throw new CustomException(ErrorCode.USER_NOT_FOUND);
//        }
//
//        List<Long> ids = socialPostLikeRepository.findSocialPostIdByUserId(userId);
//
//        return new LikedSocialPostIdsResponseDto(ids);
//    }

    // 사용자가 작성한 소셜 게시판 목록 조회
    public UserPostListResponseDTO getSocialPostsByUser(Long userId, int pageNum, int pageSize, Long loginUserId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        if (pageNum < 0 || pageSize < 0) {
            throw new CustomException(ErrorCode.INVALID_PAGE_REQUEST);
        }


        Pageable pageable = PageRequest.of(pageNum, pageSize, Sort.by("createdAt").descending());
        Page<SocialPost> socialPostsPage = socialPostRepository.findByUserId(userId, pageable);

        List<Long> socialPostIds = socialPostsPage.getContent().stream()
                .map(SocialPost::getId)
                .collect(Collectors.toList());

        Set<Long> likedPostIds = loginUserId != null
                ? new HashSet<>(socialPostLikeRepository.findSocialPostIdsByUserIdAndSocialPostIds(loginUserId, socialPostIds))
                : Collections.emptySet();

        Page<PostWithLikeResponseDTO> posts = socialPostsPage.map(socialPost -> {
            boolean likeStatus = likedPostIds.contains(socialPost.getId());
            return new PostWithLikeResponseDTO(socialPost, likeStatus);
        });

        return UserPostListResponseDTO.builder()
                .posts(posts.getContent())
                .totalPages(posts.getTotalPages())
                .pageNum(posts.getNumber())
                .pageSize(posts.getSize())
                .totalElements(posts.getTotalElements())
                .numberOfElements(posts.getNumberOfElements())
                .build();
    }

    public BookmarkEventListDTO getBookmarkedEvents(Long userId, Pageable pageable) {
        Page<Bookmark> bookmarkPage = bookmarkRepository.findAllByUserId(userId, pageable);

        Page<EventResponseDTO> eventPage = bookmarkPage.map(bookmark -> {
            if (bookmark.getPerformance() != null) {
                return new EventResponseDTO(bookmark.getPerformance(), true);
            } else if (bookmark.getExhibitEntity() != null) {
                return new EventResponseDTO(bookmark.getExhibitEntity(), true);
            } else if (bookmark.getFestivalPost() != null) {
                return new EventResponseDTO(bookmark.getFestivalPost(), true);
            } else if (bookmark.getPopupStorePost() != null) {
                return new EventResponseDTO(bookmark.getPopupStorePost(), true);
            } else {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        });

        BookmarkEventListDTO response = new BookmarkEventListDTO();
        response.setPosts(eventPage.getContent());
        response.setTotalPages(eventPage.getTotalPages());
        response.setTotalElements(eventPage.getTotalElements());
        response.setPageNum(eventPage.getNumber());
        response.setPageSize(eventPage.getSize());
        response.setNumberOfElements(eventPage.getNumberOfElements());

        return response;
    }
}
