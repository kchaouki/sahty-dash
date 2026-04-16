package com.sahty.fs.repository.pharmacy;

import com.sahty.fs.entity.pharmacy.StockLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockLocationRepository extends JpaRepository<StockLocation, String> {
    List<StockLocation> findByTenantIdAndIsActiveTrue(String tenantId);
    List<StockLocation> findByTenantIdAndScopeAndIsActiveTrue(String tenantId, StockLocation.LocationScope scope);
    List<StockLocation> findByServiceIdAndIsActiveTrue(String serviceId);
}
