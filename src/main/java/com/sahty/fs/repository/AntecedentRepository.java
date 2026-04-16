package com.sahty.fs.repository;

import com.sahty.fs.entity.Antecedent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AntecedentRepository extends JpaRepository<Antecedent, String> {
    List<Antecedent> findByPatientIdOrderByCreatedAtDesc(String patientId);
}
