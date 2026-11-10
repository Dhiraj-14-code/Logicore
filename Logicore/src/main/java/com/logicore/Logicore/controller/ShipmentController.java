
package com.logicore.Logicore.controller;

import com.logicore.Logicore.dto.ShipmentRequestDTO;
import com.logicore.Logicore.dto.ShipmentResponseDTO;
import com.logicore.Logicore.dto.ShipmentStatusRequestDTO;
import com.logicore.Logicore.enums.ShipmentPriority;
import com.logicore.Logicore.enums.ShipmentStatus;
import com.logicore.Logicore.mapper.ShipmentMapper;
import com.logicore.Logicore.service.ShipmentService;
import com.logicore.Logicore.util.ExcelHelper;

import jakarta.validation.Valid;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final ExcelHelper excelHelper;
    private final ShipmentMapper shipmentMapper;

    // Constructor injection
    public ShipmentController(
            ShipmentService shipmentService,
            ExcelHelper excelHelper, ShipmentMapper shipmentMapper) {

        this.shipmentService = shipmentService;
        this.excelHelper = excelHelper;
        this.shipmentMapper = shipmentMapper;
    }

    // Create shipment
    @PostMapping
    public ResponseEntity<ShipmentResponseDTO> createShipment(
            @Valid @RequestBody ShipmentRequestDTO requestDTO) {

        ShipmentResponseDTO responseDTO =
                shipmentService.createShipment(requestDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(responseDTO);
    }


    // Get shipment by ID
    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponseDTO> getShipmentById(
            @PathVariable Long id) {

        ShipmentResponseDTO responseDTO =
                shipmentService.getShipmentById(id);

        return ResponseEntity.ok(responseDTO);
    }

    // Update shipment
    @PutMapping("/{id}")
    public ResponseEntity<ShipmentResponseDTO> updateShipment(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentRequestDTO requestDTO) {

        ShipmentResponseDTO responseDTO =
                shipmentService.updateShipment(id, requestDTO);

        return ResponseEntity.ok(responseDTO);
    }

    // Delete shipment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(
            @PathVariable Long id) {

        shipmentService.deleteShipment(id);

        return ResponseEntity.noContent().build();
    }

    // Update shipment status
    @PatchMapping("/{id}/status")
    public ResponseEntity<ShipmentResponseDTO> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentStatusRequestDTO requestDTO) {

        ShipmentResponseDTO responseDTO =
                shipmentService.updateShipmentStatus(
                        id,
                        requestDTO.getStatus()
                );

        return ResponseEntity.ok(responseDTO);
    }

    // Import multiple shipments using Excel
    @PostMapping("/import/excel")
    public ResponseEntity<List<ShipmentResponseDTO>> importExcel(
            @RequestParam("file") MultipartFile file)
            throws IOException {

        // Uploaded file ka basic validation
        if (!excelHelper.isValidExcelFile(file)) {
            throw new IllegalArgumentException(
                    "Please upload a valid .xlsx Excel file"
            );
        }

        // Workbook automatically close hoga
        try (Workbook workbook = excelHelper.readExcelFile(file)) {

            // Excel ki first sheet access karo
            Sheet sheet = excelHelper.getFirstSheet(workbook);

            // Excel records ko DTO list mein convert karo
            List<ShipmentRequestDTO> shipmentList =
                    excelHelper.convertExcelToDTOList(sheet);

            // Bulk import service call karo
            List<ShipmentResponseDTO> result =
                    shipmentService.importShipments(shipmentList);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(result);
        }
    }
    // Pagination aur sorting ke saath saare shipments fetch karna
    @GetMapping
    public ResponseEntity<Page<ShipmentResponseDTO>> getAllShipment(
            // Default page = 0, size = 10, sorting = id descending
            @PageableDefault(
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.DESC
            )Pageable pageable)  {
        // Service ko pagination request bhejna
        Page<ShipmentResponseDTO> response= shipmentService.getAllShipment( pageable);
                return ResponseEntity.ok(response);
    }
    @GetMapping("/search/priority")
    public ResponseEntity<Page<ShipmentResponseDTO>> findByPriority(
            @RequestParam ShipmentPriority priority,
            Pageable pageable
    ){
        Page<ShipmentResponseDTO> response = shipmentService.findByPriority(priority,pageable);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/search/origin")
    public ResponseEntity<Page<ShipmentResponseDTO>> findByOriginContainingIgnoreCase(
            @RequestParam String origin,
            Pageable pageable
    ){
        Page<ShipmentResponseDTO> response=shipmentService.findByOrigin(origin,pageable);
    return ResponseEntity.ok(response);
    }
    // Multiple filters ke basis par shipments search karna
    @GetMapping("/search")
    public ResponseEntity<Page<ShipmentResponseDTO>> searchShipments(

            // Ye parameters optional hain; user koi bhi filter select kar sakta hai
            @RequestParam(required = false) ShipmentPriority priority,
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) String origin,

            // Pagination aur sorting handle karna
            Pageable pageable
    ) {

        // Service layer ko search filters pass karna
        Page<ShipmentResponseDTO> response =
                shipmentService.searchShipments(
                        priority,
                        status,
                        origin,
                        pageable
                );

        return ResponseEntity.ok(response);
    }
}
