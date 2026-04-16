package com.sahty.fs.repository;

import com.sahty.fs.entity.Observation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservationRepository extends JpaRepository<Observation, String> {
    List<Observation> findByAdmissionIdOrderByCreatedAtDesc(String admissionId);
}
