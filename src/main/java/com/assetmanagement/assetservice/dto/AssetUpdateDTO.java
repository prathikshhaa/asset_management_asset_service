package com.assetmanagement.assetservice.dto;

import com.assetmanagement.assetservice.entity.AssetStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AssetUpdateDTO {

    private String assetName;

    private String assetType;

    private String brand;

    private String model;

    private String manufacturer;

    private String description;

    private String department;

    private String location;

    private LocalDate purchaseDate;

    private LocalDate warrantyExpiry;

    private BigDecimal purchasePrice;

    private AssetStatus status;
}