package com.campus360.entity;

import com.campus360.enums.FacilityType;
import jakarta.persistence.*;

@Entity
@Table(name = "campus_locations")
public class CampusLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long locationId;

    @Column(nullable = false, length = 100)
    private String buildingName;

    @Column(nullable = false, length = 50)
    private String floorNumber;

    @Column(nullable = false, length = 50)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FacilityType facilityType;

    @Column(length = 255)
    private String description;

    public CampusLocation() {}

    public CampusLocation(Long locationId, String buildingName, String floorNumber, String roomNumber, FacilityType facilityType, String description) {
        this.locationId = locationId;
        this.buildingName = buildingName;
        this.floorNumber = floorNumber;
        this.roomNumber = roomNumber;
        this.facilityType = facilityType;
        this.description = description;
    }

    public static CampusLocationBuilder builder() {
        return new CampusLocationBuilder();
    }

    public static class CampusLocationBuilder {
        private Long locationId;
        private String buildingName;
        private String floorNumber;
        private String roomNumber;
        private FacilityType facilityType;
        private String description;

        public CampusLocationBuilder locationId(Long locationId) { this.locationId = locationId; return this; }
        public CampusLocationBuilder buildingName(String buildingName) { this.buildingName = buildingName; return this; }
        public CampusLocationBuilder floorNumber(String floorNumber) { this.floorNumber = floorNumber; return this; }
        public CampusLocationBuilder roomNumber(String roomNumber) { this.roomNumber = roomNumber; return this; }
        public CampusLocationBuilder facilityType(FacilityType facilityType) { this.facilityType = facilityType; return this; }
        public CampusLocationBuilder description(String description) { this.description = description; return this; }

        public CampusLocation build() {
            return new CampusLocation(locationId, buildingName, floorNumber, roomNumber, facilityType, description);
        }
    }

    public String getDisplayName() {
        return buildingName + " - " + floorNumber + " - " + roomNumber + " (" + facilityType + ")";
    }

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }
    public String getFloorNumber() { return floorNumber; }
    public void setFloorNumber(String floorNumber) { this.floorNumber = floorNumber; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public FacilityType getFacilityType() { return facilityType; }
    public void setFacilityType(FacilityType facilityType) { this.facilityType = facilityType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
