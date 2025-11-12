package com.webguide.search.service.impl;

import com.webguide.search.dto.GuideContentDto;
import com.webguide.search.entity.Category;
import com.webguide.search.entity.Guide;
import com.webguide.search.entity.GuideVersion;
import com.webguide.search.exception.ResourceNotFoundException;
import com.webguide.search.repository.CategoryRepository;
import com.webguide.search.repository.GuideRepository;
import com.webguide.search.repository.GuideVersionRepository;
import com.webguide.search.service.GuideContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
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
                .orElseThrow(() -> new ResourceNotFoundException("해당 카테고리의 가이드 콘텐츠를 찾을 수 없습니다. CategoryId: " + categoryId));
    }

    @Override
    @Transactional
    public GuideContentDto saveNewVersion(String portalId, GuideContentDto guideContentDto) {
        Category category = categoryRepository.findById(guideContentDto.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다. CategoryId: " + guideContentDto.getCategoryId()));

        Guide guide = guideRepository.findByCategoryIdAndPortalId(guideContentDto.getCategoryId(), portalId)
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
            .stream()
            .findFirst()
            .map(latestVersion -> latestVersion + 1)
            .orElse(1);

        GuideVersion newVersion = new GuideVersion();
        newVersion.setGuide(guide);
        newVersion.setVersion(nextVersionNum);
        newVersion.setContentBody(guideContentDto.getContentBody());

        GuideVersion savedVersion = guideVersionRepository.save(newVersion);

        guideRepository.updateCurrentVersion(guide.getId(), savedVersion.getId());

        return new GuideContentDto(
            savedVersion.getId(),
            savedVersion.getContentBody(),
            savedVersion.getVersion(),
            guideContentDto.getCategoryId()
        );
    }

    @Override
    @Transactional
    public void softDeleteGuide(String portalId, Long categoryId) {
        Guide guide = guideRepository.findActiveGuideWithCategory(categoryId, portalId)
            .orElseThrow(() -> new ResourceNotFoundException("삭제할 가이드를 찾을 수 없습니다. CategoryId: " + categoryId));

        guideRepository.softDeleteById(guide.getId(), portalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuideContentDto> getVersionHistory(String portalId, Long categoryId) {
        Guide guide = guideRepository.findByCategoryIdAndPortalId(categoryId, portalId)
                .orElseThrow(() -> new ResourceNotFoundException("가이드 기록을 찾을 수 없습니다. CategoryId: " + categoryId));

        return guideVersionRepository.findHistoryDtoByGuideId(guide.getId());
    }
}