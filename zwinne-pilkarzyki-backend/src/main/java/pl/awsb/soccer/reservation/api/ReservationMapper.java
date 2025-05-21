package pl.awsb.soccer.reservation.api;

import org.mapstruct.Mapper;
import pl.awsb.soccer.reservation.api.response.Reservation;
import pl.awsb.soccer.reservation.domain.DbReservation;

@Mapper
public interface ReservationMapper {
    Reservation toReservation(DbReservation dbReservation);
}
