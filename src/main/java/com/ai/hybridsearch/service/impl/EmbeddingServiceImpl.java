package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.config.AiModelConfig;
import com.ai.hybridsearch.service.EmbeddingService;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.googleai.GoogleAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingServiceImpl implements EmbeddingService {

    private final AiModelConfig config;
    private EmbeddingModel embeddingModel;      // 문서 임베딩용 모델
    private EmbeddingModel queryEmbeddingModel; // 질문(쿼리) 임베딩용 모델

    @PostConstruct
    public void init() {
        try {
            log.info("=== EmbeddingService 초기화 시작 ===");
            log.info("사용 모델 타입: {}, 타겟 차원: {}", config.getModelType(), config.getTargetDimensions());

            switch (config.getModelType().toLowerCase()) {
                case "onnx":
                    initOnnxModel();
                    break;
                case "openai":
                    validateOpenAiConfig();
                    initOpenAiModel();
                    break;
                case "gemini":
                    validateGeminiConfig();
                    initGeminiModel();
                    break;
                default:
                    throw new IllegalArgumentException("지원 안 하는 모델 타입: " + config.getModelType());
            }
            log.info("=== EmbeddingService 초기화 완료 ===");
        } catch (Exception e) {
            log.error("=== EmbeddingService 초기화 실패 ===", e);
            throw e; // 서버 시작을 막기 위해 예외를 다시 던짐
        }
    }

    // --- 모델 초기화 로직 ---

    private void initOnnxModel() {
        log.info("ONNX 모델 생성 시작...");
        embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        queryEmbeddingModel = embeddingModel; // ONNX는 문서/쿼리 모델이 동일
        log.info("ONNX 모델 생성 완료");
    }

    private void initOpenAiModel() {
        log.info("OpenAI 모델 생성 시작...");
        var builder = OpenAiEmbeddingModel.builder()
                .apiKey(config.getOpenai().getApiKey())
                .dimensions(config.getTargetDimensions());

        if (config.getOpenai().getEmbeddingModel() != null) {
            builder.modelName(config.getOpenai().getEmbeddingModel());
        }

        embeddingModel = builder.build();
        queryEmbeddingModel = embeddingModel; // OpenAI도 보통 문서/쿼리 모델이 동일
        log.info("OpenAI 모델 생성 완료 - Model: {}", config.getOpenai().getEmbeddingModel() != null ? config.getOpenai().getEmbeddingModel() : "default");
    }

    private void initGeminiModel() {
        log.info("Gemini 모델 생성 시작 (문서/질문용 동시 생성)...");
        var geminiConfig = config.getGemini();

        // 1. 문서 임베딩용 모델 (RETRIEVAL_DOCUMENT)
        embeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(geminiConfig.getApiKey())
                .modelName(geminiConfig.getEmbeddingModel())
                .taskType(GoogleAiEmbeddingModel.TaskType.RETRIEVAL_DOCUMENT)
                .build();

        // 2. 질문 임베딩용 모델 (RETRIEVAL_QUERY)
        queryEmbeddingModel = GoogleAiEmbeddingModel.builder()
                .apiKey(geminiConfig.getApiKey())
                .modelName(geminiConfig.getEmbeddingModel())
                .taskType(GoogleAiEmbeddingModel.TaskType.RETRIEVAL_QUERY)
                .build();
        log.info("Gemini 문서/질문 임베딩 모델 생성 완료");
    }

    // --- 설정 값 검증 로직 ---

    private void validateOpenAiConfig() {
        if (config.getOpenai() == null || config.getOpenai().getApiKey() == null || config.getOpenai().getApiKey().isBlank()) {
            throw new IllegalArgumentException("OpenAI API Key 설정이 필요함.");
        }
    }

    private void validateGeminiConfig() {
        if (config.getGemini() == null || config.getGemini().getApiKey() == null || config.getGemini().getApiKey().isBlank()) {
            throw new IllegalArgumentException("Gemini API Key 설정이 필요함.");
        }
    }

    // --- 인터페이스 구현 메서드 ---

    @Override
    public List<Embedding> embedAll(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<TextSegment> segments = texts.stream().map(TextSegment::from).collect(Collectors.toList());
            return embeddingModel.embedAll(segments).content();
        } catch (Exception e) {
            log.error("배치 임베딩 생성 실패", e);
            throw new RuntimeException("배치 임베딩 생성 중 오류 발생", e);
        }
    }

    @Override
    public Embedding generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("임베딩할 텍스트가 비어있음.");
        }
        try {
            return embeddingModel.embed(text).content();
        } catch (Exception e) {
            log.error("문서 임베딩 생성 실패 - Text: {}", text, e);
            throw new RuntimeException("문서 임베딩 생성 중 오류 발생", e);
        }
    }

    @Override
    public float[] embed(String text) {
        return generateEmbedding(text).vector();
    }

    @Override
    @Cacheable("query-embeddings") // 같은 질문은 캐시된 임베딩 값 사용
    public float[] embedQuery(String query) {
        return embedQueryToEmbedding(query).vector();
    }

    @Override
    @Cacheable("query-embeddings-obj") // 캐시 분리
    public Embedding embedQueryToEmbedding(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("임베딩할 질문 텍스트가 비어있음.");
        }
        try {
            // Gemini는 질문 전용 모델, 그 외에는 공용 모델 사용
            EmbeddingModel modelToUse = (queryEmbeddingModel != null) ? queryEmbeddingModel : embeddingModel;
            return modelToUse.embed(query).content();
        } catch (Exception e) {
            log.error("질문 임베딩 생성 실패 - Text: {}", query, e);
            throw new RuntimeException("질문 임베딩 생성 중 오류 발생", e);
        }
    }

    @Override
    public double cosineSimilarity(Embedding embedding1, Embedding embedding2) {
        float[] vector1 = embedding1.vector();
        float[] vector2 = embedding2.vector();

        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("임베딩 벡터의 차원이 서로 다름.");
        }

        double dotProduct = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            normA += vector1[i] * vector1[i];
            normB += vector2[i] * vector2[i];
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        // 분모가 0인 경우, 유사도도 0으로 처리
        return (denominator == 0.0) ? 0.0 : dotProduct / denominator;
    }
}