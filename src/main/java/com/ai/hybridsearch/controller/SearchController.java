package com.ai.hybridsearch.controller;

import com.ai.hybridsearch.dto.SearchResult;
import com.ai.hybridsearch.service.impl.HybridSearchServiceImpl;
import com.ai.hybridsearch.service.impl.SearchAnalyticsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "*")
public class SearchController {
    
    @Autowired
    private HybridSearchServiceImpl hybridSearchServiceImpl;

    @Autowired
    private SearchAnalyticsServiceImpl searchAnalyticsServiceImpl;

    @GetMapping("/hybrid")
    public ResponseEntity<List<SearchResult>> hybridSearch(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") int limit) {

        // 하이브리드 검색 - 의미적 유사도까지 고려한 고품질 결과
        List<SearchResult> results = hybridSearchServiceImpl.hybridSearch(query, category, limit);
        searchAnalyticsServiceImpl.recordSearch(query, category, results.size(), 10); // 0은 응답 시간 예시

        return ResponseEntity.ok(results);
    }
}