package com.sahty.fs.repository;

import com.sahty.fs.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsernameIgnoreCase(String username);
    List<User> findByTenantId(String tenantId);
    List<User> findByTenantIdAndActiveTrue(String tenantId);
    boolean existsByUsernameIgnoreCase(String username);
    @Query("SELECT u FROM User u WHERE u.tenant.id = :tenantId AND u.active = true ORDER BY u.lastName, u.firstName")
    List<User> findActiveByTenant(String tenantId);
}
