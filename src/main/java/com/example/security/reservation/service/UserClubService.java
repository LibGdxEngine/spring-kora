package com.example.security.reservation.service;

import com.example.security.reservation.entity.Club;
import com.example.security.reservation.entity.UserClub;
import com.example.security.reservation.repository.ClubRepository;
import com.example.security.reservation.repository.UserClubRepository;
import com.example.security.user.User;
import com.example.security.user.UserRepository;
import com.example.security.utils.exceptions.ClubNotFoundException;
import com.example.security.utils.exceptions.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserClubService {

    private final UserClubRepository userClubRepository;

    private final UserRepository userRepository;

    private final ClubRepository clubRepository;


    @Transactional
    public void followClub(User user, Integer clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        // Check if the relationship already exists
        Optional<UserClub> userClubRelation = userClubRepository.findByUserAndClub(user, club);

        if (userClubRelation.isPresent()) {
            throw new IllegalStateException("User already follows this club");
        }

        UserClub userClub = new UserClub();
        userClub.setUser(user);
        userClub.setClub(club);

        userClubRepository.save(userClub);
    }


    @Transactional
    public void unfollowClub(User user, Integer clubId) {

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        userClubRepository.deleteByUserAndClub(user, club);
    }
}
