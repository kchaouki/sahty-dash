package com.sahty.fs.repository;

import com.sahty.fs.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
    Optional<Tenant> findByCode(String code);
    List<Tenant> findByStatus(Tenant.TenantStatus status);
    List<Tenant> findByGroupId(String groupId);
    boolean existsByCode(String code);
}
