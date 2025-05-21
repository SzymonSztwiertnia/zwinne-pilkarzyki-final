package pl.awsb.soccer.reservation.api.request;

import java.time.LocalDateTime;

public record CreateReservationRequest(
        String name,
        String description,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}
