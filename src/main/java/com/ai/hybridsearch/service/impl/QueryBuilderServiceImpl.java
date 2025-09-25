package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.config.AiModelConfig;
import com.ai.hybridsearch.service.QueryBuilderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryBuilderServiceImpl implements QueryBuilderService {

    private final AiModelConfig aiModelConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ChatLanguageModel chatModel;

    @Override
    public TransformedQuery transformQuery(String userQuery) {
        String prompt = createTransformPrompt(userQuery);
        try {
            String response = chatModel.generate(prompt);
            String jsonResponse = response.substring(response.indexOf("{"), response.lastIndexOf("}") + 1);
            TransformedQuery transformedQuery = objectMapper.readValue(jsonResponse, TransformedQuery.class);
            transformedQuery.setOriginalQuery(userQuery);
            log.info("쿼리 변환 완료: {} -> Lexical: '{}', Semantic: '{}'", userQuery, transformedQuery.getLexicalQuery(), transformedQuery.getSemanticQuery());
            return transformedQuery;
        } catch (Exception e) {
            log.error("쿼리 변환 실패. 원본 쿼리를 사용합니다.", e);
            TransformedQuery fallback = new TransformedQuery();
            fallback.setLexicalQuery(userQuery);
            fallback.setSemanticQuery(userQuery);
            fallback.setOriginalQuery(userQuery);
            return fallback;
        }
    }

    private String createTransformPrompt(String userQuery) {
        return String.format("""
            당신은 사용자의 질문을 분석하여 하이브리드 검색 시스템에 최적화된 쿼리를 생성하는 전문가입니다.
            사용자의 질문을 분석하여 두 가지 형태의 쿼리를 생성해주세요:
            1.  `lexicalQuery`: PostgreSQL의 `plainto_tsquery` 함수에 사용될 키워드 기반 쿼리. 핵심 명사, 기술 용어 등을 추출하여 `&` (AND) 또는 `|` (OR) 연산자로 조합합니다.
            2.  `semanticQuery`: 벡터 임베딩 모델에 사용될 문장형 쿼리. 질문의 핵심 의도와 맥락을 가장 잘 나타내는 완전한 문장으로 재구성합니다. 사용자가 찾으려는 정보가 담긴 이상적인 문서의 한 문단을 상상하며 작성하세요.
            
            규칙:
            - 반드시 JSON 객체 형식으로만 응답해야 합니다.
            - 다른 설명이나 텍스트를 포함하지 마세요.
            - `lexicalQuery`는 간결한 키워드 조합이어야 합니다.
            - `semanticQuery`는 자연스러운 서술형 문장이어야 합니다.
            
            사용자 질문: "%s"
            
            응답 예시:
            {
                "lexicalQuery": "자바 & 스프링 & JPA & 백엔드",
                "semanticQuery": "Java와 Spring Framework, JPA 기술을 사용하여 대규모 트래픽을 처리하는 백엔드 시스템을 개발하는 경력직 채용 정보"
            }
            
            이제 위 규칙에 따라 다음 질문에 대한 JSON 응답을 생성해주세요.
            질문: "%s"
            """, userQuery, userQuery);
    }
}