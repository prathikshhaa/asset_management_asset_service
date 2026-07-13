package com.assetmanagement.assetservice.dto;

import com.assetmanagement.assetservice.entity.Asset;
import com.assetmanagement.assetservice.entity.AssetStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    private String brand;

    private String model;

    private String manufacturer;

    private String description;

    private String department;

    private String location;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiry;

    private BigDecimal purchasePrice;

    private String serialNumber;

    private AssetStatus status;

    private String imageUrl;

    private String lastModifiedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static AssetResponseDTO fromEntity(Asset asset) {

        return AssetResponseDTO.builder()
                .id(asset.getId())
                .assetName(asset.getAssetName())
                .assetType(asset.getAssetType())
                .brand(asset.getBrand())
                .model(asset.getModel())
                .manufacturer(asset.getManufacturer())
                .description(asset.getDescription())
                .department(asset.getDepartment())
                .location(asset.getLocation())
                .purchaseDate(asset.getPurchaseDate())
                .warrantyExpiry(asset.getWarrantyExpiry())
                .purchasePrice(asset.getPurchasePrice())
                .serialNumber(asset.getSerialNumber())
                .status(asset.getStatus())
                .imageUrl(asset.getImageUrl())
                .lastModifiedBy(asset.getLastModifiedBy())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }
}