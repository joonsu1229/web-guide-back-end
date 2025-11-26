package com.webguide.search.service.impl;

import com.webguide.search.dto.NoticeCategoryDto;
import com.webguide.search.repository.NoticeCategoryRepository;
import com.webguide.search.service.NoticeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeCategoryServiceImpl implements NoticeCategoryService {

    private final NoticeCategoryRepository repository;

    @Override
    public List<NoticeCategoryDto> getAllCategories() {
        return repository.findAll()
                .stream()
                .map(NoticeCategoryDto::fromEntity)
                .toList();
    }
}
