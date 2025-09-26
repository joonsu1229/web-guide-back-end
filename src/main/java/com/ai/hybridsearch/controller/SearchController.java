package com.ai.hybridsearch.controller;

import com.ai.hybridsearch.dto.SearchResult;
import com.ai.hybridsearch.service.HybridSearchService;
import com.ai.hybridsearch.service.SearchAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {

    private final HybridSearchService hybridSearchService;
    private final SearchAnalyticsService searchAnalyticsService;

    @Autowired
    public SearchController(HybridSearchService hybridSearchService, SearchAnalyticsService searchAnalyticsService) {
        this.hybridSearchService = hybridSearchService;
        this.searchAnalyticsService = searchAnalyticsService;
    }

    @GetMapping("/hybrid")
    public ResponseEntity<List<SearchResult>> hybridSearch(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") int limit) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 최적화된 하이브리드 검색 (검색 -> 융합 -> 재순위화)
        List<SearchResult> results = hybridSearchService.hybridSearch(query, category, limit);

        stopWatch.stop();
        long responseTime = stopWatch.getTotalTimeMillis();

        searchAnalyticsService.recordSearch(query, category, results.size(), responseTime);

        return ResponseEntity.ok(results);
    }
}