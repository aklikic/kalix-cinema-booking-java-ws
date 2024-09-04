package com.example.cinema.booking.api;

public record SeatBookingCommand(String showId, int seatNumber, String walletId) {}
