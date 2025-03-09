package com.example.onculture.domain.event.repository;

import com.example.onculture.domain.event.domain.PopupStorePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
@Repository
public interface PopupStorePostRepository extends JpaRepository<PopupStorePost, Long>, JpaSpecificationExecutor<PopupStorePost> {

    @Query(value = "SELECT * FROM popup_store_post WHERE status = '진행중' ORDER BY RAND() LIMIT :randomSize", nativeQuery = true)
    List<PopupStorePost> findRandomPopupStorePosts(int randomSize);

    Page<PopupStorePost> findAll(Specification<PopupStorePost> spec, Pageable pageable);

    List<PopupStorePost> findByStatusAndPopupsStartDateLessThanEqualAndPopupsEndDateGreaterThanEqual(String status, Date start, Date end);

    List<PopupStorePost> findByPopupsEndDateLessThan(Date date);
}