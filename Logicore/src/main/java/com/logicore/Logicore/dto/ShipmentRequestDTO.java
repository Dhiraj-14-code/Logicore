package com.logicore.Logicore.dto;

import com.logicore.Logicore.enums.ShipmentPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentRequestDTO {

    @NotBlank(message = "Tracking number is required")
    @Size(max = 30, message = "Tracking number cannot exceed 30 characters")
    private String trackingNumber;

    @NotBlank(message = "Origin is required")
    private String origin;

    @NotBlank(message = "Destination is required")
    private String destination;

    @NotNull(message = "Priority is required")
    private ShipmentPriority priority;
}