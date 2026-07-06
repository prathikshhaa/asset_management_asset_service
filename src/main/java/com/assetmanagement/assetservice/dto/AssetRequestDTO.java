package com.assetmanagement.assetservice.dto;

import com.assetmanagement.assetservice.entity.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssetRequestDTO {

    @NotBlank
    private String assetName;

    @NotBlank
    private String assetType;

    @NotBlank
    private String serialNumber;

    private AssetStatus status;
}
