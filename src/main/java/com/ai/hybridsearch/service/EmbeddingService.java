package com.ai.hybridsearch.service;

import dev.langchain4j.data.embedding.Embedding;
import java.util.List;

/**
 * 텍스트를 벡터로 변환하는 임베딩 관련 기능을 정의하는 인터페이스.
 * 모델 종류(OpenAI, Gemini 등)에 상관없이 일관된 기능을 제공해야 함.
 */
public interface EmbeddingService {

    /**
     * 여러 개의 텍스트를 한 번의 호출로 임베딩. (문서 임베딩용)
     * 배치 처리를 통해 성능을 최적화.
     *
     * @param texts 임베딩할 텍스트 목록
     * @return 생성된 임베딩 객체 목록
     */
    List<Embedding> embedAll(List<String> texts);

    /**
     * 단일 텍스트를 임베딩하여 Embedding 객체로 반환. (주로 문서용)
     *
     * @param text 임베딩할 텍스트
     * @return 생성된 임베딩 객체
     */
    Embedding generateEmbedding(String text);

    /**
     * 단일 텍스트를 임베딩하여 float 배열 벡터로 반환. (주로 문서용)
     *
     * @param text 임베딩할 텍스트
     * @return 생성된 float 배열 벡터
     */
    float[] embed(String text);

    /**
     * 단일 '질문(Query)'을 임베딩하여 float 배열 벡터로 반환.
     * Gemini 같이 질문/문서 모델이 분리된 경우를 대비.
     *
     * @param query 임베딩할 질문 텍스트
     * @return 생성된 float 배열 벡터
     */
    float[] embedQuery(String query);

    /**
     * 단일 '질문(Query)'을 임베딩하여 Embedding 객체로 반환.
     * Reranker 등에서 코사인 유사도 계산 시 활용.
     *
     * @param query 임베딩할 질문 텍스트
     * @return 생성된 임베딩 객체
     */
    Embedding embedQueryToEmbedding(String query);

    /**
     * 두 임베딩 벡터 간의 코사인 유사도를 계산.
     *
     * @param embedding1 첫 번째 임베딩
     * @param embedding2 두 번째 임베딩
     * @return 0과 1 사이의 코사인 유사도 값
     */
    double cosineSimilarity(Embedding embedding1, Embedding embedding2);
}