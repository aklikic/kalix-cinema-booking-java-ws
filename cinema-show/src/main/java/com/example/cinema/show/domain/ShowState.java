package com.example.cinema.show.domain;

import com.example.cinema.show.SeatStatus;
import com.example.cinema.show.ShowCommand;
import com.example.cinema.show.ShowCommandError;
import com.example.cinema.util.StateCommandProcessResult;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.cinema.show.ShowCommandError.*;
import static com.example.cinema.util.StateCommandProcessResult.error;
import static com.example.cinema.util.StateCommandProcessResult.result;

public record ShowState(String id, String title, BigDecimal seatPrice, Map<Integer, Seat> seats,
                        Map<String, Integer> pendingReservations,
                        Map<String, Integer> payedReservations,
                        int availableSeatsCount) {

    public ShowState(String id, String title, BigDecimal seatPrice){
        this(id ,title, seatPrice, new HashMap<>(), new HashMap<>(), new HashMap<>(), 0);
    }

    public record Seat(int number, SeatStatus status, Optional<String> walletId, Optional<String> reservationId, Optional<String> reservationCancellationId) {
        @JsonIgnore
        public boolean isAvailable() {
            return status == SeatStatus.AVAILABLE;
        }
        public Seat reserved(String walletId, String reservationId) {
            return new Seat(number, SeatStatus.RESERVED, Optional.of(walletId), Optional.of(reservationId), reservationCancellationId);
        }
        public Seat paid() {
            return new Seat(number, SeatStatus.PAID, walletId, reservationId,reservationCancellationId);
        }
        public Seat cancel(String reservationCancellationId){
            return new Seat(number, SeatStatus.CANCELING, walletId, reservationId,Optional.of(reservationCancellationId));
        }
        public Seat available() {
            return new Seat(number, SeatStatus.AVAILABLE, Optional.empty(),Optional.empty(),Optional.empty());
        }
    }

    private static final String EMPTY_SHOW_ID = "";

    public static ShowState empty() {
        return new ShowState(EMPTY_SHOW_ID, "",BigDecimal.ZERO);
    }

    public StateCommandProcessResult<ShowEvent, ShowCommandError> handleCommand(String showId, ShowCommand.CreateShow createShow){
        if(createShow.maxSeats() > 100){
            return error(TOO_MANY_SEATS);
        }
        if (isEmpty()) {
            return result(new ShowEvent.ShowCreated(showId, createShow.title(),createShow.seatPrice(),createSeats(createShow.maxSeats())));
        } else {
            return error(SHOW_ALREADY_EXISTS);
        }

    }

    public StateCommandProcessResult<ShowEvent, ShowCommandError> handleCommand(ShowCommand.ReserveSeat reserveSeat) {
        int seatNumber = reserveSeat.seatNumber();
        if (isDuplicate(reserveSeat.reservationId())) {
            return error(DUPLICATED_COMMAND);
        } else {
            if(seats.containsKey(seatNumber)){
                var seat = seats.get(seatNumber);
                if (seat.isAvailable()) {
                    return result(new ShowEvent.SeatReserved(id, reserveSeat.walletId(), reserveSeat.reservationId(), seatNumber,  seatPrice,availableSeatsCount()-1));
                } else {
                    return error(SEAT_NOT_AVAILABLE);
                }
            }else{
                return error(SEAT_NOT_FOUND);
            }
        }
    }

    public StateCommandProcessResult<ShowEvent, ShowCommandError> handleCommand(ShowCommand.CancelSeatReservation cancelSeatReservation) {
        String reservationId = cancelSeatReservation.reservationId();
        if(pendingReservations.containsKey(reservationId)){
            var seatNumber = pendingReservations.get(reservationId);
            if(seats.containsKey(seatNumber)){
                var seat = seats.get(seatNumber);
                var cancellationReservationId = UUID.randomUUID().toString();
                return result(new ShowEvent.SeatReservationCancelled(id, seat.walletId().get(), reservationId,  cancellationReservationId, seatNumber, availableSeatsCount()+1));
            }else{
                return error(SEAT_NOT_FOUND);
            }
        }else if (payedReservations.containsKey(reservationId)){
            return error(CANCELLING_CONFIRMED_RESERVATION);
        }else{
            return error(RESERVATION_NOT_FOUND);
        }
    }

    public StateCommandProcessResult<ShowEvent, ShowCommandError> handleCommand(ShowCommand.ConfirmReservationPayment confirmReservationPayment) {
        String reservationId = confirmReservationPayment.reservationId();
        if(pendingReservations.containsKey(reservationId)){
            var seatNumber = pendingReservations.get(reservationId);
            if(seats.containsKey(seatNumber)){
                var seat = seats.get(seatNumber);
                return result(new ShowEvent.SeatReservationPaid(id, reservationId, seatNumber));
            }else{
                return error(SEAT_NOT_FOUND);
            }
        }else{
            return error(RESERVATION_NOT_FOUND);
        }
    }


    private boolean isDuplicate(String reservationId) {
        return pendingReservations.containsKey(reservationId) ||
                payedReservations.containsKey(reservationId);
    }

    public ShowState onEvent(ShowEvent.ShowCreated event) {
        Map<Integer,Seat> seats = event.seats().stream().collect(Collectors.toMap(Seat::number, Function.identity()));
        return new ShowState(event.showId(), event.title(), event.price(),seats,pendingReservations,payedReservations,event.seats().size());
    }

    public ShowState onEvent(ShowEvent.SeatReserved event) {
        Seat seat = seats.get(event.seatNumber());
        seats.put(seat.number(), seat.reserved(event.walletId(), event.reservationId()));
        pendingReservations.put(event.reservationId(), event.seatNumber());
        return new ShowState(id, title, seatPrice, seats,pendingReservations,payedReservations,event.availableSeatsCount());
    }

    public ShowState onEvent(ShowEvent.SeatReservationPaid event) {
        Seat seat = seats.get(event.seatNumber());
        String reservationId = event.reservationId();
        seats.put(seat.number(), seat.paid());
        pendingReservations.remove(reservationId);
        payedReservations.put(reservationId, event.seatNumber());
        return new ShowState(id, title, seatPrice, seats,pendingReservations, payedReservations, availableSeatsCount());

    }

    public ShowState onEvent(ShowEvent.SeatReservationCancelled event) {
        Seat seat = seats.get(event.seatNumber());
        String reservationId = event.reservationId();
        seats.put(seat.number(), seat.available());
        pendingReservations.remove(reservationId);
        payedReservations.remove(reservationId);
        return new ShowState(id, title, seatPrice, seats,pendingReservations,payedReservations,event.availableSeatsCount());
    }

    public boolean isEmpty() {
        return id.equals(EMPTY_SHOW_ID);
    }
    private static List<Seat> createSeats(int maxSeats) {
        return IntStream.range(0, maxSeats).mapToObj(seatNum -> new Seat(seatNum, SeatStatus.AVAILABLE,Optional.empty(), Optional.empty(),Optional.empty())).toList();
    }
}
