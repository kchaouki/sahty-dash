package com.sahty.fs.repository;

import com.sahty.fs.entity.TenantGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantGroupRepository extends JpaRepository<TenantGroup, String> {
}
