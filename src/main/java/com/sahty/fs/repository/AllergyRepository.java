package com.sahty.fs.repository;

import com.sahty.fs.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, String> {
    List<Allergy> findByPatientIdAndIsActiveTrue(String patientId);
}
