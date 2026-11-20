package com.example.bookingservice.repository;

import com.example.bookingservice.domain.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BookingItemRepository extends JpaRepository<BookingItem, UUID> {

    List<BookingItem> findByBookingId(UUID bookingId);

    void deleteByBookingId(UUID bookingId);
}
