package com.ai.hybridsearch.controller;

import com.ai.hybridsearch.dto.PortalMenuDto;
import com.ai.hybridsearch.service.PortalMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portal-menus")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PortalMenuController {

    private final PortalMenuService portalMenuService;

    @GetMapping
    public ResponseEntity<List<PortalMenuDto>> getPortalMenus(@RequestParam String portalId) {
        return ResponseEntity.ok(portalMenuService.getPortalMenus(portalId));
    }
}