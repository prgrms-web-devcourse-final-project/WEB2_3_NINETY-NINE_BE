package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.FestivalPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface FestivalPostRepository extends JpaRepository<FestivalPost, Long>, JpaSpecificationExecutor<FestivalPost> {

    @Query(value = "SELECT * FROM festival_post WHERE festival_status = '진행 예정' ORDER BY RAND() LIMIT :randomSize", nativeQuery = true)
    List<FestivalPost> findRandomFestivalPosts(int randomSize);

    // festivalLocation 필드가 null이 아닌 항목들만 조회 (필요에 따라)
    List<FestivalPost> findByFestivalLocationIsNotNull();

    Page<FestivalPost> findAll(Specification<FestivalPost> spec, Pageable pageable);
}