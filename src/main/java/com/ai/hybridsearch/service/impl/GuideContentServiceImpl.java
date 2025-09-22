package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.dto.GuideContentDto;
import com.ai.hybridsearch.entity.Category;
import com.ai.hybridsearch.entity.Guide;
import com.ai.hybridsearch.entity.GuideVersion;
import com.ai.hybridsearch.repository.CategoryRepository;
import com.ai.hybridsearch.repository.GuideRepository;
import com.ai.hybridsearch.repository.GuideVersionRepository;
import com.ai.hybridsearch.service.EmbeddingService; // 1. EmbeddingService 임포트
import com.ai.hybridsearch.service.GuideContentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j // 2. 로깅을 위해 Slf4j 추가
@Service
@RequiredArgsConstructor
public class GuideContentServiceImpl implements GuideContentService {

    private final GuideRepository guideRepository;
    private final GuideVersionRepository guideVersionRepository;
    private final CategoryRepository categoryRepository;
    private final EmbeddingService embeddingService; // 3. EmbeddingService 주입

    @Override
    @Transactional(readOnly = true)
    public GuideContentDto getCurrentContent(String portalId, Long categoryId) {
        return guideRepository.findCurrentContentNative(categoryId, portalId)
                .orElse(GuideContentDto.empty(categoryId));
    }

    @Override
    @Transactional
    public GuideContentDto saveNewVersion(String portalId, Long categoryId, String contentBody) {
        Category category = categoryRepository.findByIdAndPortalId(categoryId, portalId)
            .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        Guide guide = guideRepository.findByCategoryIdAndPortalId(categoryId, portalId)
            .orElseGet(() -> {
                Guide newGuide = new Guide();
                newGuide.setCategory(category);
                newGuide.setPortalId(portalId);
                return guideRepository.save(newGuide);
            });

        if (guide.isDeleteYn()) {
            guide.setDeleteYn(false);
        }

        int nextVersionNum = guideVersionRepository.findLatestVersionByGuideId(guide.getId(), PageRequest.of(0, 1))
            .stream()      // List를 Stream으로 변환
            .findFirst()   // Stream의 첫 번째 값을 Optional<Integer>로 가져옴
            .map(latestVersion -> latestVersion + 1)
            .orElse(1);

        GuideVersion newVersion = new GuideVersion();
        newVersion.setGuide(guide);
        newVersion.setVersion(nextVersionNum);
        newVersion.setContentBody(contentBody);

        // 4. 임베딩 없이 먼저 버전을 저장하여 ID를 확보합니다.
        GuideVersion savedVersion = guideVersionRepository.save(newVersion);

        try {
            // 5. 저장된 콘텐츠로 임베딩을 생성합니다.
            log.info("가이드 버전 ID {}에 대한 임베딩 생성을 시작합니다.", savedVersion.getId());
            float[] embeddingArray = embeddingService.embed(savedVersion.getContentBody());
            String vectorStr = floatArrayToVectorString(embeddingArray);

            // 6. Repository의 네이티브 쿼리를 호출하여 임베딩을 업데이트합니다.
            guideVersionRepository.updateEmbedding(savedVersion.getId(), vectorStr);
            log.info("가이드 버전 ID {}에 대한 임베딩 업데이트가 완료되었습니다.", savedVersion.getId());

        } catch (Exception e) {
            log.error("가이드 버전 ID {}의 임베딩 생성 또는 업데이트에 실패했습니다.", savedVersion.getId(), e);
            // 필요에 따라 예외 처리 로직 추가
        }

        // 7. 가이드의 현재 버전을 새로 저장된 버전으로 업데이트합니다.
        guideRepository.updateCurrentVersion(guide.getId(), savedVersion.getVersion());

        return new GuideContentDto(
            savedVersion.getId(),
            savedVersion.getContentBody(),
            savedVersion.getVersion(),
            categoryId
        );
    }

    @Override
    @Transactional
    public void softDeleteGuide(String portalId, Long categoryId) {
        Guide guide = guideRepository.findActiveGuide(categoryId, portalId)
            .orElseThrow(() -> new EntityNotFoundException("삭제할 가이드를 찾을 수 없습니다."));

        guideRepository.softDeleteById(guide.getId(), portalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuideContentDto> getVersionHistory(String portalId, Long categoryId) {
        Guide guide = guideRepository.findByCategoryIdAndPortalId(categoryId, portalId)
                .orElseThrow(() -> new EntityNotFoundException("가이드 기록을 찾을 수 없습니다."));

        return guideVersionRepository.findHistoryDtoByGuideId(guide.getId());
    }

    /**
     * float 배열을 pgvector가 인식하는 문자열 형식 "[f1,f2,...]"으로 변환합니다.
     */
    private String floatArrayToVectorString(float[] array) {
        if (array == null) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}