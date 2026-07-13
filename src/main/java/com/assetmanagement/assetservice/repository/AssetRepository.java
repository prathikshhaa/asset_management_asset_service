package com.assetmanagement.assetservice.repository;

import com.assetmanagement.assetservice.entity.Asset;
import com.assetmanagement.assetservice.entity.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends
        JpaRepository<Asset, UUID>,
        JpaSpecificationExecutor<Asset> {

    List<Asset> findByStatus(AssetStatus status);

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    Page<Asset> findByStatusIn(List<AssetStatus> statuses, Pageable pageable);

    List<Asset> findByAssetType(String assetType);

    Page<Asset> findByAssetType(String assetType, Pageable pageable);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsBySerialNumberAndIdNot(String serialNumber, UUID id);

    long countByStatus(AssetStatus status);

    long countByAssetType(String assetType);

    @Query("SELECT DISTINCT a.assetType FROM Asset a")
    List<String> findDistinctAssetTypes();

    List<Asset> findByDeletedTrue();

    Optional<Asset> findByIdAndDeletedTrue(UUID id);

    long countByStatusAndDeletedFalse(AssetStatus status);

    List<Asset> findTop5ByDeletedFalseOrderByCreatedAtDesc();

    Page<Asset> findByAssetNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrModelContainingIgnoreCaseOrManufacturerContainingIgnoreCaseOrSerialNumberContainingIgnoreCase(
            String assetName,
            String brand,
            String model,
            String manufacturer,
            String serialNumber,
            Pageable pageable
    );

    long countByDeletedFalse();

    long countByDeletedTrue();
}