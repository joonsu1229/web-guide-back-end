package com.webguide.search.controller;

import com.webguide.search.dto.PortalMenuDto;
import com.webguide.search.service.PortalMenuService;
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

    @PostMapping
    public ResponseEntity<PortalMenuDto> createPortalMenu(@RequestBody PortalMenuDto menuDto) {
        return ResponseEntity.ok(portalMenuService.createPortalMenu(menuDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PortalMenuDto> updatePortalMenu(
            @PathVariable Long id,
            @RequestBody PortalMenuDto menuDto) {
        return ResponseEntity.ok(portalMenuService.updatePortalMenu(id, menuDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortalMenu(
            @PathVariable Long id,
            @RequestParam String portalId) {
        portalMenuService.deactivatePortalMenu(id, portalId);
        return ResponseEntity.noContent().build();
    }
}