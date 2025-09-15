package com.ai.hybridsearch.repository;

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


    // -------------------
    // 변경/배치용 메서드 (Modifying + Transactional)
    // -------------------
    // 버전 데이터는 수정/삭제하지 않는 것을 원칙으로 하므로,
    // JpaRepository의 save() 외에 별도의 @Modifying 쿼리는 작성하지 않음.
}