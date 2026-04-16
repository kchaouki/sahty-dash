package com.sahty.fs.repository.lims;

import com.sahty.fs.entity.lims.LabMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LabMethodRepository extends JpaRepository<LabMethod, String> {
    List<LabMethod> findByIsActiveTrue();
}
