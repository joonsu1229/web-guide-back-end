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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuideContentServiceImpl implements GuideContentService {

    private final GuideRepository guideRepository;
    private final GuideVersionRepository guideVersionRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public GuideContentDto getCurrentContent(String portalId, Long categoryId) {
        // '삭제되지 않은' 가이드를 조회하는 Repository 메서드 호출
        Guide guide = guideRepository.findActiveGuide(categoryId, portalId)
                .orElseThrow(() -> new EntityNotFoundException("활성화된 가이드를 찾을 수 없습니다."));

        GuideVersion currentVersion = guide.getCurrentVersion();
        if (currentVersion == null) {
            throw new EntityNotFoundException("표시할 버전이 없습니다.");
        }
        return GuideContentDto.fromEntity(currentVersion);
    }

    @Override
    @Transactional
    public GuideContentDto saveNewVersion(String portalId, Long categoryId, String contentBody) {
        Category category = categoryRepository.findByIdAndPortalId(categoryId, portalId)
            .orElseThrow(() -> new EntityNotFoundException("카테고리를 찾을 수 없습니다."));

        // 삭제된 가이드도 포함하여 조회 후, 없으면 새로 생성
        Guide guide = guideRepository.findByCategoryIdAndPortalId(categoryId, portalId)
            .orElseGet(() -> {
                Guide newGuide = new Guide();
                newGuide.setCategory(category);
                newGuide.setPortalId(portalId);
                return guideRepository.save(newGuide);
            });

        // 만약 소프트 삭제된 가이드였다면, 다시 활성화
        if (guide.isDeleteYn()) {
            guide.setDeleteYn(false);
        }

        // 최신 버전 번호를 가져와 +1
        int nextVersionNum = guideVersionRepository.findTopByGuideIdOrderByVersionDesc(guide.getId())
            .map(lastVersion -> lastVersion.getVersion() + 1)
            .orElse(1);

        // 새 버전 엔티티 생성 및 저장
        GuideVersion newVersion = new GuideVersion();
        newVersion.setGuide(guide);
        newVersion.setVersion(nextVersionNum);
        newVersion.setContentBody(contentBody);
        GuideVersion savedVersion = guideVersionRepository.save(newVersion);

        // Guide의 current_version_id를 업데이트하는 Repository 메서드 호출
        guideRepository.updateCurrentVersion(guide.getId(), savedVersion.getId());

        return GuideContentDto.fromEntity(savedVersion);
    }

    @Override
    @Transactional
    public void softDeleteGuide(String portalId, Long categoryId) {
        Guide guide = guideRepository.findActiveGuide(categoryId, portalId)
            .orElseThrow(() -> new EntityNotFoundException("삭제할 가이드를 찾을 수 없습니다."));

        // 소프트 삭제를 위한 Repository 메서드 호출
        guideRepository.softDeleteById(guide.getId(), portalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GuideContentDto> getVersionHistory(String portalId, Long categoryId) {
        Guide guide = guideRepository.findByCategoryIdAndPortalId(categoryId, portalId)
                .orElseThrow(() -> new EntityNotFoundException("가이드 기록을 찾을 수 없습니다."));

        // 버전 기록 전체를 조회하는 Repository 메서드 호출
        return guideVersionRepository.findByGuideIdOrderByVersionDesc(guide.getId())
                .stream()
                .map(GuideContentDto::fromEntity)
                .collect(Collectors.toList());
    }
}