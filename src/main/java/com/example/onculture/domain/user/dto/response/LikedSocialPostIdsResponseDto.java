package com.example.onculture.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;


import java.util.List;

@Getter
@AllArgsConstructor
public class LikedSocialPostIdsResponseDto {
    List<Long> SocialPostIds;
}
