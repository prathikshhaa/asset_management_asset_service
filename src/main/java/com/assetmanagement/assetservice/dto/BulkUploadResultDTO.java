package com.assetmanagement.assetservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResultDTO {

    private int totalReceived;

    private int successCount;

    private int failureCount;

    private List<AssetResponseDTO> createdAssets;

    private List<RowError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {

        private int rowNumber;
        private String serialNumber;
        private String reason;
    }
}
