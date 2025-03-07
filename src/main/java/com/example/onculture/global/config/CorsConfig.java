package com.example.onculture.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins("http://localhost:3000", "http://localhost:5173") // 로컬 개발 환경 허용
			.allowedMethods("GET", "POST", "PUT", "DELETE")
			.allowCredentials(true);
	}
}
