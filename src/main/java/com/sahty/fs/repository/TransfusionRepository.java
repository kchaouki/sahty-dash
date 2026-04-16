package com.sahty.fs.repository;

import com.sahty.fs.entity.Transfusion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransfusionRepository extends JpaRepository<Transfusion, String> {
    List<Transfusion> findByAdmissionIdOrderByCreatedAtDesc(String admissionId);
}
