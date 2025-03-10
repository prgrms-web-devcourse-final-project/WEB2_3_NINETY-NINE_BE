package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.Performance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long>, JpaSpecificationExecutor<Performance> {
    @Query(value = "SELECT * FROM performance WHERE performance_state = '진행중' ORDER BY RAND() LIMIT :randomSize", nativeQuery = true)
    List<Performance> findRandomPerformances(int randomSize);
    List<Performance> findByPerformanceStateAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String performanceState, Date start, Date end);
    List<Performance> findByEndDateLessThan(Date date);
    @Query("SELECT p.performanceId FROM Performance p")
    List<String> findAllPerformanceIds();
}
