package com.sahty.fs.repository.lims;

import com.sahty.fs.entity.lims.LabSpecimen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabSpecimenRepository extends JpaRepository<LabSpecimen, String> {
    List<LabSpecimen> findByIsActiveTrue();
}
