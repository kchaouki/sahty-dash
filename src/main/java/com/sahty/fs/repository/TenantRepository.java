package com.sahty.fs.repository;

import com.sahty.fs.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {

    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.group ORDER BY t.designation")
    List<Tenant> findAllWithGroup();

    List<Tenant> findByGroupId(String groupId);
}
