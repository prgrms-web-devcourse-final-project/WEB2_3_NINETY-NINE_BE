package com.example.onculture.domain.user.dto.response;

import com.example.onculture.domain.user.model.Interest;
import com.example.onculture.domain.user.model.LoginType;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private String nickname;
    private LoginType loginType;
    private String description;
    private Set<Interest> interests;
    private String profileImage;
    private String s3Bucket;

    // 빌더 사용 시, null 값 방지 ( Lombok 에서 내부적으로 사용됨 )
    public static class UserProfileResponseBuilder {
        private String description = "";
        private Set<Interest> interests = new HashSet<>();
        private String profileImage = "";
        private String s3Bucket = "";
    }
}
