package com.assetmanagement.assetservice.controller;

import com.assetmanagement.assetservice.dto.AssetSummaryDTO;
import com.assetmanagement.assetservice.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/assets/internal")
@RequiredArgsConstructor
public class InternalAssetController {

    private final AssetService assetService;

    @GetMapping("/list")
    public List<AssetSummaryDTO> getAssetsForInternalUse() {
        return assetService.getAllAssetsSummary();
    }

    @GetMapping("/{id}")
    public AssetSummaryDTO getAssetSummary(@PathVariable UUID id) {
        return assetService.getAssetSummary(id);
    }
}