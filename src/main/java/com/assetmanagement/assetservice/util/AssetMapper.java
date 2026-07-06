package com.assetmanagement.assetservice.util;

import com.assetmanagement.assetservice.dto.AssetRequestDTO;
import com.assetmanagement.assetservice.dto.AssetResponseDTO;
import com.assetmanagement.assetservice.entity.Asset;
import com.assetmanagement.assetservice.entity.AssetStatus;

public final class AssetMapper {

    private AssetMapper() {
    }

    public static Asset toEntity(AssetRequestDTO dto) {
        return Asset.builder()
                .assetName(dto.getAssetName())
                .assetType(dto.getAssetType())
                .serialNumber(dto.getSerialNumber())
                .status(dto.getStatus() != null ? dto.getStatus() : AssetStatus.AVAILABLE)
                .build();
    }

    public static AssetResponseDTO toResponseDTO(Asset asset) {
        return AssetResponseDTO.builder()
                .id(asset.getId())
                .assetName(asset.getAssetName())
                .assetType(asset.getAssetType())
                .serialNumber(asset.getSerialNumber())
                .status(asset.getStatus())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .lastModifiedBy(asset.getLastModifiedBy())
                .build();
    }
}
