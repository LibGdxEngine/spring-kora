package com.example.security.reservation.entity;

import com.example.security.user.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_club")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserClub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-userclub")
    private User user;

    @ManyToOne
    @JoinColumn(name = "club_id")
    @JsonBackReference("club-userclub")
    private Club club;

    @Override
    public String toString() {
        return "Stadium{" +
                "id=" + id +
                "user=" + user.getUsername() +
                "club=" + club.getName() +
                '}';
    }
}

