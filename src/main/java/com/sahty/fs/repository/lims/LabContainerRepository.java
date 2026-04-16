package com.sahty.fs.repository.lims;

import com.sahty.fs.entity.lims.LabContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabContainerRepository extends JpaRepository<LabContainer, String> {
    List<LabContainer> findByIsActiveTrue();
}
