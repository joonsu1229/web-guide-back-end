package com.ai.hybridsearch;

import com.ai.hybridsearch.config.AiModelConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import org.springframework.cache.annotation.EnableCaching;

import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableConfigurationProperties(AiModelConfig.class)
@EnableCaching
@EnableAsync
public class HybridSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(HybridSearchApplication.class, args);
    }
}