package pl.awsb.soccer.reservation.api.response;

import java.time.LocalDateTime;


public record Reservation(
        Long id,
        String name,
        String description,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}