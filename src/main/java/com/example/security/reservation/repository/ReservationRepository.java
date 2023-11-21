package com.example.security.reservation.repository;

import com.example.security.reservation.entity.Reservation;
import com.example.security.reservation.entity.Stadium;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    List<Reservation> findReservationsByStadiumAndReservationTimeBetween(
            Stadium stadium, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("SELECT r FROM Reservation r WHERE r.stadium.id = :stadiumId AND " +
            "r.reservationTime >= :startDate AND r.reservationTime < :endDate")
    List<Reservation> findReservationsForTheNextXDays(
            @Param("stadiumId") Integer stadiumId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    @Query("SELECT r FROM Reservation r WHERE r.stadium.id = :stadiumId AND " +
            "r.reservationTime = :date")
    List<Reservation> findReservationsByStadiumAndDate(
            @Param("stadiumId") Integer stadiumId,
            @Param("date") LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE r.stadium.id = :stadiumId AND " +
            "r.reservationTime >= :startOfHour AND r.reservationTime < :endOfHour")
    Optional<Reservation> findReservationByStadiumAndHour(
            @Param("stadiumId") Integer stadiumId,
            @Param("startOfHour") LocalDateTime startOfHour,
            @Param("endOfHour") LocalDateTime endOfHour);

    @Transactional
    @Modifying
    @Query("UPDATE Reservation r SET r.status = 'CANCELED' WHERE r.id = :reservationId")
    int cancelReservation(@Param("reservationId") Integer reservationId);


    @Query("SELECT r FROM Reservation r WHERE r.status = 'PINNED' AND " +
            "FUNCTION('DATE', r.reservationTime) = :day")
    List<Reservation> findPinnedReservationsFrom(@Param("day") LocalDate day);


}
