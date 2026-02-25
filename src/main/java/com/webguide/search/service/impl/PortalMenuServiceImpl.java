package com.webguide.search.service.impl;

import com.webguide.search.dto.PortalMenuDto;
import com.webguide.search.entity.PortalMenu;
import com.webguide.search.repository.PortalMenuRepository;
import com.webguide.search.service.PortalMenuService;
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

    @Override
    @Transactional
    public PortalMenuDto createPortalMenu(PortalMenuDto menuDto) {
        PortalMenu menu = new PortalMenu();
        updateMenuFields(menu, menuDto);
        menu.setPortalId(menuDto.getPortalId());
        menu.setActive(true);
        
        return PortalMenuDto.fromEntity(portalMenuRepository.save(menu));
    }

    @Override
    @Transactional
    public PortalMenuDto updatePortalMenu(Long menuId, PortalMenuDto menuDto) {
        PortalMenu menu = portalMenuRepository.findById(menuId)
                .orElseThrow(() -> new EntityNotFoundException("수정할 메뉴를 찾을 수 없습니다: " + menuId));
        
        updateMenuFields(menu, menuDto);
        
        return PortalMenuDto.fromEntity(portalMenuRepository.save(menu));
    }

    @Override
    @Transactional
    public void deactivatePortalMenu(Long menuId, String portalId) {
        PortalMenu menu = portalMenuRepository.findByIdAndPortalId(menuId, portalId)
                .orElseThrow(() -> new EntityNotFoundException("비활성화할 메뉴를 찾을 수 없습니다: " + menuId));
        
        menu.setActive(false);
        portalMenuRepository.save(menu);
    }

    private void updateMenuFields(PortalMenu menu, PortalMenuDto dto) {
        menu.setTitle(dto.getTitle());
        menu.setDescription(dto.getDescription());
        menu.setIcon(dto.getIcon());
        menu.setClassName(dto.getClassName());
        menu.setSection(dto.getSection());
        
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            menu.setTags(String.join(",", dto.getTags()));
        } else {
            menu.setTags(null);
        }
    }
}