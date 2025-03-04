package com.example.onculture.domain.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

//프론트에서 인증 코드 받을 때 사용하는 클래스
/*
@Controller
public class OAuthViewController {

    @GetMapping("/auth/callback")
    public String googleCallback(@RequestParam("code") String code) {
        // 코드로 액세스 토큰을 받고 사용자 정보를 처리한 후

        System.out.println("code: " + code);

        return "redirect:/index.html";  // 인증 후 index.html로 리디렉션
    }
}
 */