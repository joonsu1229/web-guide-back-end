package com.webguide.search.repository;

import com.webguide.search.entity.NoticeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeCategoryRepository extends JpaRepository<NoticeCategory, Long> {

}
