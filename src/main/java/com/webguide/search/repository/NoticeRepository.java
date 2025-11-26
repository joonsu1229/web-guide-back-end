package com.webguide.search.repository;

import com.webguide.search.entity.Notice;
import com.webguide.search.dto.NoticeDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // --------------------------
    // 기본 조회용 메서드 (readOnly)
    // --------------------------

    /**
     * 사용여부(useYn) 기준 전체 조회
     */
    @Transactional(readOnly = true)
    List<Notice> findAllByUseYn(String useYn);


    /**
     * category + useYn 조회
     */
    @Transactional(readOnly = true)
    List<Notice> findAllByCategoryAndUseYn(String category, String useYn);


    /**
     * 상세 조회(삭제되지 않은 공지만)
     */
    @Transactional(readOnly = true)
    @Query("SELECT n FROM Notice n WHERE n.noticeId = :id AND n.deleteYn = 'N'")
    Optional<Notice> findActiveNotice(@Param("id") Long id);


    /**
     * 사용중인 공지 전체 리스트 (정렬 포함)
     */
    @Transactional(readOnly = true)
    @Query("SELECT n FROM Notice n WHERE n.deleteYn = 'N' AND n.useYn = 'Y' ORDER BY n.regDt DESC")
    List<Notice> findAllActive();


    // --------------------------------
    // DTO 프로젝션 조회 (Guide 스타일)
    // --------------------------------

    /**
     * NoticeDto로 바로 반환
     */
    @Transactional(readOnly = true)
    @Query("""
        SELECT new com.webguide.search.dto.NoticeDto(
            n.noticeId,
            n.category,
            n.title,
            n.summary,
            n.content,
            n.views,
            n.useYn,
            n.isNew,
            n.regDt,
            n.modDt
        )
        FROM Notice n
        WHERE n.deleteYn = 'N'
        ORDER BY n.regDt DESC
        """)
    List<NoticeDto> findAllActiveAsDto();


    // --------------------------
    // 수정/삭제 (soft delete)
    // --------------------------

    /**
     * Soft Delete (deleteYn = true)
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notice n SET n.deleteYn = 'Y', n.modDt = CURRENT_TIMESTAMP WHERE n.noticeId = :id")
    int softDelete(@Param("id") Long id);


    /**
     * useYn 상태 변경
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notice n SET n.useYn = :useYn, n.modDt = CURRENT_TIMESTAMP WHERE n.noticeId = :id")
    int updateUseYn(@Param("id") Long id, @Param("useYn") String useYn);


    /**
     * 조회수 증가
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notice n SET n.views = n.views + 1 WHERE n.noticeId = :id")
    int increaseViews(@Param("id") Long id);


    // --------------------------------
    // NativeQuery 기반 상세 조회
    // --------------------------------

    @Transactional(readOnly = true)
    @Query(value = """
        SELECT n.notice_id,
               n.category,
               n.title,
               n.summary,
               n.content,
               n.views,
               n.use_yn,
               n.is_new,
               n.reg_dt,
               n.mod_dt
          FROM webguide.notice n
         WHERE n.notice_id = :id
           AND n.use_yn = 'Y'
           AND n.delete_yn = 'N'
        """, nativeQuery = true)
    Optional<Object[]> findNoticeNative(@Param("id") Long id);
}
