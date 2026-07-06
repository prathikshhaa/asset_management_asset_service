package com.assetmanagement.assetservice.dto;

import com.assetmanagement.assetservice.entity.Asset;
import com.assetmanagement.assetservice.entity.AssetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetResponseDTO {

    private UUID id;
    private String assetName;
    private String assetType;
    private String serialNumber;
    private AssetStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String lastModifiedBy;

    public static AssetResponseDTO fromEntity(Asset asset) {
        return AssetResponseDTO.builder()
                .id(asset.getId())
                .assetName(asset.getAssetName())
                .assetType(asset.getAssetType())
                .serialNumber(asset.getSerialNumber())
                .status(asset.getStatus())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }
}
