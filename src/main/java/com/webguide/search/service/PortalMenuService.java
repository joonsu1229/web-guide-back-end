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
}