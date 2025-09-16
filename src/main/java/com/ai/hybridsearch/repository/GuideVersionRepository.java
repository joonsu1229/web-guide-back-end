package com.ai.hybridsearch.repository;

import com.ai.hybridsearch.dto.GuideContentDto;
import com.ai.hybridsearch.entity.GuideVersion;
import org.springframework.data.jpa.repository.JpaRepository;
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

    /**
     * 특정 Guide의 가장 최신 버전을 조회
     */
    @Transactional(readOnly = true)
    Optional<GuideVersion> findTopByGuideIdOrderByVersionDesc(Long guideId);

    /**
     * 특정 Guide의 모든 버전 목록을 최신순으로 조회
     */
    @Transactional(readOnly = true)
    List<GuideVersion> findByGuideIdOrderByVersionDesc(Long guideId);

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
    @Query("SELECT new com.ai.hybridsearch.dto.GuideContentDto(gv.id, gv.contentBody, gv.version, gv.guide.category.id) " +
           "FROM GuideVersion gv " +
           "WHERE gv.guide.id = :guideId " +
           "ORDER BY gv.version DESC")
    List<GuideContentDto> findHistoryDtoByGuideId(@Param("guideId") Long guideId);
}