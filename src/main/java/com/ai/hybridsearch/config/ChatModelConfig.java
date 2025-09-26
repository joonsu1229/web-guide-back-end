package com.ai.hybridsearch.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
// 0.36.2 버전에서는 'GoogleAiGeminiChatModel' 클래스를 사용해야 합니다.
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ChatModelConfig {

    private final AiModelConfig aiModelConfig;

    private String getModelType() {
        String modelType = aiModelConfig.getAiModelType();
        if (!StringUtils.hasText(modelType)) {
            throw new IllegalArgumentException("`langchain.model-type` property must be set in application configuration.");
        }
        return modelType.toLowerCase();
    }

    @Bean
    @Qualifier("queryChatModel")
    public ChatLanguageModel queryChatModel() {
        log.info("Initializing Query Chat Model with type: {}", getModelType());
        switch (getModelType()) {
            case "gemini":
                AiModelConfig.GeminiConfig config = aiModelConfig.getGemini();
                // 0.36.2 버전에 맞는 정확한 클래스명 'GoogleAiGeminiChatModel'을 사용합니다.
                return GoogleAiGeminiChatModel.builder()
                        .apiKey(config.getApiKey())
                        .modelName(config.getAiChatModel())
                        .temperature(0.3)
                        .maxOutputTokens(500)
                        .timeout(Duration.ofSeconds(30))
                        .build();
            case "openai":
                 AiModelConfig.OpenAiConfig oaiConfig = aiModelConfig.getOpenai();
                 return OpenAiChatModel.builder()
                        .apiKey(oaiConfig.getApiKey())
                        .modelName(oaiConfig.getAiChatModel())
                        .maxTokens(500)
                        .timeout(Duration.ofSeconds(30))
                        .temperature(0.3)
                        .build();
            default:
                throw new IllegalArgumentException("Unsupported model type for queryChatModel: " + getModelType());
        }
    }
}

