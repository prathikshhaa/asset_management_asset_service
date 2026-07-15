package com.assetmanagement.assetservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetLogRequest {

    private UUID assetId;
    private String action;
    private String performedBy;
    private String details;
    private LocalDateTime timestamp;
}