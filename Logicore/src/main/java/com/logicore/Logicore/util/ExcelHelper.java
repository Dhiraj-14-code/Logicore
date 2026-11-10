
package com.logicore.Logicore.util;

import com.logicore.Logicore.dto.ShipmentRequestDTO;
import com.logicore.Logicore.enums.ShipmentPriority;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ExcelHelper {

    // Uploaded file ka basic validation
    public boolean isValidExcelFile(MultipartFile file) {

        return file != null
                && !file.isEmpty()
                && file.getOriginalFilename() != null
                && file.getOriginalFilename()
                .toLowerCase(Locale.ROOT)
                .endsWith(".xlsx");
    }

    // Excel file ko Workbook object mein convert karna
    public Workbook readExcelFile(MultipartFile file) throws IOException {

        return new XSSFWorkbook(file.getInputStream());
    }

    // Excel ki first sheet return karna
    public Sheet getFirstSheet(Workbook workbook) {

        if (workbook.getNumberOfSheets() == 0) {
            throw new IllegalArgumentException(
                    "Excel file does not contain any sheet"
            );
        }

        return workbook.getSheetAt(0);
    }

    // Excel headers validate karna
    public void validateHeaders(Sheet sheet) {

        Row headerRow = sheet.getRow(0);

        if (headerRow == null) {
            throw new IllegalArgumentException(
                    "Excel header row is missing"
            );
        }

        String[] expectedHeaders = {
                "trackingNumber",
                "origin",
                "destination",
                "priority"
        };

        for (int i = 0; i < expectedHeaders.length; i++) {

            String actualHeader = getCellValue(headerRow, i);

            if (!expectedHeaders[i].equalsIgnoreCase(actualHeader)) {
                throw new IllegalArgumentException(
                        "Invalid Excel header at column " + (i + 1)
                                + ". Expected: " + expectedHeaders[i]
                );
            }
        }
    }

    // Excel cell ki value ko String mein convert karna
    public String getCellValue(Row row, int cellIndex) {

        DataFormatter formatter = new DataFormatter();

        // Missing cell ko blank cell treat karna
        Cell cell = row.getCell(
                cellIndex,
                Row.MissingCellPolicy.CREATE_NULL_AS_BLANK
        );

        return formatter.formatCellValue(cell).trim();
    }

    // Check karna ki complete row empty hai ya nahi
    private boolean isRowEmpty(Row row) {

        if (row == null) {
            return true;
        }

        for (int i = 0; i < 4; i++) {

            if (!getCellValue(row, i).isBlank()) {
                return false;
            }
        }

        return true;
    }

    // Excel ki ek row ko ShipmentRequestDTO mein convert karna
    public ShipmentRequestDTO convertRowToShipmentDTO(Row row) {

        String trackingNumber = getCellValue(row, 0);
        String origin = getCellValue(row, 1);
        String destination = getCellValue(row, 2);
        String priority = getCellValue(row, 3);

        // Priority ko Java Enum mein convert karna
        ShipmentPriority shipmentPriority;
        // Empty priority ko separately reject karo
        if (priority.isBlank()) {
            throw new IllegalArgumentException(
                    "Priority missing at Excel row " + (row.getRowNum() + 1)
            );
        }

        try {
            shipmentPriority = ShipmentPriority.valueOf(
                    priority.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid priority '" + priority
                            + "' at Excel row " + (row.getRowNum() + 1)
            );
        }
        try {

            if (priority.isBlank()) {
                throw new IllegalArgumentException(
                        "Priority missing at Excel row "
                                + (row.getRowNum() + 1)
                );
            }

            shipmentPriority = ShipmentPriority.valueOf(
                    priority.toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Invalid priority '" + priority
                            + "' at Excel row "
                            + (row.getRowNum() + 1)
            );
        }

        // DTO object create karna
        ShipmentRequestDTO dto = new ShipmentRequestDTO();

        dto.setTrackingNumber(trackingNumber);
        dto.setOrigin(origin);
        dto.setDestination(destination);
        dto.setPriority(shipmentPriority);

        return dto;
    }

    // DTO ke required fields validate karna
    public void validateShipmentDTO(
            ShipmentRequestDTO dto,
            int rowNumber) {

        if (dto.getTrackingNumber() == null
                || dto.getTrackingNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Tracking number missing at Excel row " + rowNumber
            );
        }

        if (dto.getOrigin() == null || dto.getOrigin().isBlank()) {

            throw new IllegalArgumentException(
                    "Origin missing at Excel row " + rowNumber
            );
        }

        if (dto.getDestination() == null
                || dto.getDestination().isBlank()) {

            throw new IllegalArgumentException(
                    "Destination missing at Excel row " + rowNumber
            );
        }

        if (dto.getPriority() == null) {

            throw new IllegalArgumentException(
                    "Priority missing at Excel row " + rowNumber
            );
        }
    }

    // Complete Excel sheet ko DTO list mein convert karna
    public List<ShipmentRequestDTO> convertExcelToDTOList(Sheet sheet) {

        // Headers validate karo
        validateHeaders(sheet);

        List<ShipmentRequestDTO> shipmentList = new ArrayList<>();

        // Row 0 headers hain, isliye Row 1 se start karo
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {

            Row row = sheet.getRow(i);

            // Completely empty rows skip karo
            if (isRowEmpty(row)) {
                continue;
            }

            // Excel row ko DTO mein convert karo
            ShipmentRequestDTO dto = convertRowToShipmentDTO(row);

            // Required fields validate karo
            validateShipmentDTO(dto, i + 1);

            shipmentList.add(dto);
        }

        // Empty Excel data reject karo
        if (shipmentList.isEmpty()) {
            throw new IllegalArgumentException(
                    "Excel file contains no shipment records"
            );
        }

        return shipmentList;
    }
}
