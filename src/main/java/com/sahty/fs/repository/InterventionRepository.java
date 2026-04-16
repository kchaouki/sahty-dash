package com.sahty.fs.repository;

import com.sahty.fs.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterventionRepository extends JpaRepository<Intervention, String> {
    List<Intervention> findByAdmissionIdOrderByCreatedAtDesc(String admissionId);
}
