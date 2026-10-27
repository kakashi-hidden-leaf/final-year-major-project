package com.campus360.repository;

import com.campus360.entity.CampusLocation;
import com.campus360.enums.FacilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampusLocationRepository extends JpaRepository<CampusLocation, Long> {
    List<CampusLocation> findByFacilityType(FacilityType facilityType);

    @Query("SELECT c FROM CampusLocation c WHERE " +
           "(:keyword IS NULL OR LOWER(c.buildingName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.floorNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:type IS NULL OR c.facilityType = :type)")
    List<CampusLocation> searchLocations(@Param("keyword") String keyword, @Param("type") FacilityType type);
}
