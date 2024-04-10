package com.example.cinema.show;

import com.example.cinema.util.StateCommandProcessResult;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.cinema.show.ShowCommandError.*;
import static com.example.cinema.util.StateCommandProcessResult.error;
import static com.example.cinema.util.StateCommandProcessResult.result;

public record ShowState(String id, String title, Map<Integer, Seat> seats,
                        Map<String, Integer> pendingReservations,
                        Map<String, FinishedReservation> payedReservations,
                        int availableSeatsCount) {

    public ShowState(String id, String title){
        this(id ,title, new HashMap<>(), new HashMap<>(), new HashMap<>(), 0);
    }

    public record Seat(int number, SeatStatus status, BigDecimal price) {
        @JsonIgnore
        public boolean isAvailable() {
            return status == SeatStatus.AVAILABLE;
        }
        public Seat reserved() {
            return new Seat(number, SeatStatus.RESERVED, price);
        }
        public Seat paid() {
            return new Seat(number, SeatStatus.PAID, price);
        }
        public Seat available() {
            return new Seat(number, SeatStatus.AVAILABLE, price);
        }
    }

    public enum SeatStatus {
        AVAILABLE, RESERVED, PAID
    }

    public record FinishedReservation(String reservationId, int seatNumber) { }

    private static final String EMPTY_SHOW_ID = "";

    public static ShowState empty() {
        return new ShowState(EMPTY_SHOW_ID, "");
    }

    public StateCommandProcessResult<ShowEvent, ShowCommandError> handleCommand(String showId, ShowCommand.CreateShow createShow){
        if(createShow.maxSeats() > 100){
            return error(TOO_MANY_SEATS);
        }
        if (isEmpty()) {
            return result(new ShowEvent.ShowCreated(showId, createShow.title(),createSeats(createShow.seatPrice(),createShow.maxSeats())));
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
                    return result(new ShowEvent.SeatReserved(id, reserveSeat.walletId(), reserveSeat.reservationId(), seatNumber, seat.price(), availableSeatsCount()-1));
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
                return result(new ShowEvent.SeatReservationCancelled(id, reservationId, seatNumber, availableSeatsCount()+1));
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
        return new ShowState(event.showId(), event.title(),seats,pendingReservations,payedReservations,event.seats().size());
    }

    public ShowState onEvent(ShowEvent.SeatReserved event) {
        Seat seat = seats.get(event.seatNumber());
        seats.put(seat.number(), seat.reserved());
        pendingReservations.put(event.reservationId(), event.seatNumber());
        return new ShowState(id, title, seats,pendingReservations,payedReservations,event.availableSeatsCount());
    }

    public ShowState onEvent(ShowEvent.SeatReservationPaid event) {
        Seat seat = seats.get(event.seatNumber());
        String reservationId = event.reservationId();
        FinishedReservation finishedReservation = new FinishedReservation(reservationId, seat.number());
        seats.put(seat.number(), seat.paid());
        pendingReservations.remove(reservationId);
        payedReservations.put(reservationId, finishedReservation);
        return new ShowState(id, title, seats,pendingReservations, payedReservations, availableSeatsCount());

    }

    public ShowState onEvent(ShowEvent.SeatReservationCancelled event) {
        Seat seat = seats.get(event.seatNumber());
        String reservationId = event.reservationId();
        FinishedReservation finishedReservation = new FinishedReservation(reservationId, seat.number());
        seats.put(seat.number(), seat.available());
        pendingReservations.remove(reservationId);
        payedReservations.remove(reservationId);
        return new ShowState(id, title, seats,pendingReservations,payedReservations,event.availableSeatsCount());
    }

    public boolean isEmpty() {
        return id.equals(EMPTY_SHOW_ID);
    }
    private static List<Seat> createSeats(BigDecimal seatPrice, int maxSeats) {
        return IntStream.range(0, maxSeats).mapToObj(seatNum -> new Seat(seatNum, SeatStatus.AVAILABLE, seatPrice)).toList();
    }
}
