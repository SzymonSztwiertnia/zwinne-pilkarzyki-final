package pl.awsb.soccer.reservation.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import pl.awsb.soccer.user.domain.DbUser;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "reservations")
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class DbReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    DbUser user;

    String name;

    String description;

    LocalDateTime startAt;

    LocalDateTime endAt;
}