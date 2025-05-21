package pl.awsb.soccer.reservation.api;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import pl.awsb.soccer.reservation.ReservationService;
import pl.awsb.soccer.reservation.api.request.CreateReservationRequest;
import pl.awsb.soccer.reservation.api.request.UpdateReservationRequest;
import pl.awsb.soccer.reservation.api.response.Reservation;
import pl.awsb.soccer.security.CustomUserDetails;
import pl.awsb.soccer.security.LoggedUser;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = lombok.AccessLevel.PRIVATE)
public class ReservationController {
    ReservationService reservationService;


    @GetMapping
    public List<Reservation> getReservations(@LoggedUser CustomUserDetails userDetails) {
        return reservationService.getReservations(userDetails.getUser());
    }

    @PostMapping
    public Reservation createReservation(@LoggedUser CustomUserDetails userDetails, @RequestBody CreateReservationRequest request) {
        return reservationService.createReservation(userDetails.getUser(), request);
    }

    @DeleteMapping("/{id}")
    public void deleteReservation(@PathVariable Long id, @LoggedUser CustomUserDetails userDetails) {
        reservationService.deleteReservation(id, userDetails.getUser());
    }

    @PutMapping
    public Reservation updateReservation(@LoggedUser CustomUserDetails userDetails, @RequestBody UpdateReservationRequest request) {
        return reservationService.updateReservation(userDetails.getUser(), request);
    }
}
