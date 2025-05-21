package pl.awsb.soccer.reservation;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import pl.awsb.soccer.exception.AccessDeniedException;
import pl.awsb.soccer.reservation.api.ReservationMapper;
import pl.awsb.soccer.reservation.api.request.CreateReservationRequest;
import pl.awsb.soccer.reservation.api.request.UpdateReservationRequest;
import pl.awsb.soccer.reservation.api.response.Reservation;
import pl.awsb.soccer.reservation.domain.DbReservation;
import pl.awsb.soccer.reservation.domain.ReservationRepository;
import pl.awsb.soccer.user.domain.DbUser;

import java.util.List;

@Service
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class ReservationService {
    ReservationRepository repository;
    ReservationMapper mapper;

    public List<Reservation> getReservations(DbUser user) {
        return repository.findAllByUserId(user.getId()).stream().map(mapper::toReservation).toList();
    }

    public Reservation createReservation(DbUser user, CreateReservationRequest request) {
        DbReservation dbReservation = DbReservation.builder()
                .name(request.name())
                .description(request.description())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .user(user)
                .build();

        dbReservation = repository.save(dbReservation);

        return mapper.toReservation(dbReservation);
    }

    public void deleteReservation(Long id, DbUser user) {
        DbReservation dbReservation = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie odnaleziono rezerwacji."));

        if (!dbReservation.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Nie masz uprawnień do usunięcia tej rezerwacji.");
        }

        repository.delete(dbReservation);
    }

    public Reservation updateReservation(DbUser user, UpdateReservationRequest request) {
        DbReservation dbReservation = repository.findById(request.id())
                .orElseThrow(() -> new IllegalArgumentException("Nie odnaleziono rezerwacji."));

        if (!dbReservation.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Nie masz uprawnień do usunięcia tej rezerwacji.");
        }

        dbReservation = dbReservation.toBuilder()
                .name(request.name())
                .description(request.description())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .build();

        dbReservation = repository.save(dbReservation);

        return mapper.toReservation(dbReservation);
    }
}