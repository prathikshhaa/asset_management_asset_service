package com.assetmanagement.assetservice.dto;

import com.assetmanagement.assetservice.entity.AssetStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetSummaryDTO {

    private UUID id;
    private String assetName;
    private String assetType;
    private String serialNumber;
    private AssetStatus status;
}