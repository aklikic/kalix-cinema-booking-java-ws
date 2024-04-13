package com.example.cinema.booking;

public record SeatBookingCommand(String showId, int seatNumber, String walletId) {}
