package com.example.security.reservation.repository;

import com.example.security.reservation.entity.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClubRepository extends JpaRepository<Club, Integer> {
    @Query(value = "SELECT * FROM clubs WHERE ST_Distance_Sphere(point(longitude, latitude), point(:lon, :lat)) <= :distance", nativeQuery = true)
    List<Club> findClubsWithinDistanceFromLocation(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("distance") double distance);
}
