package com.ai.hybridsearch.repository;

import com.ai.hybridsearch.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // -------------------
    // 조회용 메서드 (readOnly)
    // -------------------

    /**
     * 특정 포탈, 특정 섹션의 최상위(1뎁스) 활성 카테고리 목록을 조회
     */
    @Transactional(readOnly = true)
    @Query("SELECT c FROM Category c WHERE c.portalId = :portalId AND c.section = :section AND c.parent IS NULL AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findTopLevelActiveCategories(@Param("portalId") String portalId, @Param("section") String section);

    /**
     * 특정 포탈 내에서 ID로 활성 카테고리 조회
     */
    @Transactional(readOnly = true)
    Optional<Category> findByIdAndPortalIdAndIsActiveTrue(Long id, String portalId);

    /**
     * 특정 포탈 내에서 ID로 카테고리 조회 (활성 여부 무관)
     */
    @Transactional(readOnly = true)
    Optional<Category> findByIdAndPortalId(Long id, String portalId);

    /**
     * 특정 부모 카테고리에 속한 모든 활성 자식 카테고리 목록을 조회
     */
    @Transactional(readOnly = true)
    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.portalId = :portalId AND c.isActive = true ORDER BY c.displayOrder ASC")
    List<Category> findActiveChildren(@Param("parentId") Long parentId, @Param("portalId") String portalId);

    /**
     * 특정 포탈, 특정 부모 아래에 동일한 이름의 카테고리가 존재하는지 확인
     */
    @Transactional(readOnly = true)
    boolean existsByNameAndParentAndPortalId(String name, Category parent, String portalId);

    /**
     * 특정 포탈 내에서 모든 카테고리 조회 (활성 여부 무관)
     */
    @Transactional(readOnly = true)
    @Query("SELECT c FROM Category c WHERE c.portalId = :portalId ORDER BY c.depth ASC, c.displayOrder ASC")
    List<Category> findAllByPortalId(@Param("portalId") String portalId);

    // -------------------
    // 변경/배치용 메서드 (Modifying + Transactional)
    // -------------------

    /**
     * 특정 카테고리 목록을 비활성화 처리
     */
    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.isActive = false, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id IN :ids AND c.portalId = :portalId")
    int deactivateByIds(@Param("ids") List<Long> ids, @Param("portalId") String portalId);
}