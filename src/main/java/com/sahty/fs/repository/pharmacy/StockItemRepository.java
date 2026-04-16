package com.sahty.fs.repository.pharmacy;

import com.sahty.fs.entity.pharmacy.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, String> {

    List<StockItem> findByTenantIdAndLocationIdAndStatus(String tenantId, String locationId, StockItem.ItemStatus status);

    @Query("""
        SELECT si FROM StockItem si
        WHERE si.tenantId = :tenantId
        AND si.product.id = :productId
        AND si.status = 'AVAILABLE'
        ORDER BY si.expirationDate ASC NULLS LAST
        """)
    List<StockItem> findAvailableByProductFEFO(@Param("tenantId") String tenantId,
                                                @Param("productId") String productId);

    @Query("""
        SELECT si.product.id, SUM(si.quantityUnits) as total
        FROM StockItem si
        WHERE si.tenantId = :tenantId
        AND si.location.id = :locationId
        AND si.status = 'AVAILABLE'
        GROUP BY si.product.id
        """)
    List<Object[]> getStockSummaryByLocation(@Param("tenantId") String tenantId,
                                              @Param("locationId") String locationId);

    @Query("""
        SELECT SUM(si.quantityUnits)
        FROM StockItem si
        WHERE si.tenantId = :tenantId
        AND si.product.id = :productId
        AND si.status = 'AVAILABLE'
        """)
    Integer getTotalAvailableQty(@Param("tenantId") String tenantId,
                                  @Param("productId") String productId);
}
