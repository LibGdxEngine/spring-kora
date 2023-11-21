package com.example.security.reservation.controller;

import com.example.security.reservation.dto.*;
import com.example.security.reservation.service.ReservationService;
import com.example.security.reservation.entity.Club;
import com.example.security.reservation.entity.Reservation;
import com.example.security.reservation.entity.Stadium;
import com.example.security.reservation.repository.ClubRepository;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import com.example.security.utils.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    @GetMapping("/clubs")
    public ResponseEntity<List<ClubDto>> getAllClubs() {
        var clubs = reservationService.getAllClubs();
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/near-clubs")
    public ResponseEntity<List<ClubDto>> getNearClubs(@RequestParam("lat") double latitude,
                                                   @RequestParam("long") double longitude,
                                                   @RequestParam("dist") double distance) {
        var clubs = reservationService.getClubsNearLocation(latitude, longitude, distance);
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/clubs/{clubId}")
    public ResponseEntity<ClubDto> getClubWithId(@PathVariable Integer clubId) {
        var club = reservationService.getClub(clubId);
        return ResponseEntity.ok(club);
    }

    @DeleteMapping("/clubs/{clubId}")
    public ResponseEntity<String> deleteClubWithId(@PathVariable Integer clubId) {
        reservationService.deleteClub(clubId);
        return ResponseEntity.ok("Club " + clubId + " deleted successfully");
    }

    @PatchMapping("/clubs/{clubId}")
    public ResponseEntity<Club> updateClubWithId(@PathVariable Integer clubId, @RequestBody ClubUpdateDto cud) {
        var updatedClub = reservationService.updateClub(clubId, cud);
        return ResponseEntity.ok(updatedClub);
    }

    @PostMapping("/clubs")
    public ResponseEntity<Club> createNewClub(@AuthenticationPrincipal UserDetails authentication,
                                              @RequestBody Club club) {
        User user = userRepository.findByEmail(authentication.getUsername())
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with id: " + authentication.getUsername())
                );
        club.setUser(user);
        var createdClub = reservationService.createClub(club);
        return ResponseEntity.ok(createdClub);
    }

    @PostMapping("/stadiums")
    public ResponseEntity<Stadium> createNewStadiumForClub(@AuthenticationPrincipal UserDetails authentication,
                                                           @RequestBody StadiumCreationRequest stdRequest) {
        User user = userRepository.findByEmail(authentication.getUsername())
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with id: " + authentication.getUsername())
                );
        Stadium createdStadium = reservationService.createStadiumForClub(stdRequest.stadium(), stdRequest.clubId());
        return ResponseEntity.ok(createdStadium);
    }

    @PatchMapping("/stadiums/{stadiumId}")
    public ResponseEntity<Stadium> updateStadiumWithId(@PathVariable Integer stadiumId,
                                                           @RequestBody StadiumUpdateDto sud) {
        Stadium updatedStadium = reservationService.updateStadium(stadiumId, sud);
        return ResponseEntity.ok(updatedStadium);
    }

    @PostMapping("/create")
    public ResponseEntity<Reservation> createNewReservation(@AuthenticationPrincipal UserDetails authentication,
                                                            @RequestBody ReservationRequest reservationRequest) {
        User user = userRepository.findByEmail(authentication.getUsername())
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with id: " + authentication.getUsername())
                );
        Reservation createdReservation = reservationService.createReservation(
                user, reservationRequest.stadiumId(), reservationRequest.reservationTime(),
                reservationRequest.playerName());
        return ResponseEntity.ok(createdReservation);
    }

    @GetMapping("/")
    public ResponseEntity<List<Reservation>> showReservationsForNextXDays(@RequestParam("stadiumId") int stadiumId,
                                                                          @RequestParam(value = "days", defaultValue = "14") int days) {

        var returnedReservations = reservationService.getReservationsForNextXDays(stadiumId, days);
        return ResponseEntity.ok(returnedReservations);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<String> cancelReservation(@PathVariable Integer reservationId) {
        var isCanceled = reservationService.cancelReservation(reservationId);
        if (isCanceled) {
            return ResponseEntity.ok("Reservations cancelled successfully");
        } else {
            throw new RuntimeException("Error cancelling the reservation");
        }
    }

    @PostMapping("/create-pinned")
    public ResponseEntity<Reservation> createNewPinnedReservation(@AuthenticationPrincipal UserDetails authentication,
                                                            @RequestBody ReservationRequest reservationRequest) {
        User user = userRepository.findByEmail(authentication.getUsername())
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with id: " + authentication.getUsername())
                );
        Reservation createdReservation = reservationService.createPinnedReservation(
                user, reservationRequest.stadiumId(), reservationRequest.reservationTime(),
                "Mazen");
        return ResponseEntity.ok(createdReservation);
    }


}
