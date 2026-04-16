package com.sahty.fs.repository.pharmacy;

import com.sahty.fs.entity.pharmacy.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findByTenantIdIsNullAndIsActiveTrue();
    List<Product> findByTenantIdAndIsActiveTrue(String tenantId);
    @Query("""
        SELECT p FROM Product p WHERE p.isActive = true
        AND (p.tenantId IS NULL OR p.tenantId = :tenantId)
        AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(p.dciName) LIKE LOWER(CONCAT('%', :q, '%')))
        """)
    Page<Product> searchProducts(@Param("tenantId") String tenantId, @Param("q") String q, Pageable pageable);
    List<Product> findByProductTypeAndIsActiveTrue(Product.ProductType productType);
}
