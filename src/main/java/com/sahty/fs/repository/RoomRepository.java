package com.sahty.fs.repository;

import com.sahty.fs.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByServiceId(String serviceId);
    List<Room> findByServiceIdAndIsActiveTrue(String serviceId);
}
