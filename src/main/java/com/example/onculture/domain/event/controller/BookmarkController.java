package com.example.onculture.domain.event.controller;

import com.example.onculture.domain.event.service.BookmarkService;
import com.example.onculture.global.response.SuccessResponse;
import com.example.onculture.global.utils.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @Operation(summary = "공연 게시글 좋아요 토글",
            description = "북마크를 누른 유저가 요청하면 삭제, 누르지 않은 유저가 요청하면 추가됩니다.")
    @PostMapping("/api/events/{eventPostId}/bookmarks")
    public ResponseEntity<SuccessResponse<String>> toggleBookmark(
            @PathVariable Long eventPostId,
            @RequestParam String genre,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String result = bookmarkService.toggleBookmark(userDetails.getUserId(), eventPostId, genre);

        return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.success(HttpStatus.OK, result));
    }
}

