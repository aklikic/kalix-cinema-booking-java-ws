package com.example.cinema.booking;

record SeatBookingCommand(String showId, int seatNumber, String walletId) {}
