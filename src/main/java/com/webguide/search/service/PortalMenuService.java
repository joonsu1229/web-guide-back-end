package com.webguide.search.service;

import com.webguide.search.dto.PortalMenuDto;
import java.util.List;

public interface PortalMenuService {

    /**
     * 특정 포탈의 메인 메뉴 전체 조회.
     */
    List<PortalMenuDto> getPortalMenus(String portalId);

    /**
     * 특정 포탈의 메인 메뉴 단건 조회.
     */
    PortalMenuDto getPortalMenu(String portalId, Long menuId);

    /**
     * 포탈 메뉴 생성.
     */
    PortalMenuDto createPortalMenu(PortalMenuDto menuDto);

    /**
     * 포탈 메뉴 수정.
     */
    PortalMenuDto updatePortalMenu(Long menuId, PortalMenuDto menuDto);

    /**
     * 포탈 메뉴 비활성화 (Soft Delete).
     */
    void deactivatePortalMenu(Long menuId, String portalId);
}