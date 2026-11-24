package com.example.bookingservice.mcp;

import com.example.bookingservice.domain.Booking;
import com.example.bookingservice.Exception.BookingNotFoundException;
import com.example.bookingservice.service.BookingService;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class BookingMcpTools {

    private final BookingService bookingService;

    public BookingMcpTools(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @McpTool(
            name = "getMyBookings",
            description = "Get all bookings belonging to a customer")
    public List<Booking> getMyBookings(
            @McpToolParam(description = "Customer UUID", required = true) UUID customerId
    ) {
        return bookingService.getBookingsByCustomerId(customerId);
    }

    @McpTool(
            name = "getBookingStatus",
            description = "Get the status and details of a booking"
    )
    public Booking getBookingStatus(
            @McpToolParam(description = "Booking UUID", required = true) UUID bookingId
    ) {
        try {
            return bookingService.getBookingById(bookingId);
        } catch (BookingNotFoundException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }
    }

}
