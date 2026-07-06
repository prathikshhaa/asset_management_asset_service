package com.assetmanagement.assetservice.controller;

import com.assetmanagement.assetservice.dto.*;
import com.assetmanagement.assetservice.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public ResponseEntity<ApiResponse<AssetResponseDTO>> createAsset(
            @Valid @RequestBody AssetRequestDTO requestDTO) {

        AssetResponseDTO created = assetService.createAsset(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Asset created successfully", created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponseDTO<AssetResponseDTO>>> getAllAssets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "assetName") String sortBy) {
        int internalPage = page > 0 ? page - 1 : 0;
        Page<AssetResponseDTO> assets = assetService.getAllAssets(internalPage, size, sortBy);
        PagedResponseDTO<AssetResponseDTO> cleanResponse = PagedResponseDTO.from(assets);
        return ResponseEntity.ok(
                ApiResponse.success("Assets fetched successfully", cleanResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponseDTO>> getAssetById(
            @PathVariable UUID id) {

        AssetResponseDTO asset = assetService.getAssetById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Asset fetched successfully", asset));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponseDTO<AssetResponseDTO>>> getByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "assetName") String sortBy) {

        int internalPage = page > 0 ? page - 1 : 0;
        Page<AssetResponseDTO> assets = assetService.getAssetsByStatus(status, internalPage, size, sortBy);

        return ResponseEntity.ok(
                ApiResponse.success("Assets fetched successfully", PagedResponseDTO.from(assets)));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<PagedResponseDTO<AssetResponseDTO>>> getByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "assetName") String sortBy) {

        int internalPage = page > 0 ? page - 1 : 0;
        Page<AssetResponseDTO> assets = assetService.getAssetsByType(type, internalPage, size, sortBy);

        return ResponseEntity.ok(
                ApiResponse.success("Assets fetched successfully", PagedResponseDTO.from(assets)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponseDTO<AssetResponseDTO>>> searchAssets(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "assetName") String sortBy) {

        int internalPage = page > 0 ? page - 1 : 0;
        Page<AssetResponseDTO> results = assetService.searchAssets(keyword, internalPage, size, sortBy);

        return ResponseEntity.ok(
                ApiResponse.success("Search results fetched successfully", PagedResponseDTO.from(results)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponseDTO>> updateAsset(
            @PathVariable UUID id,
            @Valid @RequestBody AssetRequestDTO requestDTO) {

        AssetResponseDTO updated = assetService.updateAsset(id, requestDTO);

        return ResponseEntity.ok(
                ApiResponse.success("Asset updated successfully", updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponseDTO>> patchAsset(
            @PathVariable UUID id,
            @RequestBody AssetUpdateDTO updateDTO) {

        AssetResponseDTO updated = assetService.patchAsset(id, updateDTO);

        return ResponseEntity.ok(
                ApiResponse.success("Asset updated successfully", updated));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AssetResponseDTO>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AssetStatusUpdateDTO statusUpdateDTO) {

        AssetResponseDTO updated = assetService.updateAssetStatus(id, statusUpdateDTO);

        return ResponseEntity.ok(
                ApiResponse.success("Asset status updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(
            @PathVariable UUID id) {

        assetService.deleteAsset(id);

        return ResponseEntity.ok(
                ApiResponse.success("Asset deleted successfully", null));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<com.assetmanagement.assetservice.dto.BulkUploadResultDTO>> bulkCreateAssets(
            @RequestBody List<AssetRequestDTO> requests) {

        com.assetmanagement.assetservice.dto.BulkUploadResultDTO result =
                assetService.bulkCreateAssets(requests);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        result.getSuccessCount() + " of " + result.getTotalReceived() + " assets created",
                        result));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BulkUploadResultDTO>> uploadAssets(
            @RequestPart("file") MultipartFile file) {

        BulkUploadResultDTO result = assetService.bulkCreateAssetsFromExcel(file);
        return ResponseEntity.ok(
                ApiResponse.success("File processed successfully", result));
    }


    @GetMapping("/sumhistory")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary() {

        DashboardSummaryDTO summary = assetService.getDashboardSummary();

        return ResponseEntity.ok(
                ApiResponse.success("Summary fetched successfully", summary));
    }
}