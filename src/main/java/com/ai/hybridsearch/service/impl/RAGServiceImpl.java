package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.config.AiModelConfig;
import com.ai.hybridsearch.dto.SearchResult;
import com.ai.hybridsearch.entity.Document;
import com.ai.hybridsearch.service.RAGService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RAGServiceImpl implements RAGService {

    private final AiModelConfig aiModelConfig;
    private ChatLanguageModel chatModel;

    @PostConstruct
    public void init() {
        if ("gemini".equalsIgnoreCase(aiModelConfig.getAiModelType())) {
            log.info("=== RAGService 초기화 (Gemini) ===");
            AiModelConfig.GeminiConfig geminiConfig = aiModelConfig.getGemini();
            this.chatModel = GoogleAiGeminiChatModel.builder()
                    .apiKey(geminiConfig.getApiKey())
                    .modelName(geminiConfig.getAiChatModel())
                    .maxOutputTokens(aiModelConfig.getGemini().getOutputMaxToken())
                    .timeout(Duration.ofSeconds(60))
                    .temperature(0.3)
                    .build();
        }
    }

    @Override
    public GeneratedResponse generate(String originalQuery, List<SearchResult> contextDocs) {
        String prompt = createGenerationPrompt(originalQuery, contextDocs);
        String answer = "답변을 생성하지 못했습니다.";
        try {
            log.info("Gemini 답변 생성 시작. 컨텍스트 문서 {}개", contextDocs.size());
            answer = chatModel.generate(prompt);
        } catch (Exception e) {
            log.error("Gemini 답변 생성 중 오류 발생", e);
        }

        GeneratedResponse response = new GeneratedResponse();
        response.setAnswer(answer);
        response.setSources(contextDocs.stream().map(SearchResult::getDocument).collect(Collectors.toList()));
        return response;
    }

    private String createGenerationPrompt(String query, List<SearchResult> searchResults) {
        String context = searchResults.stream()
                .map(result -> String.format(
                        "--- 문서 ID: %d ---\n내용: %s\n",
                        result.getDocument().getId(),
                        result.getDocument().getContentBody()))
                .collect(Collectors.joining("\n"));

        return String.format("""
            당신은 주어진 '문서' 내용만을 활용하여 사용자의 '질문'에 대해 답변하는 AI 어시스턴트입니다.
            
            중요 규칙:
            1.  답변은 반드시 주어진 '문서'에 있는 정보에만 근거해야 합니다.
            2.  문서에 내용이 없거나 질문과 관련 없는 정보는 절대로 언급하지 마세요. "정보를 찾을 수 없습니다."라고 답변하세요.
            3.  답변은 친절하고 명확한 한국어 문장으로 정리하여 제공하세요.
            4.  답변의 각 문장 끝에, 근거가 된 문서의 ID를 `[doc: ID]` 형식으로 명시하세요.
            5.  여러 문서 내용을 종합하여 답변할 수 있습니다.
            
            --- 문서 ---
            %s
            
            --- 질문 ---
            %s
            
            --- 답변 ---
            """, context, query);
    }
}