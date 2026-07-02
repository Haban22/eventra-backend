package com.eventra.backend.module.auth.controller;

import com.eventra.backend.module.auth.repository.UserRepository;
import com.eventra.backend.module.booking.repository.BookingRepository;
import com.eventra.backend.module.event.repository.EventRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final UserRepository    userRepository;
    private final EventRepository   eventRepository;
    private final BookingRepository bookingRepository;

    public AdminAnalyticsController(UserRepository userRepository,
                                    EventRepository eventRepository,
                                    BookingRepository bookingRepository) {
        this.userRepository    = userRepository;
        this.eventRepository   = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/overview")
    public Map<String, Object> overview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total_users",    userRepository.count());
        data.put("total_events",   eventRepository.count());
        data.put("total_bookings", bookingRepository.count());
        data.put("total_revenue",  0);
        data.put("pending_payouts", 0);
        data.put("active_organizers", 0);
        return data;
    }

    @GetMapping("/trend")
    public List<Map<String, Object>> trend(@RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date",      date.toString());
            day.put("new_users", 0);
            day.put("bookings",  0);
            result.add(day);
        }
        return result;
    }
}
