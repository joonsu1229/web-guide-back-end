package com.webguide.search.repository;

import com.webguide.search.dto.GuideContentDto;
import com.webguide.search.dto.SearchResult;
import com.webguide.search.entity.GuideVersion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuideVersionRepository extends JpaRepository<GuideVersion, Long> {

    // -------------------
    // 조회용 메서드 (readOnly)
    // -------------------
    @Query("SELECT gv.version FROM GuideVersion gv WHERE gv.guide.id = :guideId ORDER BY gv.version DESC")
    List<Integer> findLatestVersionByGuideId(@Param("guideId") Long guideId, Pageable pageable);


    /**
     * JPQL을 사용하여 특정 Guide의 특정 버전을 조회
     */
    @Transactional(readOnly = true)
    @Query("SELECT gv FROM GuideVersion gv WHERE gv.guide.id = :guideId AND gv.version = :version")
    Optional<GuideVersion> findByGuideIdAndVersion(@Param("guideId") Long guideId, @Param("version") int version);

    /**
     * getVersionHistory()의 N+1 문제 해결을 위한 DTO 프로젝션 메서드
     */
    @Transactional(readOnly = true)
    @Query("SELECT new com.webguide.search.dto.GuideContentDto(gv.id, gv.contentBody, gv.version, gv.guide.category.id) " +
           "FROM GuideVersion gv " +
           "WHERE gv.guide.id = :guideId " +
           "ORDER BY gv.version DESC")
    List<GuideContentDto> findHistoryDtoByGuideId(@Param("guideId") Long guideId);

    @Query(value = """
        SELECT gv.id AS id,
               gv.guide_id AS guideId,
               g.category_id AS categoryId,
               gv.version AS version,
               gv.content_body AS contentBody,
               to_char(gv.created_at, 'YYYY-MM-DD"T"HH24:MI:SS') AS createdAt
          FROM webguide.guides g
          JOIN webguide.guide_versions gv
            ON g.current_version_id = gv.version
           AND g.id = gv.guide_id
         WHERE g.portal_id = :portalId
           AND g.delete_yn = false
           AND (:categoryId IS NULL OR g.category_id = :categoryId)
           AND gv.content_body &@* :keyword
         ORDER BY pgroonga_score(gv.tableoid, gv.ctid) DESC, gv.created_at DESC
         LIMIT :limit
    """, nativeQuery = true)
    List<SearchResult> searchByKeyword(@Param("portalId") String portalId,
                                       @Param("categoryId") Long categoryId,
                                       @Param("keyword") String keyword,
                                       @Param("limit") int limit);
}