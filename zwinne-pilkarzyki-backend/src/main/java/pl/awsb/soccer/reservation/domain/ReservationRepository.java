package pl.awsb.soccer.reservation.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<DbReservation, Long> {
    List<DbReservation> findAllByUserId(Long userId);
    boolean existsByStartAtBeforeAndEndAtAfter(LocalDateTime startAt, LocalDateTime endAt);
}