package com.example.onculture.domain.user.repository;

import com.example.onculture.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // email로 사용자 정보를 가져옴
    // Optional : 결과값이 null일 경우, 빈 값으로 치환
    Optional<User> findByEmail(String email);
}
