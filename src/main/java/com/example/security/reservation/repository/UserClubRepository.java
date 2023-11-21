package com.example.security.reservation.repository;

import com.example.security.reservation.entity.Club;
import com.example.security.reservation.entity.UserClub;
import com.example.security.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserClubRepository extends JpaRepository<UserClub, Integer> {
    Optional<UserClub> findByUserAndClub(User user, Club club);

    List<UserClub> findByClub(Club club);

    void deleteByUserAndClub(User user, Club club);
}
