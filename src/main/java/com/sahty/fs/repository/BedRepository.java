package com.sahty.fs.repository;

import com.sahty.fs.entity.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BedRepository extends JpaRepository<Bed, String> {
    List<Bed> findByRoomId(String roomId);
    List<Bed> findByRoomIdAndStatus(String roomId, Bed.BedStatus status);
}
