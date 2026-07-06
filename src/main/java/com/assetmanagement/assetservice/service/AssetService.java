package com.assetmanagement.assetservice.service;
import java.io.InputStream;
import com.assetmanagement.assetservice.dto.*;
import com.assetmanagement.assetservice.entity.Asset;
import com.assetmanagement.assetservice.entity.AssetStatus;
import com.assetmanagement.assetservice.exception.DuplicateAssetException;
import com.assetmanagement.assetservice.exception.FileProcessingException;
import com.assetmanagement.assetservice.exception.ResourceNotFoundException;
import com.assetmanagement.assetservice.repository.AssetRepository;
import com.assetmanagement.assetservice.util.AssetMapper;
import com.assetmanagement.assetservice.util.ExcelAssetParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    @Transactional
    public AssetResponseDTO createAsset(AssetRequestDTO requestDTO) {
        if (assetRepository.existsBySerialNumber(requestDTO.getSerialNumber())) {
            throw new DuplicateAssetException(
                    "Asset with Serial Number " + requestDTO.getSerialNumber() + " already exists");
        }
        Asset asset = AssetMapper.toEntity(requestDTO);
        Asset saved = assetRepository.save(asset);
        return AssetMapper.toResponseDTO(saved);
    }

    public Page<AssetResponseDTO> getAllAssets(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return assetRepository.findAll(pageable)
                .map(AssetMapper::toResponseDTO);
    }

    public AssetResponseDTO getAssetById(UUID id) {
        Asset asset = findAssetOrThrow(id);
        return AssetMapper.toResponseDTO(asset);
    }

    public Page<AssetResponseDTO> getAssetsByStatus(String statusText, int page, int size, String sortBy) {
        AssetStatus status = parseStatus(statusText);
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return assetRepository.findByStatus(status, pageable)
                .map(AssetMapper::toResponseDTO);
    }

    private AssetStatus parseStatus(String statusText) {
        if (!StringUtils.hasText(statusText)) {
            throw new IllegalArgumentException("Status must not be empty.");
        }
        try {
            return AssetStatus.valueOf(statusText.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid status '" + statusText + "'. Allowed values: AVAILABLE, ASSIGNED, MAINTENANCE");
        }
    }

    public Page<AssetResponseDTO> getAssetsByType(String type, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return assetRepository.findByAssetType(type, pageable)
                .map(AssetMapper::toResponseDTO);
    }

    public Page<AssetResponseDTO> searchAssets(String keyword, int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        return assetRepository
                .findByAssetNameContainingIgnoreCase(keyword, pageable)
                .map(AssetMapper::toResponseDTO);
    }

    @Transactional
    public AssetResponseDTO updateAsset(UUID id, AssetRequestDTO requestDTO) {
        Asset existing = findAssetOrThrow(id);

        if (!existing.getSerialNumber().equals(requestDTO.getSerialNumber())
                && assetRepository.existsBySerialNumberAndIdNot(requestDTO.getSerialNumber(), id)) {
            throw new DuplicateAssetException(
                    "Asset with Serial Number " + requestDTO.getSerialNumber() + " already exists");
        }

        existing.setAssetName(requestDTO.getAssetName());
        existing.setAssetType(requestDTO.getAssetType());
        existing.setSerialNumber(requestDTO.getSerialNumber());
        existing.setStatus(requestDTO.getStatus() != null ? requestDTO.getStatus() : existing.getStatus());

        Asset updated = assetRepository.save(existing);
        return AssetMapper.toResponseDTO(updated);
    }

    @Transactional
    public AssetResponseDTO patchAsset(UUID id, AssetUpdateDTO updateDTO) {
        Asset existing = findAssetOrThrow(id);

        if (StringUtils.hasText(updateDTO.getAssetName())) {
            existing.setAssetName(updateDTO.getAssetName());
        }
        if (StringUtils.hasText(updateDTO.getAssetType())) {
            existing.setAssetType(updateDTO.getAssetType());
        }
        if (StringUtils.hasText(updateDTO.getSerialNumber())
                && !updateDTO.getSerialNumber().equals(existing.getSerialNumber())) {
            if (assetRepository.existsBySerialNumberAndIdNot(updateDTO.getSerialNumber(), id)) {
                throw new DuplicateAssetException(
                        "Asset with Serial Number " + updateDTO.getSerialNumber() + " already exists");
            }
            existing.setSerialNumber(updateDTO.getSerialNumber());
        }
        if (updateDTO.getStatus() != null) {
            existing.setStatus(updateDTO.getStatus());
        }

        Asset updated = assetRepository.save(existing);
        return AssetMapper.toResponseDTO(updated);
    }

    @Transactional
    public AssetResponseDTO updateAssetStatus(UUID id, AssetStatusUpdateDTO statusUpdateDTO) {

        Asset existing = findAssetOrThrow(id);

        AssetStatus previousStatus = existing.getStatus();
        AssetStatus newStatus = statusUpdateDTO.getStatus();


        if (previousStatus == newStatus) {
            throw new IllegalArgumentException(
                    "Asset is already in " + newStatus + " status.");
        }

        switch (previousStatus) {

            case AVAILABLE -> {
                if (newStatus != AssetStatus.ASSIGNED) {
                    throw new IllegalArgumentException(
                            "AVAILABLE assets can only be changed to ASSIGNED.");
                }
            }

            case ASSIGNED -> {
                if (newStatus != AssetStatus.AVAILABLE
                        && newStatus != AssetStatus.MAINTENANCE) {
                    throw new IllegalArgumentException(
                            "ASSIGNED assets can only be changed to AVAILABLE or MAINTENANCE.");
                }
            }

            case MAINTENANCE -> {
                if (newStatus != AssetStatus.AVAILABLE) {
                    throw new IllegalArgumentException(
                            "MAINTENANCE assets can only be changed to AVAILABLE.");
                }
            }
        }

        String caller = currentCaller();

        existing.setStatus(newStatus);
        existing.setLastModifiedBy(caller);

        Asset updated = assetRepository.save(existing);

        // NOTE: Status-change history is no longer recorded here.
        // The asset-assignment service is now the single owner of asset history.
        // previousStatus/newStatus/caller/reason are still available above if that
        // service needs to be notified (e.g. via an event or a call) in future.

        return AssetMapper.toResponseDTO(updated);
    }

    private String currentCaller() {

        var authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication != null
                && authentication.getName() != null
                && !authentication.getName().equals("anonymousUser")) {
            return authentication.getName();
        }

        return "unknown";
    }

    @Transactional
    public void deleteAsset(UUID id) {
        Asset existing = findAssetOrThrow(id);
        existing.setDeleted(true);
        existing.setDeletedAt(java.time.LocalDateTime.now());
        existing.setLastModifiedBy(currentCaller());
        assetRepository.save(existing);
    }

    public DashboardSummaryDTO getDashboardSummary() {
        long totalAssets = assetRepository.count();

        Map<String, Long> countByStatus = new HashMap<>();
        for (AssetStatus status : AssetStatus.values()) {
            countByStatus.put(status.name(), assetRepository.countByStatus(status));
        }

        Map<String, Long> countByType = new HashMap<>();
        for (String type : assetRepository.findDistinctAssetTypes()) {
            countByType.put(type, assetRepository.countByAssetType(type));
        }

        return DashboardSummaryDTO.builder()
                .totalAssets(totalAssets)
                .countByStatus(countByStatus)
                .countByType(countByType)
                .build();
    }


    public BulkUploadResultDTO bulkCreateAssets(List<AssetRequestDTO> requests) {

        List<AssetResponseDTO> created = new ArrayList<>();
        List<BulkUploadResultDTO.RowError> errors = new ArrayList<>();
        Set<String> seenInBatch = new HashSet<>();

        for (int i = 0; i < requests.size(); i++) {
            int rowNumber = i + 1;
            AssetRequestDTO dto = requests.get(i);

            String validationError = validateBulkRow(dto, seenInBatch);
            if (validationError != null) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(rowNumber)
                        .serialNumber(dto != null ? dto.getSerialNumber() : null)
                        .reason(validationError)
                        .build());
                continue;
            }

            try {
                seenInBatch.add(dto.getSerialNumber());
                created.add(createAsset(dto));
            } catch (DuplicateAssetException ex) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(rowNumber)
                        .serialNumber(dto.getSerialNumber())
                        .reason(ex.getMessage())
                        .build());
            } catch (Exception ex) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(rowNumber)
                        .serialNumber(dto.getSerialNumber())
                        .reason("Failed to save: " + ex.getMessage())
                        .build());
            }
        }

        return BulkUploadResultDTO.builder()
                .totalReceived(requests.size())
                .successCount(created.size())
                .failureCount(errors.size())
                .createdAssets(created)
                .errors(errors)
                .build();
    }
    @Transactional
    public BulkUploadResultDTO bulkCreateAssetsFromExcel(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new FileProcessingException("Uploaded file is empty.");
        }

        List<AssetResponseDTO> created = new ArrayList<>();
        List<BulkUploadResultDTO.RowError> errors = new ArrayList<>();
        Set<String> seenInBatch = new HashSet<>();

        List<ExcelAssetParser.ExcelRow> rows;

        try (InputStream inputStream = file.getInputStream()) {
            rows = ExcelAssetParser.parse(inputStream);
        } catch (IOException ex) {
            throw new FileProcessingException(
                    "Unable to read uploaded file: " + ex.getMessage(), ex);
        }

        for (ExcelAssetParser.ExcelRow row : rows) {

            if (row.parseError() != null) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(row.rowNumber())
                        .reason(row.parseError())
                        .build());
                continue;
            }

            AssetRequestDTO dto = row.dto();

            String validationError = validateBulkRow(dto, seenInBatch);

            if (validationError != null) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(row.rowNumber())
                        .serialNumber(dto != null ? dto.getSerialNumber() : null)
                        .reason(validationError)
                        .build());
                continue;
            }

            try {
                seenInBatch.add(dto.getSerialNumber());
                created.add(createAsset(dto));

            } catch (DuplicateAssetException ex) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(row.rowNumber())
                        .serialNumber(dto.getSerialNumber())
                        .reason(ex.getMessage())
                        .build());

            } catch (Exception ex) {
                errors.add(BulkUploadResultDTO.RowError.builder()
                        .rowNumber(row.rowNumber())
                        .serialNumber(dto.getSerialNumber())
                        .reason("Failed to save: " + ex.getMessage())
                        .build());
            }
        }

        return BulkUploadResultDTO.builder()
                .totalReceived(rows.size())
                .successCount(created.size())
                .failureCount(errors.size())
                .createdAssets(created)
                .errors(errors)
                .build();
    }

    private String validateBulkRow(AssetRequestDTO dto, Set<String> seenInBatch) {
        if (dto == null) {
            return "Row could not be parsed.";
        }
        if (!StringUtils.hasText(dto.getAssetName())) {
            return "assetName is required.";
        }
        if (!StringUtils.hasText(dto.getAssetType())) {
            return "assetType is required.";
        }
        if (!StringUtils.hasText(dto.getSerialNumber())) {
            return "serialNumber is required.";
        }
        if (seenInBatch.contains(dto.getSerialNumber())) {
            return "Duplicate serial number within this upload: " + dto.getSerialNumber();
        }
        return null;
    }

    private Asset findAssetOrThrow(UUID id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
    }
    public List<AssetSummaryDTO> getAllAssetsSummary() {
        return assetRepository.findAll().stream()
                .map(asset -> AssetSummaryDTO.builder()
                        .id(asset.getId())
                        .assetName(asset.getAssetName())
                        .status(asset.getStatus().name())
                        .assetType(asset.getAssetType())
                        .build())
                .toList();
    }
}