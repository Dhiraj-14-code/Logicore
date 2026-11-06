package com.logicore.Logicore.controller;

import com.logicore.Logicore.dto.ShipmentRequestDTO;
import com.logicore.Logicore.dto.ShipmentResponseDTO;
import com.logicore.Logicore.dto.ShipmentStatusRequestDTO;
import com.logicore.Logicore.mapper.ShipmentMapper;
import com.logicore.Logicore.service.ShipmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {
    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;

    }
    @PostMapping
    public ResponseEntity<ShipmentResponseDTO> createShipment(
            @Valid
            @RequestBody ShipmentRequestDTO requestDTO
            ){
        ShipmentResponseDTO responseDTO  = shipmentService.createShipment(requestDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDTO);

    }
    @GetMapping
    public ResponseEntity<List<ShipmentResponseDTO>> getAllShipment() {
        List<ShipmentResponseDTO> responseDTOS = shipmentService.getAllShipments();
        return ResponseEntity.ok(responseDTOS);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ShipmentResponseDTO> getShipmentById(
            @PathVariable Long id
    ){
        ShipmentResponseDTO responseDTO = shipmentService.getShipmentById(id);
        return ResponseEntity.ok(responseDTO);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ShipmentResponseDTO> updateShipment(
            @Valid
            @PathVariable Long id ,
            @RequestBody ShipmentRequestDTO requestDTO
    ){
        ShipmentResponseDTO responseDTO = shipmentService.updateShipment(id,requestDTO);
        return ResponseEntity.ok(responseDTO);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShipment(
            @PathVariable Long id
    ){
        shipmentService.deleteShipment(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/status")//Updates selected fields in existing fields
    public  ResponseEntity<ShipmentResponseDTO> updateStatus(
            @Valid
            @PathVariable Long id ,
            @RequestBody ShipmentStatusRequestDTO requestDTO
            ){
        ShipmentResponseDTO responseDTO = shipmentService.updateShipmentStatus(id,requestDTO.getStatus());
        return ResponseEntity.ok(responseDTO);
    }
}
