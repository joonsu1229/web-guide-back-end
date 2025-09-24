package com.ai.hybridsearch.repository;

import com.ai.hybridsearch.entity.GuideVersion; // 엔티티 타입을 GuideVersion으로 변경
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// JpaRepository의 제네릭 타입을 실제 쿼리 대상인 GuideVersion으로 변경하는 것을 권장합니다.
public interface VectorSearchRepository extends JpaRepository<GuideVersion, Long> {

    // [수정됨] g.id = gv.guide_id JOIN 조건을 추가하고, 버전 비교는 WHERE절로 이동
    @Query(value = """
        SELECT gv.*, 1 - (gv.embedding <=> CAST(:vector AS vector)) AS similarity
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.id = gv.guide_id
        WHERE g.current_version_id = gv.version
          AND g.delete_yn = false
        ORDER BY gv.embedding <=> CAST(:vector AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> searchByEmbedding(@Param("vector") String vector, @Param("limit") int limit);

    // [수정됨] g.id = gv.guide_id JOIN 조건을 추가하고, 버전 비교는 WHERE절로 이동
    @Query(value = """
        SELECT gv.*, 1 - (gv.embedding <=> CAST(:vector AS vector)) AS similarity
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.id = gv.guide_id
        WHERE g.current_version_id = gv.version
          AND g.category_id = :categoryId
          AND g.delete_yn = false
        ORDER BY gv.embedding <=> CAST(:vector AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> searchByEmbeddingAndCategory(@Param("vector") String vector, @Param("categoryId") String categoryId, @Param("limit") int limit);

    // [수정됨] g.id = gv.guide_id JOIN 조건을 추가하고, 버전 비교는 WHERE절로 이동
    @Query(value = """
        SELECT gv.*, ts_rank(gv.search_vector, plainto_tsquery(:query)) as rank
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.id = gv.guide_id
        WHERE g.current_version_id = gv.version
          AND gv.search_vector @@ plainto_tsquery(:query)
          AND g.delete_yn = false
        ORDER BY rank DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findByFullTextSearch(@Param("query") String query, @Param("limit") int limit);

    // [수정됨] g.id = gv.guide_id JOIN 조건을 추가하고, 버전 비교는 WHERE절로 이동
    @Query(value = """
        SELECT gv.*, ts_rank(gv.search_vector, plainto_tsquery(:query)) as rank
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.id = gv.guide_id
        WHERE g.current_version_id = gv.version
          AND gv.search_vector @@ plainto_tsquery(:query)
          AND g.delete_yn = false
          AND (:categoryId IS NULL OR g.category_id = :categoryId)
        ORDER BY rank DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findByFullTextSearchAndCategory(@Param("query") String query, @Param("categoryId") String categoryId, @Param("limit") int limit);

    // [수정됨] g.id = gv.guide_id JOIN 조건을 추가하고, 버전 비교는 WHERE절로 이동
    @Query(value = """
        SELECT gv.*,
               ts_rank_cd(gv.search_vector, plainto_tsquery(:query)) as detailed_rank,
               ts_rank(gv.search_vector, plainto_tsquery(:query)) as simple_rank
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.id = gv.guide_id
        WHERE g.current_version_id = gv.version
          AND gv.search_vector @@ plainto_tsquery(:query)
          AND g.delete_yn = false
        ORDER BY detailed_rank DESC, simple_rank DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findByFullTextSearchWithDetailedRanking(@Param("query") String query, @Param("limit") int limit);

    // [수정됨] g.id = gv.guide_id JOIN 조건을 추가하고, 버전 비교는 WHERE절로 이동
    @Query(value = """
        SELECT
            MIN(ts_rank(gv.search_vector, plainto_tsquery(:query))) as min_score,
            MAX(ts_rank(gv.search_vector, plainto_tsquery(:query))) as max_score,
            AVG(ts_rank(gv.search_vector, plainto_tsquery(:query))) as avg_score,
            COUNT(*) as match_count
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.id = gv.guide_id
        WHERE g.current_version_id = gv.version
          AND gv.search_vector @@ plainto_tsquery(:query)
          AND g.delete_yn = false
        """, nativeQuery = true)
    Object[] analyzeFullTextScores(@Param("query") String query);

}
