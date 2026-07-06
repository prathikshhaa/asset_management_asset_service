package com.assetmanagement.assetservice.controller;

import com.assetmanagement.assetservice.dto.AssetSummaryDTO;
import com.assetmanagement.assetservice.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/assets/internal")
@RequiredArgsConstructor
public class InternalAssetController {

    private final AssetService assetService;

    @GetMapping("/list")
    public List<AssetSummaryDTO> getAssetsForInternalUse() {
        return assetService.getAllAssetsSummary();
    }
}