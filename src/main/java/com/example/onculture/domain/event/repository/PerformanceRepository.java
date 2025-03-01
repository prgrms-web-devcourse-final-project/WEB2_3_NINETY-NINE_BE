package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    @Query(value = "SELECT * FROM performance WHERE performance_state = '공연중' ORDER BY RAND() LIMIT :randomSize", nativeQuery = true)
    List<Performance> findRandomPerformances(int randomSize);
}
