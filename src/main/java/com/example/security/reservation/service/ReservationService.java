package com.example.security.reservation.service;

import com.example.security.reservation.dto.*;
import com.example.security.reservation.entity.*;
import com.example.security.reservation.repository.ClubRepository;
import com.example.security.reservation.repository.ReservationRepository;
import com.example.security.reservation.repository.StadiumRepository;
import com.example.security.reservation.repository.UserClubRepository;
import com.example.security.user.User;
import com.example.security.utils.EmailService;
import com.example.security.utils.exceptions.ClubNotFoundException;
import com.example.security.utils.exceptions.ReservationNotFoundException;
import com.example.security.utils.exceptions.StadiumNotFoundException;
import com.example.security.utils.exceptions.TimeSlotUnavailableException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ClubRepository clubRepository;
    private final StadiumRepository stadiumRepository;
    private final ReservationRepository reservationRepository;
    private final EmailService emailService;
    private final UserClubRepository userClubRepository;

    public ClubDto getClub(int clubId) {
        Club club = clubRepository.findById(clubId).orElseThrow(() ->
                new ClubNotFoundException("Club with id: " + clubId + " is not found")
        );
        return convertClubToDto(club);
    }

    public Stadium createStadiumForClub(Stadium stadium, Integer clubId) {
        return clubRepository.findById(clubId).map(club -> {
            stadium.setClub(club);
            clubRepository.save(club);
            return stadiumRepository.save(stadium);
        }).orElseThrow(() -> new ClubNotFoundException("Club not found with id " + clubId));
    }

    public List<ClubDto> getAllClubs() {
        var clubs = clubRepository.findAll();
        return clubs.stream().map(this::convertClubToDto).collect(Collectors.toList());
    }

    public List<ClubDto> getClubsNearLocation(double latitude, double longitude, double distance) {
        List<Club> clubs = clubRepository.findClubsWithinDistanceFromLocation(latitude, longitude, distance);
        return clubs.stream().map(this::convertClubToDto).collect(Collectors.toList());
    }

    public List<Reservation> getReservationsOfStadiumInSingleDay(int stadiumId, LocalDate date) {
        return reservationRepository.findReservationsByStadiumAndDate(stadiumId, date);
    }

    public Optional<Reservation> getReservationSlot(int stadiumId, LocalDateTime reservationTime) {
        LocalDateTime startOfHour = reservationTime.truncatedTo(ChronoUnit.HOURS);
        LocalDateTime endOfHour = startOfHour.plusHours(1);
        return reservationRepository
                .findReservationByStadiumAndHour(stadiumId, startOfHour, endOfHour);
    }

    public Reservation createReservation(User user, Integer stadiumId,
                                         LocalDateTime reservationTime, String playerName) {
        // First, check if the stadium exists
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new StadiumNotFoundException("Stadium not found with id " + stadiumId));
        // Check if the time slot is available
        Optional<Reservation> reservationSlot = getReservationSlot(stadiumId, reservationTime);
        boolean isSlotAvailable = reservationSlot.isEmpty() ||
                reservationSlot.get().getStatus() == ReservationStatus.CANCELED;
        if (!isSlotAvailable) {
            throw new TimeSlotUnavailableException("The selected time slot is already reserved.");
        }

        // Create and save the new reservation
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setPlayerName(playerName);
        reservation.setStadium(stadium);
        reservation.setReservationTime(reservationTime);
        reservation.setStatus(ReservationStatus.RESERVED);
        reservationRepository.save(reservation);
        return reservation;
    }

    public List<Reservation> getReservationsForNextXDays(int stadiumId, int numberOfDays) {
        var stadium = stadiumRepository.findById(stadiumId).orElseThrow(() ->
                new StadiumNotFoundException("Stadiums with id: " + stadiumId + " not found"));
        var startDate = LocalDateTime.now();
        var endDate = startDate.plusDays(numberOfDays);
        return reservationRepository.findReservationsByStadiumAndReservationTimeBetween(stadium, startDate, endDate);
    }

    public boolean cancelReservation(int reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id " + reservationId));

        notifyFollowers(reservation.getStadium().getClub(), reservation);

        if (reservation.getStatus() == ReservationStatus.PINNED) {
            // Cancel this and all future reservations
            reservationRepository.cancelReservation(reservationId);
            var nextWeekReservationTime = reservation.getReservationTime().plusWeeks(1);
            var nextReservation = reservationRepository.findReservationByStadiumAndHour(reservation.getStadium().getId()
                    , nextWeekReservationTime, nextWeekReservationTime.plusHours(1));
            nextReservation.ifPresent(value -> reservationRepository.cancelReservation(value.getId()));
            return true;
        } else {
            var updatedRows = reservationRepository.cancelReservation(reservationId);
            return updatedRows > 0;
        }
    }

    public Reservation createPinnedReservation(User user, int stadiumId,
                                               LocalDateTime reservationTime, String playerName) {
        // First, check if the stadium exists
        Stadium stadium = stadiumRepository.findById(stadiumId)
                .orElseThrow(() -> new StadiumNotFoundException("Stadium not found with id " + stadiumId));
        // Check if the time slot is available
        boolean isSlotAvailable = !getReservationSlot(stadiumId, reservationTime).isPresent();
        if (!isSlotAvailable) {
            throw new TimeSlotUnavailableException("The selected time slot is already booked.");
        }

        // Create and save the new reservation
        Reservation reservationWeek1 = Reservation.builder()
                .stadium(stadium)
                .user(user)
                .status(ReservationStatus.PINNED)
                .playerName(playerName)
                .reservationTime(reservationTime)
                .build();
        Reservation reservationWeek2 = Reservation.builder()
                .stadium(stadium)
                .user(user)
                .playerName(playerName)
                .status(ReservationStatus.PINNED)
                .reservationTime(reservationTime.plusWeeks(1))
                .build();
        reservationRepository.save(reservationWeek1);
        reservationRepository.save(reservationWeek2);
        return reservationWeek1;
    }

    @Scheduled(cron = "0 0 1 * * ?") // Runs at 1 AM every day
    @Transactional
    public void handleWeeklyPinnedReservations() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        List<Reservation> pinnedReservations = reservationRepository.findPinnedReservationsFrom(yesterday);

        for (Reservation pinnedReservation : pinnedReservations) {
            LocalTime reservationTime = pinnedReservation.getReservationTime().toLocalTime();
            LocalDate nextWeek = yesterday.plusWeeks(1);

            // Check if a reservation for the next week already exists to avoid duplication
            if (!getReservationSlot(
                    pinnedReservation.getStadium().getId(),
                    LocalDateTime.of(nextWeek, reservationTime)).isPresent()) {

                Reservation newReservation = new Reservation();
                // Copy the details from the pinned reservation
                newReservation.setStadium(pinnedReservation.getStadium());
                newReservation.setUser(pinnedReservation.getUser());
                newReservation.setPlayerName(pinnedReservation.getPlayerName());
                newReservation.setReservationTime(LocalDateTime.of(nextWeek, reservationTime));
                newReservation.setStatus(ReservationStatus.PINNED); // Keep the reservation pinned

                reservationRepository.save(newReservation);
            }
        }
    }

    public Club createClub(Club club) {
        return clubRepository.save(club);
    }

    public void deleteClub(int clubId) {
        var returnedClub = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found for deletion"));
        for (Stadium stadium : returnedClub.getStadiums()) {
            deleteStadium(stadium);
        }
        clubRepository.deleteById(clubId);
    }

    public void deleteStadium(Stadium stadium) {
        stadiumRepository.delete(stadium);
    }

    public Club updateClub(int clubId, ClubUpdateDto clubUpdateDto) {
        var oldClub = clubRepository.findById(clubId).orElseThrow(
                () -> new ClubNotFoundException("Club with id: " + clubId + " is not found")
        );
        oldClub.setName(clubUpdateDto.name());
        oldClub.setAddress(clubUpdateDto.address());
        oldClub.setLatitude(clubUpdateDto.latitude());
        oldClub.setLongitude(clubUpdateDto.longitude());
        return clubRepository.save(oldClub);
    }

    public Stadium updateStadium(int stadiumId, StadiumUpdateDto sud) {
        var oldStadium = stadiumRepository.findById(stadiumId).orElseThrow(
                () -> new ClubNotFoundException("Stadium with id: " + stadiumId + " is not found")
        );
        oldStadium.setName(sud.name());
        oldStadium.setCost(sud.cost());
        oldStadium.setImages(sud.images());
        oldStadium.setFeatures(sud.features());
        oldStadium.setSize(sud.size());
        oldStadium.setDescription(sud.description());
        return stadiumRepository.save(oldStadium);
    }


    private void notifyFollowers(Club club, Reservation reservation) {
        List<UserClub> followers = userClubRepository.findByClub(club);

        String subject = "إلحق إحجز في ملعب " + club.getName();
        String content = "لقد تم إلغاء حجز الساغة" + reservation.getReservationTime() + "في ملعب " + reservation.getStadium().getName();


        for (UserClub userClub : followers) {
            emailService.sendEmail(userClub.getUser().getEmail(), subject, content);
        }
    }

    private ClubDto convertClubToDto(Club club) {
        var followers = club.getFollowers().stream().map(this::convertUserClubToDto).toList();
        return new ClubDto(club.getId(), club.getName(), club.getUser(), followers, club.getStadiums());
    }

    private UserClubDto convertUserClubToDto(UserClub userClub) {
        return new UserClubDto(userClub.getId(),
                convertUserToDto(userClub.getUser()));
    }

    private UserDto convertUserToDto(User user) {
        return new UserDto(user.getId(), user.getEmail());
    }

    public List<Club> getAdminClubs(User admin) {
        return clubRepository.findByUser(admin);
    }
}
