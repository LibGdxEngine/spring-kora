package com.example.security.reservation.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "stadiums")
public class Stadium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private String size;
    private String features;
    private String cost;
    private String description;
    @ElementCollection(fetch = FetchType.EAGER)
    @Lob
    @Column(columnDefinition = "TEXT")
    private List<String> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id")
    @JsonBackReference("club-stadium")
    private Club club;

    @OneToMany(mappedBy = "stadium", cascade = CascadeType.ALL)
    @JsonManagedReference("stadium-reservation")
    private List<Reservation> reservations;

    @Override
    public String toString() {
        return "Stadium{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", size='" + size + '\'' +
                // ... other fields ...
                '}';
    }
}
