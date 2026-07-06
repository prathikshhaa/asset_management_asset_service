package com.assetmanagement.assetservice.dto;

import com.assetmanagement.assetservice.entity.AssetStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssetStatusUpdateDTO {

    @NotNull
    private AssetStatus status;


    private String reason;
}
