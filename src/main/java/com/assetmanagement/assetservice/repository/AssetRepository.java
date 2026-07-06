package com.assetmanagement.assetservice.repository;

import com.assetmanagement.assetservice.entity.Asset;
import com.assetmanagement.assetservice.entity.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {

    List<Asset> findByStatus(AssetStatus status);

    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    Page<Asset> findByStatusIn(List<AssetStatus> statuses, Pageable pageable);

    List<Asset> findByAssetType(String assetType);

    Page<Asset> findByAssetType(String assetType, Pageable pageable);

    Page<Asset> findByAssetTypeContainingIgnoreCase(String assetType, Pageable pageable);

    Page<Asset> findByAssetNameContainingIgnoreCase(
            String keyword,
            Pageable pageable
    );

    boolean existsBySerialNumber(String serialNumber);

    boolean existsBySerialNumberAndIdNot(String serialNumber, UUID id);

    long countByStatus(AssetStatus status);

    long countByAssetType(String assetType);

    @org.springframework.data.jpa.repository.Query(
            "SELECT DISTINCT a.assetType FROM Asset a"
    )
    List<String> findDistinctAssetTypes();
}