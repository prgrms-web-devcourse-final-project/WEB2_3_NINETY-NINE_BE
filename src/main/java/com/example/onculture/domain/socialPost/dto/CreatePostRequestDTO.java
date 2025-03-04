package com.example.onculture.domain.socialPost.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreatePostRequestDTO {
    private String title;
    private String content;
    private List<String> imageUrls;
}
