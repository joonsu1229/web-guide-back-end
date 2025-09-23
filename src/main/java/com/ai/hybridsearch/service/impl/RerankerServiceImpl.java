package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.dto.SearchResult;
import com.ai.hybridsearch.service.EmbeddingService;
import com.ai.hybridsearch.service.RerankerService;
import dev.langchain4j.data.embedding.Embedding;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RerankerServiceImpl implements RerankerService {

    private final EmbeddingService embeddingService;

    // @Autowired 필드 주입보다는 생성자 주입을 권장합니다.
    public RerankerServiceImpl(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    /**
     * RRF 등으로 1차 정렬된 검색 결과를 받아, 의미적 유사도를 추가로 계산하여 재정렬(rerank)합니다.
     * @param results 초기 검색 결과 (RRF 점수가 포함된)
     * @param query 원본 사용자 쿼리
     * @param topK 최종 반환할 결과 수
     * @return 재정렬된 최종 결과
     */
    @Override
    public List<SearchResult> rerank(List<SearchResult> results, String query, int topK) {
        if (results == null || results.isEmpty()) {
            return results;
        }

        // 1. 쿼리를 한 번만 임베딩합니다.
        Embedding queryEmbedding = embeddingService.generateEmbedding(query);

        // 2. 각 결과에 대해 의미적 유사도를 계산하고 새로운 점수를 부여합니다.
        return results.stream()
            .map(result -> {
                // Document에 title이 없으므로 contentBody만 사용합니다.
                String content = result.getDocument().getContentBody();
                if (content == null || content.isBlank()) {
                    // 내용이 없는 경우 점수를 낮게 조정하거나 그대로 둡니다.
                    result.setScore(result.getScore() * 0.5); // 예시: 패널티 부여
                    return result;
                }

                Embedding docEmbedding = embeddingService.generateEmbedding(content);
                double semanticSimilarity = embeddingService.cosineSimilarity(queryEmbedding, docEmbedding);

                // 최종 점수 계산: (초기 점수 * 가중치) + (의미 유사도 점수 * 가중치)
                // 이 가중치는 실험을 통해 최적의 값을 찾아야 합니다.
                double finalScore = (result.getScore() * 0.4) + (semanticSimilarity * 0.6);
                result.setScore(finalScore);

                return result;
            })
            // 3. 새로 계산된 점수를 기준으로 내림차순 정렬합니다.
            .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
            // 4. 상위 K개만 선택합니다.
            .limit(topK)
            .collect(Collectors.toList());
    }
}