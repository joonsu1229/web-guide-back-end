package com.ai.hybridsearch.repository;

import com.ai.hybridsearch.entity.PortalMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortalMenuRepository extends JpaRepository<PortalMenu, Long> {

    // -------------------
    // 조회용 메서드 (readOnly)
    // -------------------

    /**
     * 특정 포탈의 모든 메인 메뉴를 displayOrder 순으로 조회
     */
    @Transactional(readOnly = true)
    List<PortalMenu> findByPortalIdOrderByDisplayOrderAsc(String portalId);

    /**
     * 특정 포탈 내에서 section 키로 메인 메뉴 조회
     */
    @Transactional(readOnly = true)
    Optional<PortalMenu> findBySectionAndPortalId(String section, String portalId);

    /**
     * 특정 포탈 내에서 title로 메뉴 존재 여부 확인
     */
    @Transactional(readOnly = true)
    boolean existsByTitleAndPortalId(String title, String portalId);

    /**
     * portalId와 id로 메뉴 조회
     */
    @Transactional(readOnly = true)
    Optional<PortalMenu> findByIdAndPortalId(Long id, String portalId);

    /**
     * JPQL을 사용한 title 검색 예시
     */
    @Transactional(readOnly = true)
    @Query("SELECT pm FROM PortalMenu pm WHERE pm.portalId = :portalId AND pm.title LIKE %:title%")
    List<PortalMenu> findByPortalIdAndTitleContaining(@Param("portalId") String portalId, @Param("title") String title);


    // -------------------
    // 변경/배치용 메서드 (Modifying + Transactional)
    // -------------------
    // PortalMenu는 데이터 양이 많지 않고, CUD가 JpaRepository 기본 메서드로 충분하여
    // 별도의 @Modifying 쿼리는 작성하지 않음. 필요시 여기에 추가.
}