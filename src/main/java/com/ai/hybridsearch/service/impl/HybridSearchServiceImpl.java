package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.dto.SearchResult;
import com.ai.hybridsearch.service.HybridSearchService;
import com.ai.hybridsearch.service.QueryBuilderService; // 인터페이스 타입
import com.ai.hybridsearch.service.RAGService;           // 인터페이스 타입
import com.ai.hybridsearch.service.VectorSearchService;
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
public class HybridSearchServiceImpl implements HybridSearchService {

    private static final int K_CONST = 60; // RRF 랭킹 상수

    private final VectorSearchService vectorSearchService;
    private final QueryBuilderService queryBuilderService;
    private final RAGService ragService;

    // 생성자 주입
    public HybridSearchServiceImpl(VectorSearchService vectorSearchService,
                                   QueryBuilderService queryBuilderService,
                                   RAGService ragService) {
        this.vectorSearchService = vectorSearchService;
        this.queryBuilderService = queryBuilderService;
        this.ragService = ragService;
    }

    /**
     * RAG 파이프라인을 실행하는 메인 메서드.
     * 최종 답변과 출처 문서를 포함하는 객체를 반환합니다.
     */
    public RAGService.GeneratedResponse searchAndGenerate(String query, String category, int limit) {
        // 1. 쿼리 변환
        QueryBuilderService.TransformedQuery tQuery = queryBuilderService.transformQuery(query);

        // 2. 검색 (Retrieval) - 어휘/의미 검색 병렬 실행
        CompletableFuture<List<SearchResult>> lexicalFuture = CompletableFuture.supplyAsync(
                () -> lexicalSearch(tQuery.getLexicalQuery(), category, limit * 2)
        );
        CompletableFuture<List<SearchResult>> semanticFuture = CompletableFuture.supplyAsync(
                () -> vectorSearchService.semanticSearch(tQuery.getSemanticQuery(), category, limit * 2)
        );

        CompletableFuture.allOf(lexicalFuture, semanticFuture).join();

        try {
            // 3. 결과 융합 (Reciprocal Rank Fusion)
            List<SearchResult> fusedResults = fuseResultsWithRRF(lexicalFuture.get(), semanticFuture.get());

            // 4. 최종 랭킹 및 limit 적용
            List<SearchResult> finalContext = fusedResults.stream()
                    .sorted(Comparator.comparing(SearchResult::getScore).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());

            // 5. 답변 생성 (Generation)
            return ragService.generate(tQuery.getOriginalQuery(), finalContext);

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("RAG 검색 처리 중 병렬 오류 발생", e);
        }
    }

    /**
     * RRF (Reciprocal Rank Fusion)를 사용하여 두 검색 결과를 결합합니다.
     * @param lexicalResults 어휘 검색 결과
     * @param semanticResults 의미 검색 결과
     * @return RRF 점수가 계산된 통합 결과 리스트
     */
    private List<SearchResult> fuseResultsWithRRF(List<SearchResult> lexicalResults, List<SearchResult> semanticResults) {
        Map<Long, Double> rrfScores = new ConcurrentHashMap<>();

        // 랭크를 계산 (rank = index + 1)
        IntStream.range(0, lexicalResults.size()).forEach(i -> {
            SearchResult res = lexicalResults.get(i);
            double score = 1.0 / (K_CONST + i + 1);
            rrfScores.merge(res.getDocument().getId(), score, Double::sum);
        });

        IntStream.range(0, semanticResults.size()).forEach(i -> {
            SearchResult res = semanticResults.get(i);
            double score = 1.0 / (K_CONST + i + 1);
            rrfScores.merge(res.getDocument().getId(), score, Double::sum);
        });

        Map<Long, SearchResult> allDocs = Stream.concat(lexicalResults.stream(), semanticResults.stream())
                .collect(Collectors.toMap(res -> res.getDocument().getId(), res -> res, (existing, replacement) -> existing));

        return rrfScores.entrySet().stream()
                .map(entry -> {
                    SearchResult result = allDocs.get(entry.getKey());
                    result.setScore(entry.getValue().floatValue()); // RRF 점수로 업데이트
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 어휘 검색을 수행합니다.
     */
    public List<SearchResult> lexicalSearch(String query, String category, int limit) {
        return vectorSearchService.findByFullTextSearch(query, category, limit)
                .stream()
                .map(doc -> new SearchResult(doc, doc.getScore(), "lexical"))
                .collect(Collectors.toList());
    }

    /**
     * 기존 인터페이스와의 호환성을 위해 유지합니다.
     * RAG의 검색 결과(컨텍스트)만 반환합니다.
     */
    @Override
    public List<SearchResult> hybridSearch(String query, String category, int limit) {
        QueryBuilderService.TransformedQuery tQuery = queryBuilderService.transformQuery(query);

        // 2. 검색 (Retrieval) - 어휘/의미 검색 병렬 실행
        CompletableFuture<List<SearchResult>> lexicalFuture = CompletableFuture.supplyAsync(
                () -> lexicalSearch(tQuery.getLexicalQuery(), category, limit * 2)
        );
        CompletableFuture<List<SearchResult>> semanticFuture = CompletableFuture.supplyAsync(
                () -> vectorSearchService.semanticSearch(tQuery.getSemanticQuery(), category, limit * 2)
        );

        try {
            // 두 비동기 작업이 모두 완료될 때까지 기다림
            CompletableFuture.allOf(lexicalFuture, semanticFuture).join();

            // 3. 결과 융합 (Reciprocal Rank Fusion)
            List<SearchResult> fusedResults = fuseResultsWithRRF(lexicalFuture.get(), semanticFuture.get());

            // 4. 최종 랭킹 및 limit 적용
            return fusedResults.stream()
                    .sorted(Comparator.comparing(SearchResult::getScore).reversed())
                    .limit(3)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("하이브리드 검색 처리 중 병렬 오류 발생", e);
        }
    }

    // advancedHybridSearch 메서드는 RAG 플로우와 맞지 않아 생략하거나,
    // 필요 시 위 로직을 기반으로 재구현해야 합니다.
    @Override
    public List<SearchResult> advancedHybridSearch(String query, String category,
                                                  boolean useFuzzy, boolean usePhrase, int limit) {
        // 이 메서드는 새로운 RAG 아키텍처에 맞게 재설계가 필요합니다.
        // 여기서는 기본 hybridSearch를 호출하는 것으로 대체합니다.
        return hybridSearch(query, category, limit);
    }
}