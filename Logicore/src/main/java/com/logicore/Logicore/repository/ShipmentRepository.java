package com.logicore.Logicore.repository;

import com.logicore.Logicore.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment,Long> {
    boolean existsByTrackingNumber(String trackingNumber );
    boolean existsByTrackingNumberAndIdNot(String trackingNumber , Long id);
}
