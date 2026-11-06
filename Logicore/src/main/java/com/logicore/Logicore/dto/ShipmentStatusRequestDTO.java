package com.logicore.Logicore.dto;

import com.logicore.Logicore.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentStatusRequestDTO {
    @NotNull(message = "Shipment status required!")
    private ShipmentStatus status;
}
