package com.sahty.fs.repository;

import com.sahty.fs.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    @Query("SELECT r FROM Role r WHERE r.tenantId = :tenantId")
    List<Role> findByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT r FROM Role r WHERE r.tenantId IS NULL")
    List<Role> findGlobalRoles();
}
