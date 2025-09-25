package com.ai.hybridsearch.repository;

import com.ai.hybridsearch.dto.GuideContentDto;
import com.ai.hybridsearch.entity.Guide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {

    // -------------------
    // 조회용 메서드 (readOnly)
    // -------------------

    /**
     * 특정 포탈, 특정 카테고리 ID에 해당하는 Guide를 조회 (삭제 여부 무관)
     */
    @Transactional(readOnly = true)
    Optional<Guide> findByCategoryIdAndPortalId(Long categoryId, String portalId);

    /**
     * 특정 포탈, 특정 카테고리 ID에 해당하는 '삭제되지 않은' Guide를 조회
     */
    @Transactional(readOnly = true)
    @Query("SELECT g FROM Guide g WHERE g.category.id = :categoryId AND g.portalId = :portalId AND g.deleteYn = false")
    Optional<Guide> findActiveGuide(@Param("categoryId") Long categoryId, @Param("portalId") String portalId);

    /**
     * N+1 문제 해결을 위해 Category를 함께 fetch join하는 메서드
     */
    @Transactional(readOnly = true)
    @Query("SELECT g FROM Guide g JOIN FETCH g.category WHERE g.category.id = :categoryId AND g.portalId = :portalId AND g.deleteYn = false")
    Optional<Guide> findActiveGuideWithCategory(@Param("categoryId") Long categoryId, @Param("portalId") String portalId);

    /**
     * getCurrentContent()를 위한 DTO 프로젝션 메서드
     */
    @Transactional(readOnly = true)
    @Query("SELECT new com.ai.hybridsearch.dto.GuideContentDto(gv.id, gv.contentBody, gv.version, g.category.id) " +
           "FROM Guide g " +
           "JOIN g.currentVersion gv " +
           "WHERE g.category.id = :categoryId AND g.portalId = :portalId AND g.deleteYn = false")
    Optional<GuideContentDto> findCurrentContentDto(@Param("categoryId") Long categoryId, @Param("portalId") String portalId);


    // -------------------
    // 변경/배치용 메서드 (Modifying + Transactional)
    // -------------------

    /**
     * 특정 Guide를 소프트 삭제 처리 (deleteYn = true)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Guide g SET g.deleteYn = true, g.updatedAt = CURRENT_TIMESTAMP WHERE g.id = :guideId AND g.portalId = :portalId")
    int softDeleteById(@Param("guideId") Long guideId, @Param("portalId") String portalId);

    /**
     * 특정 Guide의 현재 버전을 업데이트
     */
    @Modifying
    @Transactional
    @Query("UPDATE Guide g SET g.currentVersion.id = :versionId, g.updatedAt = CURRENT_TIMESTAMP WHERE g.id = :guideId")
    int updateCurrentVersion(@Param("guideId") Long guideId, @Param("versionId") Long versionId);

    /**
     * JPQL 번역 계층을 완전히 우회하기 위한 네이티브 쿼리 테스트
     */
    @Query(value = "SELECT gv.id, gv.content_body, gv.version, g.category_id " +
                   "FROM webguide.guides g " +
                   "JOIN webguide.guide_versions gv ON  g.current_version_id = gv.version AND g.id = gv.guide_id " +
                   "WHERE g.category_id = :categoryId AND g.portal_id = :portalId AND g.delete_yn = false",
           nativeQuery = true) // nativeQuery = true 옵션 추가
    Optional<GuideContentDto> findCurrentContentNative(@Param("categoryId") Long categoryId, @Param("portalId") String portalId);
}