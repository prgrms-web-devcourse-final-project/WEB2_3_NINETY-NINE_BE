package com.example.onculture.domain.user.repository;

import com.example.onculture.domain.user.domain.Profile;
import com.example.onculture.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}
