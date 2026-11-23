package com.example.bookingservice.service;


import com.example.bookingservice.Exception.BookingNotFoundException;
import com.example.bookingservice.domain.Booking;
import com.example.bookingservice.domain.BookingItem;
import com.example.bookingservice.domain.BookingStatus;
import com.example.bookingservice.dto.BookingItemRequest;
import com.example.bookingservice.dto.CreateBookingRequest;
import com.example.bookingservice.feign.InventoryServiceRestClient;
import com.example.bookingservice.feign.PaymentServiceRestClient;
import com.example.bookingservice.repository.BookingItemRepository;
import com.example.bookingservice.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final InventoryServiceRestClient inventoryServiceRestClient;
    private final PaymentServiceRestClient paymentServiceRestClient;
    private final BookingItemRepository bookingItemRepository;

    public BookingService(
            BookingRepository bookingRepository,
            InventoryServiceRestClient inventoryServiceRestClient,
            PaymentServiceRestClient paymentServiceRestClient,
            BookingItemRepository bookingItemRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.paymentServiceRestClient = paymentServiceRestClient;
        this.bookingItemRepository = bookingItemRepository;
        this.inventoryServiceRestClient = inventoryServiceRestClient;
    }
    

    @Transactional
    public Booking createBooking(CreateBookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("booking request is required");
        }
        if (request.customerId() == null || request.eventId() == null) {
            throw new IllegalArgumentException("customer ID and event ID are required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("At least one seat is required");
        }

        Booking booking = new Booking();
        booking.setCustomerId(request.customerId());
        booking.setEventId(request.eventId());
        List<BookingItem> bookingItems = new ArrayList<>();
        List<UUID> seatIds = new ArrayList<>();
        long totalCents = 0;
        for (BookingItemRequest itemRequest : request.items()) {
            if (itemRequest == null || itemRequest.seatId() == null) {
                throw new IllegalArgumentException("seat ID is required");
            }
            if (itemRequest.priceCents() == null || itemRequest.priceCents() < 0) {
                throw new IllegalArgumentException("seat price is required");
            }
            if (seatIds.contains(itemRequest.seatId())) {
                throw new IllegalArgumentException("seat IDs must not be duplicated");
            }
            seatIds.add(itemRequest.seatId());
            totalCents += itemRequest.priceCents();
            BookingItem item = new BookingItem();
            item.setSeatId(itemRequest.seatId());
            item.setPriceCents(itemRequest.priceCents());
            bookingItems.add(item);
        }
        booking.setTotalCents(totalCents);

        InventoryServiceRestClient.StatusRequest statusRequest =
                new InventoryServiceRestClient.StatusRequest(booking.getEventId(), seatIds);
        List<InventoryServiceRestClient.SeatStatus> statuses =
                inventoryServiceRestClient.getSeatStatuses(statusRequest);
        if (statuses.size() != seatIds.size()) {
            throw new IllegalStateException("Inventory returned an invalid seat status response");
        }
        for (InventoryServiceRestClient.SeatStatus status : statuses) {
            if (status != InventoryServiceRestClient.SeatStatus.AVAILABLE) {
                throw new IllegalStateException("One or more selected seats are unavailable");
            }
        }

        Instant expiresAt = Instant.now().plusSeconds(900);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(Instant.now());
        Booking pendingBooking = bookingRepository.save(booking);
        InventoryServiceRestClient.HoldRequest holdRequest =
                new InventoryServiceRestClient.HoldRequest(
                        pendingBooking.getId(),
                        booking.getEventId(),
                        seatIds,
                        expiresAt
                );
        InventoryServiceRestClient.HoldResponse hold =
                inventoryServiceRestClient.holdSeats(holdRequest);
        pendingBooking.setInventoryHoldId(hold.holdId());
        pendingBooking.setHoldExpiresAt(hold.expiresAt());
        pendingBooking.setStatus(BookingStatus.HELD);
        bookingRepository.save(pendingBooking);

        boolean holdActive = true;
        try {
            Booking heldBooking = pendingBooking;
            for (BookingItem item : bookingItems) {
                item.setBookingId(heldBooking.getId());
                bookingItemRepository.save(item);
            }
            PaymentServiceRestClient.SimulatePaymentRequest paymentRequest =
                    new PaymentServiceRestClient.SimulatePaymentRequest(
                            heldBooking.getId(),
                            heldBooking.getTotalCents()
                    );
            PaymentServiceRestClient.PaymentResponse payment =
                    paymentServiceRestClient.simulate(paymentRequest);
            if (payment.status() != PaymentServiceRestClient.PaymentStatus.SUCCEEDED) {
                inventoryServiceRestClient.releaseHold(heldBooking.getId());
                holdActive = false;
                heldBooking.setStatus(BookingStatus.FAILED);
                return bookingRepository.save(heldBooking);
            }

            inventoryServiceRestClient.confirmHold(heldBooking.getId());
            holdActive = false;
            heldBooking.setStatus(BookingStatus.CONFIRMED);
            return bookingRepository.save(heldBooking);
        } catch (RuntimeException exception) {
            if (holdActive) {
                inventoryServiceRestClient.releaseHold(pendingBooking.getId());
            }
            throw exception;
        }
    }

    public Booking getBookingById(UUID bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("booking ID is required");
        }
        Optional<Booking> result = bookingRepository.findById(bookingId);
        if (result.isEmpty()) {
            throw new BookingNotFoundException("Booking not found: " + bookingId);
        }
        return result.get();
    }

    public List<Booking> getBookingsByCustomerId(UUID customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("customer ID is required");
        }
        return bookingRepository.findByCustomerId(customerId);
    }

    public List<Booking> getBookingsByEventId(UUID eventId) {
        if (eventId == null) {
            throw new IllegalArgumentException("event ID is required");
        }
        return bookingRepository.findByEventId(eventId);
    }

    public Booking updateBooking(Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("booking is required");
        }
        return bookingRepository.save(booking);
    }

    @Transactional
    public void deleteBooking(UUID bookingId) {
        if (bookingId == null) {
            throw new IllegalArgumentException("booking ID is required");
        }
        Optional<Booking> result = bookingRepository.findById(bookingId);
        if (result.isEmpty()) {
            throw new BookingNotFoundException("Booking not found: " + bookingId);
        }
        Booking booking = result.get();
        if (booking.getStatus() == BookingStatus.HELD && booking.getInventoryHoldId() != null) {
            inventoryServiceRestClient.releaseHold(booking.getId());
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    public Optional<Booking> findByInventoryHoldId(UUID inventoryHoldId) {
        if (inventoryHoldId == null) {
            throw new IllegalArgumentException("inventory hold ID is required");
        }
        return Optional.ofNullable(bookingRepository.findByInventoryHoldId(inventoryHoldId));
    }

    public List<Booking> getBookings() {
        return bookingRepository.findAll();
    }
}
