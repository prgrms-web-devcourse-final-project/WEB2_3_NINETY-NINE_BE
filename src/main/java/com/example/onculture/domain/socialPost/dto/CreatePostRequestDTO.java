package com.example.onculture.domain.socialPost.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePostRequestDTO {
    private String title;
    private String content;
    private String imageUrl;
}
