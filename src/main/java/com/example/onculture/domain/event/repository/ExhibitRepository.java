package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.ExhibitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExhibitRepository extends JpaRepository<ExhibitEntity, Long> {

    // 기간별 조회 (시작일과 종료일 사이에 해당하는 공연)
    List<ExhibitEntity> findByStartDateGreaterThanEqualAndEndDateLessThanEqual(String from, String to);

    // 지역별 조회 (area 혹은 place를 활용)
    List<ExhibitEntity> findByAreaAndStartDateGreaterThanEqualAndEndDateLessThanEqual(String area, String from, String to);

    // 분야별 조회 (realmName 혹은 별도의 분류코드)
    List<ExhibitEntity> findByRealmNameAndStartDateGreaterThanEqualAndEndDateLessThanEqual(String realmName, String from, String to);
}
