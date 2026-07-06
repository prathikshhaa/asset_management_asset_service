package com.assetmanagement.assetservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AssetSummaryDTO {
    private UUID id;
    private String assetName;
    private String status;
    private String assetType;
}