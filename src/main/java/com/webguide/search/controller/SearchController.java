package com.webguide.search.controller;

import com.webguide.search.dto.SearchResult;
import com.webguide.search.repository.GuideVersionRepository;
import com.webguide.search.service.SearchAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class SearchController {

    private final GuideVersionRepository guideVersionRepository;
    private final SearchAnalyticsService searchAnalyticsService;

    /**
     * ✅ PGroonga 기반 검색 API
     */
    @GetMapping
    public ResponseEntity<List<SearchResult>> search(
            @RequestParam String query,
            @RequestParam(value = "portalId", defaultValue = "P1") String portalId,
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(defaultValue = "10") int limit) {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<SearchResult> results = guideVersionRepository.searchByKeyword(portalId, categoryId, query, limit);

        stopWatch.stop();
        long responseTime = stopWatch.getTotalTimeMillis();

        searchAnalyticsService.recordSearch(query, categoryId != null ? categoryId.toString() : null,
                results.size(), responseTime);

        return ResponseEntity.ok(results);
    }

    private LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp ts) return ts.toLocalDateTime();
        if (value instanceof Instant instant)
            return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (value instanceof LocalDateTime ldt) return ldt;
        throw new IllegalArgumentException("Unsupported datetime type: " + value.getClass());
    }
}
