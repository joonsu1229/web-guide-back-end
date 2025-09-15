package com.ai.hybridsearch.service.impl;

import com.ai.hybridsearch.dto.PortalMenuDto;
import com.ai.hybridsearch.entity.PortalMenu;
import com.ai.hybridsearch.repository.PortalMenuRepository;
import com.ai.hybridsearch.service.PortalMenuService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalMenuServiceImpl implements PortalMenuService {

    private final PortalMenuRepository portalMenuRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PortalMenuDto> getPortalMenus(String portalId) {
        // Repository에 정의된 JPA 쿼리 메서드 사용.
        return portalMenuRepository.findByPortalIdOrderByDisplayOrderAsc(portalId)
                .stream()
                .map(PortalMenuDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PortalMenuDto getPortalMenu(String portalId, Long menuId) {
        // portalId와 id로 조회하는 Repository 메서드 사용.
        PortalMenu portalMenu = portalMenuRepository.findByIdAndPortalId(menuId, portalId)
                .orElseThrow(() -> new EntityNotFoundException("메뉴를 찾을 수 없습니다: " + menuId));
        return PortalMenuDto.fromEntity(portalMenu);
    }
}