package com.example.security.reservation.entity;

import com.example.security.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "clubs")
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL)
    @JsonManagedReference("club-stadium")
    private List<Stadium> stadiums = new ArrayList<>();

    @OneToMany(mappedBy = "club")
    @JsonManagedReference("club-userclub")
    private List<UserClub> followers;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public String toString() {
        return "Club{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
