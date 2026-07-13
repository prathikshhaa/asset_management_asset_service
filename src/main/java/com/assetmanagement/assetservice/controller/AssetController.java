package com.assetmanagement.assetservice.controller;

import com.assetmanagement.assetservice.dto.*;
import com.assetmanagement.assetservice.service.AssetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.assetmanagement.assetservice.entity.AssetStatus;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final ObjectMapper objectMapper;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<AssetResponseDTO>> createAsset(

            @RequestPart("asset") String assetJson,

            @RequestPart(value = "image", required = false)
            MultipartFile image) throws IOException {

        AssetRequestDTO requestDTO =
                objectMapper.readValue(assetJson, AssetRequestDTO.class);

        AssetResponseDTO created =
                assetService.createAsset(requestDTO, image);

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

        Page<AssetResponseDTO> assets =
                assetService.getAllAssets(internalPage, size, sortBy);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Assets fetched successfully",
                        PagedResponseDTO.from(assets)
                ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponseDTO>> getAssetById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Asset fetched successfully",
                        assetService.getAssetById(id)
                ));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PagedResponseDTO<AssetResponseDTO>>> getByStatus(

            @PathVariable String status,

            @RequestParam(defaultValue = "1") int page,

            @RequestParam(defaultValue = "5") int size,

            @RequestParam(defaultValue = "assetName") String sortBy) {

        int internalPage = page > 0 ? page - 1 : 0;

        Page<AssetResponseDTO> assets =
                assetService.getAssetsByStatus(status, internalPage, size, sortBy);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Assets fetched successfully",
                        PagedResponseDTO.from(assets)
                ));
    }


    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<PagedResponseDTO<AssetResponseDTO>>> getByType(

            @PathVariable String type,

            @RequestParam(defaultValue = "1") int page,

            @RequestParam(defaultValue = "5") int size,

            @RequestParam(defaultValue = "assetName") String sortBy) {

        int internalPage = page > 0 ? page - 1 : 0;

        Page<AssetResponseDTO> assets =
                assetService.getAssetsByType(type, internalPage, size, sortBy);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Assets fetched successfully",
                        PagedResponseDTO.from(assets)
                ));
    }


    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponseDTO<AssetResponseDTO>>> searchAssets(

            @RequestParam String keyword,

            @RequestParam(defaultValue = "1") int page,

            @RequestParam(defaultValue = "5") int size,

            @RequestParam(defaultValue = "assetName") String sortBy) {

        int internalPage = page > 0 ? page - 1 : 0;

        Page<AssetResponseDTO> assets =
                assetService.searchAssets(keyword, internalPage, size, sortBy);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Search results fetched successfully",
                        PagedResponseDTO.from(assets)
                ));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponseDTO>> updateAsset(

            @PathVariable UUID id,

            @Valid @RequestBody AssetRequestDTO requestDTO) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Asset updated successfully",
                        assetService.updateAsset(id, requestDTO)
                ));
    }


    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponseDTO>> patchAsset(

            @PathVariable UUID id,

            @Valid @RequestBody AssetUpdateDTO dto) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Asset updated successfully",
                        assetService.patchAsset(id, dto)
                ));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<AssetResponseDTO>> updateStatus(

            @PathVariable UUID id,

            @Valid @RequestBody AssetStatusUpdateDTO dto) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Asset status updated successfully",
                        assetService.updateAssetStatus(id, dto)
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(
            @PathVariable UUID id) {

        assetService.deleteAsset(id);

        return ResponseEntity.ok(
                ApiResponse.success("Asset deleted successfully", null));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<BulkUploadResultDTO>> bulkCreateAssets(

            @RequestBody List<AssetRequestDTO> requests) {

        BulkUploadResultDTO result =
                assetService.bulkCreateAssets(requests);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        result.getSuccessCount() + " of " +
                                result.getTotalReceived() +
                                " assets created",
                        result));
    }

    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BulkUploadResultDTO>> uploadAssets(

            @RequestPart("file") MultipartFile file) {

        BulkUploadResultDTO result =
                assetService.bulkCreateAssetsFromExcel(file);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "File processed successfully",
                        result));
    }


    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDTO>> getDashboardSummary() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Summary fetched successfully",
                        assetService.getDashboardSummary()));
    }
    @GetMapping("/deleted")
    public ResponseEntity<ApiResponse<List<AssetResponseDTO>>> getDeletedAssets() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Deleted assets fetched successfully",
                        assetService.getDeletedAssets()));

    }
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse<AssetResponseDTO>> restoreAsset(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Asset restored successfully",
                        assetService.restoreAsset(id)));

    }
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PagedResponseDTO<AssetResponseDTO>>> filterAssets(

            @RequestParam(required = false) String assetName,

            @RequestParam(required = false) String assetType,

            @RequestParam(required = false) AssetStatus status,

            @RequestParam(required = false) String serialNumber,

            @RequestParam(defaultValue = "1") int page,

            @RequestParam(defaultValue = "5") int size,

            @RequestParam(defaultValue = "assetName") String sortBy,

            @RequestParam(defaultValue = "asc") String sortDirection
    ) {

        int internalPage = page > 0 ? page - 1 : 0;

        Page<AssetResponseDTO> assets =
                assetService.filterAssets(
                        assetName,
                        assetType,
                        status,
                        serialNumber,
                        internalPage,
                        size,
                        sortBy,
                        sortDirection);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Assets filtered successfully",
                        PagedResponseDTO.from(assets)));
    }
}