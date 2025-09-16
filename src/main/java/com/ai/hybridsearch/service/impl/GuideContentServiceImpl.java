package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.dto.GuideContentDto;
import com.ai.hybridsearch.entity.Category;
import com.ai.hybridsearch.entity.Guide;
import com.ai.hybridsearch.entity.GuideVersion;
import com.ai.hybridsearch.repository.CategoryRepository;
import com.ai.hybridsearch.repository.GuideRepository;
import com.ai.hybridsearch.repository.GuideVersionRepository;
import com.ai.hybridsearch.service.GuideContentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuideContentServiceImpl implements GuideContentService {

    private final GuideRepository guideRepository;
    private final GuideVersionRepository guideVersionRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public GuideContentDto getCurrentContent(String portalId, Long categoryId) {
        return guideRepository.findCurrentContentNative(categoryId, portalId)
                .orElseThrow(() -> new EntityNotFoundException("활성화된 가이드 콘텐츠를 찾을 수 없습니다."));
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

        int nextVersionNum = guideVersionRepository.findTopByGuideIdOrderByVersionDesc(guide.getId())
            .map(lastVersion -> lastVersion.getVersion() + 1)
            .orElse(1);

        GuideVersion newVersion = new GuideVersion();
        newVersion.setGuide(guide);
        newVersion.setVersion(nextVersionNum);
        newVersion.setContentBody(contentBody);
        GuideVersion savedVersion = guideVersionRepository.save(newVersion);

        guideRepository.updateCurrentVersion(guide.getId(), savedVersion.getId());

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
}