package com.ai.hybridsearch.repository;

import com.ai.hybridsearch.dto.GuideContentDto;
import com.ai.hybridsearch.entity.GuideVersion;
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

    @Transactional(readOnly = true)
    @Query("SELECT new com.ai.hybridsearch.dto.GuideContentDto(gv.id, gv.contentBody, gv.version, gv.guide.category.id) " +
           "FROM GuideVersion gv " +
           "WHERE gv.guide.id = :guideId " +
           "ORDER BY gv.version DESC")
    List<GuideContentDto> findTopDtoByGuideId(@Param("guideId") Long guideId, Pageable pageable);


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

    /**
     * ID를 기준으로 GuideVersion의 임베딩을 업데이트합니다.
     */
    @Modifying
    @Transactional
    @Query(value = "UPDATE guide_version SET embedding = CAST(:embeddingText AS vector) WHERE id = :id", nativeQuery = true)
    void updateEmbedding(@Param("id") Long id, @Param("embeddingText") String embeddingText);
}