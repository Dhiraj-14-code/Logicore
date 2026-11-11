package com.logicore.Logicore.dto;

import com.logicore.Logicore.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTrackingHistoryResponseDTO {
        private Long id ;

        private ShipmentStatus previousStatus;

        private ShipmentStatus newStatus;

        private String remarks;

        private LocalDateTime changedAt;
}
