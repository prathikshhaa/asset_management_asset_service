package com.assetmanagement.assetservice.dto;

import com.assetmanagement.assetservice.entity.AssetStatus;
import lombok.Data;

@Data
public class AssetUpdateDTO {

    private String assetName;
    private String assetType;
    private String serialNumber;
    private AssetStatus status;
}
