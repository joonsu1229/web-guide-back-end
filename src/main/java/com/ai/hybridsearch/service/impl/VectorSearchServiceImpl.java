package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.dto.SearchResult;
import com.ai.hybridsearch.entity.Document;
import com.ai.hybridsearch.repository.VectorSearchRepository;
import com.ai.hybridsearch.service.EmbeddingService;
import com.ai.hybridsearch.service.VectorSearchService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VectorSearchServiceImpl implements VectorSearchService {

    private final EmbeddingService embeddingService;
    private final VectorSearchRepository vectorSearchRepository;

    // Object[] 매핑 상수 (GUIDE_VERSIONS 테이블 순서에 정확히 일치)
    private static final int IDX_ID = 0;
    private static final int IDX_GUIDE_ID = 1;
    private static final int IDX_VERSION = 2;
    private static final int IDX_CONTENT_BODY = 3;
    private static final int IDX_CREATED_AT = 4;
    // 점수는 gv.* 이후 마지막 인덱스에 위치

    public VectorSearchServiceImpl(EmbeddingService embeddingService, VectorSearchRepository vectorSearchRepository) {
        this.embeddingService = embeddingService;
        this.vectorSearchRepository = vectorSearchRepository;
    }

    @Override
    public List<SearchResult> semanticSearch(String query, String category, int limit) {
        float[] embedding = embeddingService.embed(query);
        String vectorStr = toVectorString(embedding);

        List<Object[]> results;
        if (StringUtils.hasText(category)) {
            results = vectorSearchRepository.searchByEmbeddingAndCategory(vectorStr, category, limit);
        } else {
            results = vectorSearchRepository.searchByEmbedding(vectorStr, limit);
        }

        return mapToSearchResults(results, "semantic");
    }

    @Override
    public List<Document> findByFullTextSearch(String searchQuery, String category, int limit) {
        List<Object[]> results;
        if (StringUtils.hasText(category)) {
            results = vectorSearchRepository.findByFullTextSearchAndCategory(searchQuery, category, limit);
        } else {
            results = vectorSearchRepository.findByFullTextSearch(searchQuery, limit);
        }
        return mapToDocumentsWithScore(results);
    }

    // Private helper methods
    private String toVectorString(float[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }
        return Arrays.toString(array).replace(" ", "");
    }

    private List<Document> mapToDocumentsWithScore(List<Object[]> results) {
        return results.stream()
                .map(this::mapRowToDocumentWithScore)
                .collect(Collectors.toList());
    }

    private List<SearchResult> mapToSearchResults(List<Object[]> results, String searchType) {
        return results.stream()
                .map(row -> new SearchResult(mapRowToDocument(row), extractScore(row), searchType))
                .collect(Collectors.toList());
    }

    private float extractScore(Object[] row) {
        Object scoreObj = row[row.length - 1];
        if (scoreObj instanceof Number) {
            return ((Number) scoreObj).floatValue();
        }
        return 0f;
    }

    private Document mapRowToDocumentWithScore(Object[] row) {
        Document document = mapRowToDocument(row);
        document.setScore(extractScore(row));
        return document;
    }

    /**
     * Repository의 'SELECT gv.*' 쿼리 결과에 맞춰 모든 요청된 필드를 매핑
     */
    private Document mapRowToDocument(Object[] row) {
        Document document = new Document();

        document.setId(getDbValue(row, IDX_ID, Number.class, Number::longValue));
        document.setGuideId(getDbValue(row, IDX_GUIDE_ID, Number.class, Number::longValue));
        document.setVersion(getDbValue(row, IDX_VERSION, Number.class, Number::intValue));
        document.setContentBody(getDbValue(row, IDX_CONTENT_BODY, String.class, val -> val));
        document.setCreatedAt(getDbValue(row, IDX_CREATED_AT, Timestamp.class, Timestamp::toLocalDateTime));

        return document;
    }

    private <T, R> R getDbValue(Object[] row, int index, Class<T> type, java.util.function.Function<T, R> mapper) {
        if (row.length > index && row[index] != null && type.isInstance(row[index])) {
            return mapper.apply(type.cast(row[index]));
        }
        return null;
    }
}