package com.assetmanagement.assetservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    private long totalAssets;
    private Map<String, Long> countByStatus;
    private Map<String, Long> countByType;
}
