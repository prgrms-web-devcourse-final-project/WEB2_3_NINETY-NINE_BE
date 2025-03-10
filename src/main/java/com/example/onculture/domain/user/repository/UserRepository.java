package com.example.onculture.domain.user.repository;

import com.example.onculture.domain.user.domain.User;
import com.example.onculture.domain.user.dto.response.UserListResponse;
import com.example.onculture.domain.user.dto.response.UserProfileResponse;
import jakarta.persistence.Tuple;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // email로 사용자 정보를 가져옴
    // Optional : 결과값이 null일 경우, 빈 값으로 치환
    Optional<User> findByEmail(String email);

    // 닉네임으로 유저를 찾는 쿼리 ( 네임드 기반 파라미터 방식 - 사용 O )
    @Query(value = "select u from User u where u.nickname = :nickname")
    Optional<User> findByNickname(@Param("nickname") String nickname);

    boolean existsByNickname(String nickname);

    boolean existsByEmail(String email);

    @Query(value = "select u.id as id, u.email as email from User u")
    List<Tuple> findUserList(Pageable pageable);
}
