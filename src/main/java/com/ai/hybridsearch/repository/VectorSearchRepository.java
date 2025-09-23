package com.ai.hybridsearch.repository;

import com.ai.hybridsearch.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VectorSearchRepository extends JpaRepository<Document, Long> {

    // [수정됨] GUIDES와 GUIDE_VERSIONS를 JOIN하여 현재 버전의 embedding으로 벡터 검색
    @Query(value = """
        SELECT gv.*, 1 - (gv.embedding <=> CAST(:vector AS vector)) AS similarity
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.current_version_id = gv.version
        WHERE g.delete_yn = false
        ORDER BY gv.embedding <=> CAST(:vector AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> searchByEmbedding(@Param("vector") String vector, @Param("limit") int limit);

    // [수정됨] 카테고리 필터링 추가
    @Query(value = """
        SELECT gv.*, 1 - (gv.embedding <=> CAST(:vector AS vector)) AS similarity
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.current_version_id = gv.version
        WHERE g.category_id = :category AND g.delete_yn = false
        ORDER BY gv.embedding <=> CAST(:vector AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> searchByEmbeddingAndCategory(@Param("vector") String vector, @Param("category") String category, @Param("limit") int limit);

    // [수정됨] GUIDES와 GUIDE_VERSIONS를 JOIN하여 현재 버전의 search_vector로 Full-text 검색
    @Query(value = """
        SELECT gv.*, ts_rank(gv.search_vector, plainto_tsquery(:query)) as rank
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.current_version_id = gv.version
        WHERE gv.search_vector @@ plainto_tsquery(:query) AND g.delete_yn = false
        ORDER BY rank DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findByFullTextSearch(@Param("query") String query, @Param("limit") int limit);

    // [수정됨] 카테고리 필터링 추가
    @Query(value = """
        SELECT gv.*, ts_rank(gv.search_vector, plainto_tsquery(:query)) as rank
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.current_version_id = gv.version
        WHERE gv.search_vector @@ plainto_tsquery(:query)
        AND g.delete_yn = false
        AND (:category IS NULL OR g.category_id = :category)
        ORDER BY rank DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findByFullTextSearchAndCategory(@Param("query") String query, @Param("category") String category, @Param("limit") int limit);

    // [수정됨] 상세 랭킹을 위한 Full-text 검색
    @Query(value = """
        SELECT gv.*,
               ts_rank_cd(gv.search_vector, plainto_tsquery(:query)) as detailed_rank,
               ts_rank(gv.search_vector, plainto_tsquery(:query)) as simple_rank
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.current_version_id = gv.version
        WHERE gv.search_vector @@ plainto_tsquery(:query) AND g.delete_yn = false
        ORDER BY detailed_rank DESC, simple_rank DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findByFullTextSearchWithDetailedRanking(@Param("query") String query, @Param("limit") int limit);

    // [수정됨] Full-text 점수 분석
    @Query(value = """
        SELECT
            MIN(ts_rank(gv.search_vector, plainto_tsquery(:query))) as min_score,
            MAX(ts_rank(gv.search_vector, plainto_tsquery(:query))) as max_score,
            AVG(ts_rank(gv.search_vector, plainto_tsquery(:query))) as avg_score,
            COUNT(*) as match_count
        FROM webguide.guide_versions gv
        JOIN webguide.guides g ON g.current_version_id = gv.version
        WHERE gv.search_vector @@ plainto_tsquery(:query) AND g.delete_yn = false
        """, nativeQuery = true)
    Object[] analyzeFullTextScores(@Param("query") String query);

}