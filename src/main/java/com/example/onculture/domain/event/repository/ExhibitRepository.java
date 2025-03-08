package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.ExhibitEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExhibitRepository extends JpaRepository<ExhibitEntity, Long>, JpaSpecificationExecutor<ExhibitEntity> {

     @Query(value = "SELECT * FROM exhibit WHERE exhibit_status = '진행중' ORDER BY RAND() LIMIT :randomSize", nativeQuery = true)
    List<ExhibitEntity> findRandomExhibitions(int randomSize);

    Page<ExhibitEntity> findAll(Specification<ExhibitEntity> spec, Pageable pageable);
}
