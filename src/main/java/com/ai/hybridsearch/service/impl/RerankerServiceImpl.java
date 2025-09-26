package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.dto.SearchResult;
import com.ai.hybridsearch.service.EmbeddingService;
import com.ai.hybridsearch.service.RerankerService;
import dev.langchain4j.data.embedding.Embedding;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class RerankerServiceImpl implements RerankerService {

    private final EmbeddingService embeddingService;

    // 이 가중치는 실험을 통해 최적의 값을 찾아 조정할 수 있음.
    private static final double LEXICAL_SCORE_WEIGHT = 0.4; // 초기 검색 점수(어휘) 가중치
    private static final double SEMANTIC_SCORE_WEIGHT = 0.6; // 의미 유사도 점수 가중치

    /**
     * 초기 검색 결과를 받아 '질문'과 '문서'의 의미적 유사도를 계산하여 재정렬.
     * AI 모델 호출을 최소화(질문 1회, 전체 문서 1회)하여 성능을 극대화함.
     *
     * @param results 초기 검색 결과 목록 (재정렬 대상)
     * @param query   원본 사용자 질문
     * @param topK    최종적으로 반환할 상위 결과의 수
     * @return 재정렬된 SearchResult 객체 목록
     */
    @Override
    public List<SearchResult> rerank(List<SearchResult> results, String query, int topK) {
        if (results == null || results.isEmpty()) {
            return results; // 재정렬할 결과가 없으면 그대로 반환
        }

        // --- 성능 최적화 핵심 로직 ---
        // 1. 질문을 '질문 전용 모델'로 임베딩 (AI 호출 1회)
        Embedding queryEmbedding = embeddingService.embedQueryToEmbedding(query);

        // 2. 재정렬 대상인 모든 문서 내용을 한 번의 '배치 호출'로 임베딩 (AI 호출 1회)
        List<String> documentContents = results.stream()
                .map(result -> result.getDocument().getContentBody())
                .collect(Collectors.toList());
        List<Embedding> docEmbeddings = embeddingService.embedAll(documentContents);
        // --- AI 모델 호출 종료 ---

        // 3. 미리 계산된 임베딩 값으로 각 결과의 최종 점수를 로컬에서 계산 (AI 호출 없음)
        return IntStream.range(0, results.size())
            .mapToObj(i -> {
                SearchResult result = results.get(i);
                Embedding docEmbedding = docEmbeddings.get(i);

                // 코사인 유사도로 의미적 유사도 점수를 계산
                double semanticSimilarity = embeddingService.cosineSimilarity(queryEmbedding, docEmbedding);

                // 최종 점수 = (초기 점수 * 가중치) + (의미 유사도 점수 * 가중치)
                double finalScore = (result.getScore() * LEXICAL_SCORE_WEIGHT) + (semanticSimilarity * SEMANTIC_SCORE_WEIGHT);
                result.setScore(finalScore);

                return result;
            })
            // 4. 새로 계산된 최종 점수를 기준으로 내림차순 정렬
            .sorted((r1, r2) -> Double.compare(r2.getScore(), r1.getScore()))
            // 5. 상위 K개의 결과만 선택하여 반환
            .limit(topK)
            .collect(Collectors.toList());
    }
}