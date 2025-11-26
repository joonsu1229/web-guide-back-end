package com.webguide.search.repository;

import com.webguide.search.dto.NoticeSearchResult;
import com.webguide.search.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NoticeSearchRepository extends JpaRepository<Notice, Long> {

    // -------------------------------------------------------------------------
    // 1) 전체 검색 (키워드 없으면 전체 공지, 있으면 PGroonga 검색)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    @Query(value = """
        SELECT
            n.notice_id AS id,
            n.category AS category,
            n.title AS title,
            n.summary AS summary,
            n.content AS content,
            n.views AS views,
            n.use_yn AS useYn,
            n.is_new AS isNew,
            to_char(n.reg_dt, 'YYYY-MM-DD"T"HH24:MI:SS') AS regDt,
            to_char(n.mod_dt, 'YYYY-MM-DD"T"HH24:MI:SS') AS modDt
        FROM webguide.notice n
        WHERE n.delete_yn = 'N'
          AND n.use_yn = 'Y'
          AND (
                :keyword = '' 
                OR :keyword IS NULL 
                OR (
                     (n.title::text || ' '::text || n.summary::text || ' '::text || n.content::text)
                     &@* :keyword
                   )
              )
        ORDER BY pgroonga_score(n.tableoid, n.ctid) DESC, n.reg_dt DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<NoticeSearchResult> searchAll(
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );

    // -------------------------------------------------------------------------
    // 2) 카테고리 + 검색 (키워드 없으면 해당 카테고리 전체)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    @Query(value = """
        SELECT
            n.notice_id AS id,
            n.category AS category,
            n.title AS title,
            n.summary AS summary,
            n.content AS content,
            n.views AS views,
            n.use_yn AS useYn,
            n.is_new AS isNew,
            to_char(n.reg_dt, 'YYYY-MM-DD"T"HH24:MI:SS') AS regDt,
            to_char(n.mod_dt, 'YYYY-MM-DD"T"HH24:MI:SS') AS modDt
        FROM webguide.notice n
        WHERE n.delete_yn = 'N'
          AND n.use_yn = 'Y'
          AND n.category = :category
          AND (
                :keyword = '' 
                OR :keyword IS NULL 
                OR (
                     (n.title::text || ' '::text || n.summary::text || ' '::text || n.content::text)
                     &@* :keyword
                   )
              )
        ORDER BY pgroonga_score(n.tableoid, n.ctid) DESC, n.reg_dt DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<NoticeSearchResult> searchByCategory(
            @Param("category") String category,
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );

    // -------------------------------------------------------------------------
    // 3) 포털 + 검색 (키워드 없으면 해당 포털 전체)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    @Query(value = """
        SELECT
            n.notice_id AS id,
            n.category AS category,
            n.title AS title,
            n.summary AS summary,
            n.content AS content,
            n.views AS views,
            n.use_yn AS useYn,
            n.is_new AS isNew,
            to_char(n.reg_dt, 'YYYY-MM-DD"T"HH24:MI:SS') AS regDt,
            to_char(n.mod_dt, 'YYYY-MM-DD"T"HH24:MI:SS') AS modDt
        FROM webguide.notice n
        WHERE n.delete_yn = 'N'
          AND n.use_yn = 'Y'
          AND n.portal_id = :portalId
          AND (
                :keyword = '' 
                OR :keyword IS NULL 
                OR (
                     (n.title::text || ' '::text || n.summary::text || ' '::text || n.content::text)
                     &@* :keyword
                   )
              )
        ORDER BY pgroonga_score(n.tableoid, n.ctid) DESC, n.reg_dt DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<NoticeSearchResult> searchByPortal(
            @Param("portalId") String portalId,
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );
}
