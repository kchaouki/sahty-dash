package com.sahty.fs.repository.pharmacy;

import com.sahty.fs.entity.pharmacy.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, String> {
    List<Supplier> findByTenantIdIsNullAndIsActiveTrue();
    List<Supplier> findByTenantIdAndIsActiveTrue(String tenantId);
    List<Supplier> findByIsActiveTrueAndTenantIdIsNullOrTenantId(String tenantId);
}
