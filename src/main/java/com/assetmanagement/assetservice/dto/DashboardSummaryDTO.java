package com.assetmanagement.assetservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {


    private long totalAssets;
    private long availableAssets;
    private long assignedAssets;
    private long maintenanceAssets;
    private long deletedAssets;


    private Map<String, Long> countByStatus;
    private Map<String, Long> countByType;


    private long totalAssetTypes;


    private List<AssetResponseDTO> latestAssets;
}