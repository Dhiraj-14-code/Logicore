package com.logicore.Logicore.repository;

import com.logicore.Logicore.entity.ShipmentTrackingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShipmentTrackingHistoryRepository extends JpaRepository<ShipmentTrackingHistory,Long> {
    List<ShipmentTrackingHistory> findByShipmentIdOrderByChangedAtAsc(Long shipmentId);

    Long id(Long id);
}
