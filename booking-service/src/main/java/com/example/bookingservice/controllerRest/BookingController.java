package com.example.bookingservice.controllerRest;

import com.example.bookingservice.domain.Booking;
import com.example.bookingservice.dto.CreateBookingRequest;
import com.example.bookingservice.service.BookingService;
import com.example.bookingservice.Exception.BookingNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<Booking> getBookings() {
        return bookingService.getBookings();
    }


    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Booking createBooking(@RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String bookingNotFound(BookingNotFoundException exception) {
        return exception.getMessage();
    }

    @GetMapping("/{bookingId}")
    public Booking getBookingById(@PathVariable UUID bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    @GetMapping("/customer/{customerId}")
    public List<Booking> getBookingsByCustomerId(@PathVariable UUID customerId) {
        return bookingService.getBookingsByCustomerId(customerId);
    }

    @GetMapping("/event/{eventId}")
    public List<Booking> getBookingsByEventId(@PathVariable UUID eventId) {
        return bookingService.getBookingsByEventId(eventId);
    }

    @DeleteMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelBooking(@PathVariable UUID bookingId) {
        bookingService.deleteBooking(bookingId);
    }
}
