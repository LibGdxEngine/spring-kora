package com.example.security.reservation;

import com.example.security.reservation.dto.ClubUpdateDto;
import com.example.security.reservation.dto.StadiumUpdateDto;
import com.example.security.reservation.entity.*;
import com.example.security.reservation.repository.ClubRepository;
import com.example.security.reservation.repository.ReservationRepository;
import com.example.security.reservation.repository.StadiumRepository;
import com.example.security.reservation.repository.UserClubRepository;
import com.example.security.reservation.service.ReservationService;
import com.example.security.user.User;
import com.example.security.utils.EmailService;
import com.example.security.utils.exceptions.ClubNotFoundException;
import com.example.security.utils.exceptions.TimeSlotUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private StadiumRepository stadiumRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserClubRepository userClubRepository;
    private User userMock;
    private Club clubMock;
    private Stadium stadiumMock;
    private List<Club> clubsListMock;
    private List<Reservation> reservationsListMock;
    private UserClub userClubMock;

    @BeforeEach
    public void beforeEach() {
        userMock = new User();
        userMock.setEmail("ahmed.fathy1445@gmail.com");
        clubMock = Club.builder()
                .id(1)
                .stadiums(List.of())
                .name("ClubMock")
                .followers(List.of())
                .build();
        stadiumMock = Stadium.builder()
                .id(1)
                .club(clubMock)
                .build();

        clubsListMock = getClubsListMock();
        reservationsListMock = getReservationsListMock();
        userClubMock = new UserClub(1, userMock, clubMock);
    }

    private Reservation createMockPinnedReservation(LocalDateTime reservationTime) {
        return Reservation.builder()
                .id(1)
                .stadium(stadiumMock)
                .user(userMock)
                .reservationTime(reservationTime)
                .status(ReservationStatus.PINNED)
                .build();
    }

    private  List<Reservation> getReservationsListMock() {
        return Stream.of(
                Reservation.builder()
                        .id(1)
                        .reservationTime(LocalDateTime.now())
                        .stadium(stadiumMock)
                        .build(),
                Reservation.builder()
                        .id(2)
                        .reservationTime(LocalDateTime.now().plusHours(2))
                        .build()
        ).collect(Collectors.toList());
    }

    private static List<Club> getClubsListMock() {
        return Stream.of(
                Club.builder()
                        .id(1)
                        .latitude(30.321607343277815)
                        .longitude(31.72472743888783)
                        .followers(List.of())
                        .stadiums(List.of())
                        .build(),
                Club.builder()
                        .id(2)
                        .latitude(30.32165751132323)
                        .longitude(31.72009976574753)
                        .followers(List.of())
                        .stadiums(List.of())
                        .build()
        ).collect(Collectors.toList());
    }

    @Test
    void getSingleClubWithIdReturnValidClub() {
        // Arrange
        when(clubRepository.findById(1)).thenReturn(Optional.of(clubMock));
        // Act
        var returnedClub = reservationService.getClub(1);
        // Assert
        assertEquals(returnedClub.id(), 1);
        verify(clubRepository).findById(any(Integer.class));
    }

    @Test
    void deleteSingleClubWithIdReturnSuccessMessage() {
        // Arrange
        doNothing().when(clubRepository).deleteById(any(Integer.class));
        when(clubRepository.findById(1)).thenReturn(Optional.of(clubMock));
        // Act
        reservationService.deleteClub(1);
        // Assert
        verify(clubRepository).deleteById(any(Integer.class));
        verify(clubRepository).findById(any(Integer.class));
    }

    @Test
    void deleteSingleStadiumWithIdReturnSuccessMessage() {
        // Arrange
        doNothing().when(stadiumRepository).delete(any(Stadium.class));
        // Act
        reservationService.deleteStadium(stadiumMock);
        // Assert
        verify(stadiumRepository).delete(any(Stadium.class));
    }

    @Test
    void testUpdateSingleStadium_ReturnUpdatedStadium() {
        // Arrange
        StadiumUpdateDto stadiumUpdateDto = new StadiumUpdateDto("", "",
                "features", "cost", List.of("", ""), "description");
        when(stadiumRepository.findById(any())).thenReturn(Optional.of(stadiumMock));
        when(stadiumRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        // Act
        var updatedClub = reservationService.updateStadium(1, stadiumUpdateDto);
        // Assert
        assertEquals(updatedClub.getId(), 1);
        assertEquals(updatedClub.getName(), "");
        assertEquals(updatedClub.getSize(), "");
        assertEquals(updatedClub.getFeatures(), "features");
        assertEquals(updatedClub.getCost(), "cost");
        assertEquals(updatedClub.getImages(), List.of("",""));
        assertEquals(updatedClub.getDescription(), "description");

        verify(stadiumRepository).findById(any());
        verify(stadiumRepository).save(any());
    }

    @Test
    void updateClubWithIdReturnSuccessMessage() {
        // Arrange
        ClubUpdateDto clubUpdateDto = new ClubUpdateDto("", "", 1.2, 2.2);
        when(clubRepository.findById(any())).thenReturn(Optional.of(clubMock));
        when(clubRepository.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        // Act
        var updatedClub = reservationService.updateClub(1, clubUpdateDto);
        // Assert
        assertEquals(updatedClub.getId(), 1);
        assertEquals(updatedClub.getName(), "");
        assertEquals(updatedClub.getAddress(), "");
        assertEquals(updatedClub.getLatitude(), 1.2);
        assertEquals(updatedClub.getLongitude(), 2.2);

        verify(clubRepository).findById(any());
        verify(clubRepository).save(any());
    }

    @Test
    void createSingleStadiumForClubReturnValidStadium() {
        // Arrange
        when(clubRepository.findById(1)).thenReturn(Optional.of(clubMock));
        when(stadiumRepository.save(any(Stadium.class))).thenAnswer(invocations -> invocations.getArgument(0));
        // Act
        var createdStadium = reservationService.createStadiumForClub(stadiumMock, 1);
        // Assert
        assertNotNull(createdStadium);
        assertEquals(createdStadium.getId(), 1);
        assertEquals(createdStadium.getClub().getId(), 1);

        verify(clubRepository).findById(any(Integer.class));
        verify(stadiumRepository).save(any(Stadium.class));

    }

    @Test
    void createSingleStadiumForNotCreatedClubReturnException() {
        // Arrange
        when(clubRepository.findById(12345)).thenReturn(Optional.empty());
        assertThrows(ClubNotFoundException.class, () -> {
            reservationService.createStadiumForClub(stadiumMock, 12345);
        });
    }

    @Test
    void getAllClubs() {
        // Arrange
        when(clubRepository.findAll()).thenReturn(clubsListMock);
        // Act
        var returnedClubs = reservationService.getAllClubs();
        // Assert
        assertEquals(returnedClubs.size(), 2);

        verify(clubRepository).findAll();
    }

    @Test
    void getAllClubsWithinRange1000MeterReturnValidClubs() {
        // Arrange
        when(clubRepository.findClubsWithinDistanceFromLocation(
                any(Double.class),
                any(Double.class),
                any(Double.class)
        )).thenReturn(clubsListMock);
        // Act
        var returnedClubs = reservationService.getClubsNearLocation(
                30.319976867816333,
                31.722925770224098,
                1000);
        // Assert
        assertEquals(2, returnedClubs.size());

        verify(clubRepository).findClubsWithinDistanceFromLocation(
                any(Double.class),
                any(Double.class),
                any(Double.class));
    }

    @Test
    void testCheckReservedSlotExistReturnReservationSlot() {
        // Arrange
        when(reservationRepository.findReservationByStadiumAndHour(
                any(Integer.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class))
        ).thenReturn(Optional.of(reservationsListMock.get(0)));
        // Act
        var slot = reservationService.getReservationSlot(1, LocalDateTime.now());
        // Assert
        assertTrue(slot.isPresent());
        assertEquals(slot.get().getReservationTime().getHour(), LocalDateTime.now().getHour());

        verify(reservationRepository).findReservationByStadiumAndHour(
                any(Integer.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    void createNewReservationInEmptySlot_ReturnValidReservation() {
        // Arrange
        when(stadiumRepository.findById(any(Integer.class))).thenReturn(Optional.of(stadiumMock));
        when(reservationService.getReservationSlot(1, LocalDateTime.now()))
                .thenReturn(Optional.empty());
        // Act
        var createdReservation = reservationService.createReservation(userMock, 1, LocalDateTime.now(), "Mazen");
        // Assert
        assertNotNull(createdReservation);
        assertEquals(LocalDateTime.now().getHour(), createdReservation.getReservationTime().getHour());
        assertEquals(createdReservation.getPlayerName(), "Mazen");

        verify(stadiumRepository).findById(any(Integer.class));
        verify(reservationRepository).save(createdReservation);
        verify(reservationRepository).findReservationByStadiumAndHour(any(Integer.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    void createNewReservationInCanceledSlot_ReturnValidReservation() {
        // Arrange
        when(stadiumRepository.findById(any(Integer.class))).thenReturn(Optional.of(stadiumMock));
        when(reservationService.getReservationSlot(1, LocalDateTime.now()))
                .thenReturn(Optional.of(Reservation.builder()
                        .id(12)
                        .reservationTime(LocalDateTime.now())
                        .status(ReservationStatus.CANCELED)
                        .build()));
        // Act
        var createdReservation = reservationService.createReservation(userMock, 1, LocalDateTime.now(), "Mazen");
        // Assert
        assertNotNull(createdReservation);
        assertEquals(LocalDateTime.now().getHour(), createdReservation.getReservationTime().getHour());

        verify(stadiumRepository).findById(any(Integer.class));
        verify(reservationRepository).save(createdReservation);
        verify(reservationRepository).findReservationByStadiumAndHour(any(Integer.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    void createNewReservationForSlotThatExist_ThrowsException() {
        // Arrange
        when(stadiumRepository.findById(any(Integer.class))).thenReturn(Optional.of(stadiumMock));
        when(reservationService.getReservationSlot(1, LocalDateTime.now()))
                .thenReturn(Optional.of(getReservationsListMock().get(0)));
        // Act
        // Assert
        assertThrows(TimeSlotUnavailableException.class, () -> {
            reservationService.createReservation(userMock, 1, LocalDateTime.now(), "Mazen");
        });
    }

    @Test
    void createPinnedReservation() {
        // Arrange
        when(stadiumRepository.findById(any(Integer.class))).thenReturn(Optional.of(stadiumMock));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocations -> invocations.getArgument(0));
        // Act
        var createdReservation = reservationService.createPinnedReservation(userMock, 1, LocalDateTime.now(), "Mazen");
        // Assert
        assertEquals(createdReservation.getStatus(), ReservationStatus.PINNED);
        assertEquals(createdReservation.getPlayerName(), "Mazen");

        verify(reservationRepository, times(2)).save(any(Reservation.class));
    }

    @Test
    void whenHandleWeeklyRecurringReservations_thenCreateNewReservations() {
        // Arrange
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Reservation pinnedReservation = createMockPinnedReservation(yesterday.atStartOfDay());

        when(reservationRepository.findPinnedReservationsFrom(yesterday))
                .thenReturn(Collections.singletonList(pinnedReservation));

        // Act
        reservationService.handleWeeklyPinnedReservations();
        // Assert
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void cancelReservationWithId_AndSendNotificationToFollowers() {
        // Arrange
        when(reservationRepository.findById(any(Integer.class))).thenReturn(Optional.of(reservationsListMock.get(0)));
        when(reservationRepository.cancelReservation(any(Integer.class)))
                .thenReturn(1);
        when(userClubRepository.findByClub(any())).thenReturn(List.of(userClubMock));
        doNothing().when(emailService).sendEmail(any(), any(),any());
        // Act
        boolean updatedRows = reservationService.cancelReservation(1);
        // Assert
        assertTrue(updatedRows);
        verify(reservationRepository).findById(any(Integer.class));
        verify(reservationRepository).cancelReservation(any(Integer.class));
        verify(userClubRepository).findByClub(any());
        verify(emailService).sendEmail(any(), any(), any());
    }

    @Test
    void cancelPinnedReservation() {
        // Arrange
        when(reservationRepository.findById(any(Integer.class)))
                .thenReturn(Optional.of(createMockPinnedReservation(LocalDateTime.now())));
        // Act
        boolean cancelled = reservationService.cancelReservation(1);
        // Assert
        assertTrue(cancelled);

        verify(reservationRepository).cancelReservation(any(Integer.class));
        verify(reservationRepository).findReservationByStadiumAndHour(
                any(Integer.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        );
    }

    @Test
    void showStadiumReservationsForSpecificDay() {
        // Arrange
        when(reservationRepository.findReservationsByStadiumAndDate(any(Integer.class), any(LocalDate.class)))
                .thenReturn(reservationsListMock);
        // Act
        var returnedReservations = reservationService.getReservationsOfStadiumInSingleDay(1, LocalDate.now());
        // Assert

        assertEquals(2, returnedReservations.size());

        verify(reservationRepository).findReservationsByStadiumAndDate(
                any(Integer.class),
                any(LocalDate.class)
        );
    }

    @Test
    void showStadiumReservationForNextXDays() {
        // Arrange
        when(stadiumRepository.findById(any(Integer.class))).thenReturn(Optional.of(stadiumMock));
        when(reservationRepository.findReservationsByStadiumAndReservationTimeBetween(
                any(Stadium.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class))
        ).thenReturn(getReservationsListMock());
        // Act
        var returnedReservations = reservationService.getReservationsForNextXDays(1, 14);
        // Assert
        assertEquals(2, returnedReservations.size());

        verify(stadiumRepository).findById(any(Integer.class));
        verify(reservationRepository).findReservationsByStadiumAndReservationTimeBetween(
                any(Stadium.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class));
    }

    @Test
    void testCreateClub_ReturnValidClub() {
        // Arrange
        when(clubRepository.save(any(Club.class))).thenAnswer(invocations -> invocations.getArgument(0));
        // Act
        var createdClub = reservationService.createClub(clubMock);
        // Assert
        assertEquals(createdClub.getId(), clubMock.getId());

        verify(clubRepository).save(createdClub);
    }

    @Test
    void getClubsOfSpecificUser_ReturnValidClubs() {
        // Arrange
        when(clubRepository.findByUser(any(User.class))).thenReturn(clubsListMock);
        // Act
        var returnedClubs = reservationService.getAdminClubs(userMock);
        // Assert
        assertEquals(returnedClubs.size(), clubsListMock.size());

        verify(clubRepository).findByUser(any(User.class));
    }

}
