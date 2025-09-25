package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.dto.GuideContentDto;
import com.ai.hybridsearch.entity.Category;
import com.ai.hybridsearch.entity.Guide;
import com.ai.hybridsearch.entity.GuideVersion;
import com.ai.hybridsearch.exception.ResourceNotFoundException;
import com.ai.hybridsearch.repository.CategoryRepository;
import com.ai.hybridsearch.repository.GuideRepository;
import com.ai.hybridsearch.repository.GuideVersionRepository;
import com.ai.hybridsearch.service.EmbeddingService;
import com.ai.hybridsearch.service.GuideContentService;
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
    private final EmbeddingService embeddingService;

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

        // 4가지 정보를 조합하여 임베딩할 텍스트 생성
        String textToEmbed = String.join("\n\n",
                guideContentDto.getMenu() != null ? guideContentDto.getMenu() : "",
                guideContentDto.getTitle() != null ? guideContentDto.getTitle() : "",
                guideContentDto.getDescription() != null ? guideContentDto.getDescription() : "",
                guideContentDto.getContentBody() != null ? guideContentDto.getContentBody() : ""
        );

        // 비동기적으로 임베딩 및 업데이트 처리
        updateEmbeddingAsync(savedVersion.getId(), textToEmbed);

        //guideRepository.updateCurrentVersion(guide.getId(), savedVersion.getId());

        return new GuideContentDto(
            savedVersion.getId(),
            savedVersion.getContentBody(),
            savedVersion.getVersion(),
            guideContentDto.getCategoryId()
        );
    }

    @Async
    @Transactional
    public void updateEmbeddingAsync(Long versionId, String textToEmbed) {
        try {
            log.info("가이드 버전 ID {}에 대한 비동기 임베딩 생성을 시작합니다.", versionId);
            float[] embeddingArray = embeddingService.embed(textToEmbed);
            String vectorStr = Arrays.toString(embeddingArray);

            Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(textToEmbed);
            List<String> meaningfulTokens = JavaConverters.seqAsJavaList(tokens).stream()
                    .filter(token -> {
                        String text = token.text().trim();
                        if (text.isEmpty() || text.matches("[\\p{Punct}\\d]+")) return false;
                        String pos = token.pos().toString();
                        return !pos.equals("Space") && !pos.equals("Josa") && !pos.equals("Punctuation");
                    })
                    .map(KoreanTokenizer.KoreanToken::text)
                    .collect(Collectors.toList());

            String searchVector = String.join(" ", meaningfulTokens);
            guideVersionRepository.updateEmbedding(versionId, vectorStr, searchVector);
            log.info("가이드 버전 ID {}에 대한 비동기 임베딩 업데이트가 완료되었습니다.", versionId);

        } catch (Exception e) {
            log.error("가이드 버전 ID {}의 비동기 임베딩 생성 또는 업데이트에 실패했습니다.", versionId, e);
        }
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