package com.assetmanagement.assetservice.util;

import com.assetmanagement.assetservice.dto.AssetRequestDTO;
import com.assetmanagement.assetservice.entity.AssetStatus;
import com.assetmanagement.assetservice.exception.FileProcessingException;
import org.apache.poi.ss.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public final class ExcelAssetParser {

    private ExcelAssetParser() {
    }

    public record ExcelRow(int rowNumber, AssetRequestDTO dto, String parseError) {
    }

    public static List<ExcelRow> parse(InputStream inputStream) {

        List<ExcelRow> rows = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) {
                throw new FileProcessingException(
                        "Excel file is empty or missing data rows. Expected a header row followed by data.");
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> columnIndex = mapHeaders(headerRow);

            requireColumn(columnIndex, "assetname");
            requireColumn(columnIndex, "assettype");
            requireColumn(columnIndex, "serialnumber");

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowBlank(row)) {
                    continue;
                }

                int excelRowNumber = r + 1; // 1-based, matches what user sees in Excel

                try {
                    AssetRequestDTO dto = new AssetRequestDTO();
                    dto.setAssetName(getCellString(row, columnIndex.get("assetname")));
                    dto.setAssetType(getCellString(row, columnIndex.get("assettype")));
                    dto.setSerialNumber(getCellString(row, columnIndex.get("serialnumber")));

                    if (columnIndex.containsKey("status")) {
                        String statusStr = getCellString(row, columnIndex.get("status"));
                        if (statusStr != null && !statusStr.isBlank()) {
                            dto.setStatus(parseStatus(statusStr));
                        }
                    }

                    rows.add(new ExcelRow(excelRowNumber, dto, null));

                } catch (IllegalArgumentException ex) {
                    rows.add(new ExcelRow(excelRowNumber, null, ex.getMessage()));
                }
            }

        } catch (IOException ex) {
            throw new FileProcessingException("Unable to read Excel file: " + ex.getMessage(), ex);
        }

        return rows;
    }

    private static Map<String, Integer> mapHeaders(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) {
            throw new FileProcessingException("Excel file is missing a header row.");
        }
        for (Cell cell : headerRow) {
            String header = cell.getStringCellValue();
            if (header != null && !header.isBlank()) {
                map.put(header.trim().toLowerCase(Locale.ROOT).replace(" ", ""), cell.getColumnIndex());
            }
        }
        return map;
    }

    private static void requireColumn(Map<String, Integer> columnIndex, String column) {
        if (!columnIndex.containsKey(column)) {
            throw new FileProcessingException(
                    "Excel file is missing required column: " + column
                            + ". Expected columns: assetName, assetType, serialNumber, status (optional)");
        }
    }

    private static boolean isRowBlank(Row row) {
        for (Cell cell : row) {
            if (cell.getCellType() != CellType.BLANK
                    && !getCellString(row, cell.getColumnIndex()).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static String getCellString(Row row, Integer columnIndex) {
        if (columnIndex == null) {
            return "";
        }
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getStringCellValue().trim();
            default -> "";
        };
    }

    private static AssetStatus parseStatus(String value) {
        try {
            return AssetStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid status value '" + value + "'. Allowed values: AVAILABLE, ASSIGNED, MAINTENANCE");
        }
    }
}
