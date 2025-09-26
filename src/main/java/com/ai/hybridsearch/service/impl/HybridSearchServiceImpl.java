package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.dto.SearchResult;
import com.ai.hybridsearch.service.HybridSearchService;
import com.ai.hybridsearch.service.QueryBuilderService;
import com.ai.hybridsearch.service.RAGService;
import com.ai.hybridsearch.service.RerankerService;
import com.ai.hybridsearch.service.VectorSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class HybridSearchServiceImpl implements HybridSearchService {

    private static final int K_CONST = 60; // RRF 랭킹 상수, 튜닝 가능

    private final VectorSearchService vectorSearchService;
    private final QueryBuilderService queryBuilderService;
    private final RAGService ragService;
    private final RerankerService rerankerService; // 최적화된 Reranker 주입

    /**
     * RAG 파이프라인 전체 과정을 실행하는 메인 메서드.
     * 검색-재순위화된 컨텍스트를 기반으로 최종 답변을 생성.
     */
    @Override
    public RAGService.GeneratedResponse searchAndGenerate(String query, String category, int limit) {
        // Step 1: 고성능 검색 및 재정렬 파이프라인 실행
        List<SearchResult> rerankedContext = retrieveAndRerank(query, category, limit);
        // Step 2: 최종 컨텍스트를 기반으로 답변 생성 (LLM 호출 1회)
        return ragService.generate(query, rerankedContext);
    }

    /**
     * 재순위화까지 완료된 순수 검색 결과를 반환. (LLM 답변 생성 없음)
     */
    @Override
    public List<SearchResult> hybridSearch(String query, String category, int limit) {
        return retrieveAndRerank(query, category, limit);
    }

    /**
     * 검색, 융합, 재순위화의 공통 로직을 수행하는 고성능 헬퍼 메서드
     */
    private List<SearchResult> retrieveAndRerank(String query, String category, int limit) {
        // 1. 쿼리 변환 (필요 시 AI 모델 호출 1회)
        QueryBuilderService.TransformedQuery tQuery = queryBuilderService.transformQuery(query);

        // 2. 검색 (Retrieval) - 어휘/의미 검색을 병렬로 동시에 실행
        CompletableFuture<List<SearchResult>> lexicalFuture = CompletableFuture.supplyAsync(
                () -> lexicalSearch(tQuery.getLexicalQuery(), category, limit * 2)
        );
        CompletableFuture<List<SearchResult>> semanticFuture = CompletableFuture.supplyAsync(
                () -> vectorSearchService.semanticSearch(tQuery.getSemanticQuery(), category, limit * 2)
        );

        try {
            // 두 검색이 모두 완료될 때까지 대기
            CompletableFuture.allOf(lexicalFuture, semanticFuture).join();

            // 3. 결과 융합 (Reciprocal Rank Fusion) - 두 검색 결과를 RRF 알고리즘으로 결합
            List<SearchResult> fusedResults = fuseResultsWithRRF(lexicalFuture.get(), semanticFuture.get());

            // 4. 재순위화 (Reranking) - 성능이 최적화된 Reranker 호출
            return rerankerService.rerank(fusedResults, tQuery.getOriginalQuery(), limit);

        } catch (Exception e) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복원
            throw new RuntimeException("하이브리드 검색 및 재정렬 병렬 처리 중 오류 발생", e);
        }
    }

    /**
     * RRF (Reciprocal Rank Fusion)를 사용해 두 검색 결과를 결합
     */
    private List<SearchResult> fuseResultsWithRRF(List<SearchResult> lexicalResults, List<SearchResult> semanticResults) {
        Map<Long, Double> rrfScores = new ConcurrentHashMap<>();

        // 어휘 검색 결과에 RRF 점수 부여 (병렬 처리)
        IntStream.range(0, lexicalResults.size()).parallel().forEach(i -> {
            SearchResult res = lexicalResults.get(i);
            double score = 1.0 / (K_CONST + i + 1);
            rrfScores.merge(res.getDocument().getId(), score, Double::sum);
        });

        // 의미 검색 결과에 RRF 점수 부여 (병렬 처리)
        IntStream.range(0, semanticResults.size()).parallel().forEach(i -> {
            SearchResult res = semanticResults.get(i);
            double score = 1.0 / (K_CONST + i + 1);
            rrfScores.merge(res.getDocument().getId(), score, Double::sum);
        });

        // 모든 문서 정보를 ID 기반으로 맵에 저장해 중복 제거 및 빠른 조회
        Map<Long, SearchResult> allDocs = Stream.concat(lexicalResults.stream(), semanticResults.stream())
                .collect(Collectors.toMap(res -> res.getDocument().getId(), res -> res, (existing, replacement) -> existing));

        // 최종 RRF 점수를 기반으로 정렬해 반환
        return rrfScores.entrySet().stream()
                .map(entry -> {
                    SearchResult result = allDocs.get(entry.getKey());
                    result.setScore(entry.getValue()); // 점수를 RRF 점수로 업데이트
                    return result;
                })
                .sorted(Comparator.comparing(SearchResult::getScore).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 어휘 검색(Full-text search)을 수행
     */
    public List<SearchResult> lexicalSearch(String query, String category, int limit) {
        // 실제로는 Elasticsearch/OpenSearch 등을 직접 호출하는 로직이 들어감
        return vectorSearchService.findByFullTextSearch(query, category, limit)
                .stream()
                .map(doc -> new SearchResult(doc, doc.getScore(), "lexical"))
                .collect(Collectors.toList());
    }

    @Override
    public List<SearchResult> advancedHybridSearch(String query, String category,
                                                  boolean useFuzzy, boolean usePhrase, int limit) {
        // 이 메서드는 새로운 RAG 아키텍처에 맞게 재설계가 필요하므로, 기본 hybridSearch를 호출
        return hybridSearch(query, category, limit);
    }
}