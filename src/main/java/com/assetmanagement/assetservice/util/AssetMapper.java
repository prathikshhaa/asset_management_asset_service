package com.assetmanagement.assetservice.util;

import com.assetmanagement.assetservice.dto.AssetRequestDTO;
import com.assetmanagement.assetservice.dto.AssetResponseDTO;
import com.assetmanagement.assetservice.dto.AssetSummaryDTO;
import com.assetmanagement.assetservice.dto.AssetUpdateDTO;
import com.assetmanagement.assetservice.entity.Asset;

public class AssetMapper {

    private AssetMapper() {
    }

    /**
     * Convert Request DTO -> Entity
     */
    public static Asset toEntity(AssetRequestDTO dto) {

        if (dto == null) {
            return null;
        }

        return Asset.builder()
                .assetName(dto.getAssetName())
                .assetType(dto.getAssetType())
                .brand(dto.getBrand())
                .model(dto.getModel())
                .manufacturer(dto.getManufacturer())
                .description(dto.getDescription())
                .department(dto.getDepartment())
                .location(dto.getLocation())
                .purchaseDate(dto.getPurchaseDate())
                .warrantyExpiry(dto.getWarrantyExpiry())
                .purchasePrice(dto.getPurchasePrice())
                .serialNumber(dto.getSerialNumber())
                .status(dto.getStatus())
                .build();
    }

    /**
     * Convert Entity -> Response DTO
     */
    public static AssetResponseDTO toResponseDTO(Asset asset) {

        if (asset == null) {
            return null;
        }

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

    /**
     * Update Entity using Request DTO (PUT)
     */
    public static void updateEntity(Asset asset, AssetRequestDTO dto) {

        asset.setAssetName(dto.getAssetName());
        asset.setAssetType(dto.getAssetType());
        asset.setBrand(dto.getBrand());
        asset.setModel(dto.getModel());
        asset.setManufacturer(dto.getManufacturer());
        asset.setDescription(dto.getDescription());
        asset.setDepartment(dto.getDepartment());
        asset.setLocation(dto.getLocation());
        asset.setPurchaseDate(dto.getPurchaseDate());
        asset.setWarrantyExpiry(dto.getWarrantyExpiry());
        asset.setPurchasePrice(dto.getPurchasePrice());

        if (dto.getStatus() != null) {
            asset.setStatus(dto.getStatus());
        }

        // Serial Number is intentionally NOT updated.
    }

    /**
     * Update Entity using Update DTO (PATCH)
     */
    public static void patchEntity(Asset asset, AssetUpdateDTO dto) {

        if (dto.getAssetName() != null)
            asset.setAssetName(dto.getAssetName());

        if (dto.getAssetType() != null)
            asset.setAssetType(dto.getAssetType());

        if (dto.getBrand() != null)
            asset.setBrand(dto.getBrand());

        if (dto.getModel() != null)
            asset.setModel(dto.getModel());

        if (dto.getManufacturer() != null)
            asset.setManufacturer(dto.getManufacturer());

        if (dto.getDescription() != null)
            asset.setDescription(dto.getDescription());

        if (dto.getDepartment() != null)
            asset.setDepartment(dto.getDepartment());

        if (dto.getLocation() != null)
            asset.setLocation(dto.getLocation());

        if (dto.getPurchaseDate() != null)
            asset.setPurchaseDate(dto.getPurchaseDate());

        if (dto.getWarrantyExpiry() != null)
            asset.setWarrantyExpiry(dto.getWarrantyExpiry());

        if (dto.getPurchasePrice() != null)
            asset.setPurchasePrice(dto.getPurchasePrice());

        if (dto.getStatus() != null)
            asset.setStatus(dto.getStatus());
    }
    public static AssetSummaryDTO toSummaryDTO(Asset asset) {

        if (asset == null) {
            return null;
        }

        return AssetSummaryDTO.builder()
                .id(asset.getId())
                .assetName(asset.getAssetName())
                .assetType(asset.getAssetType())
                .serialNumber(asset.getSerialNumber())
                .status(asset.getStatus())
                .build();
    }
}