package com.eventra.backend.module.analytics.service;

import com.eventra.backend.module.analytics.dto.AdminAnalyticsOverviewResponse;
import com.eventra.backend.module.analytics.dto.AdminAnalyticsTrendResponse;
import com.eventra.backend.module.analytics.dto.DailyCountPoint;
import com.eventra.backend.module.auth.entity.User;
import com.eventra.backend.module.auth.entity.UserRole;
import com.eventra.backend.module.auth.entity.UserStatus;
import com.eventra.backend.module.auth.repository.UserRepository;
import com.eventra.backend.module.booking.entity.Booking;
import com.eventra.backend.module.booking.enums.BookingStatus;
import com.eventra.backend.module.booking.repository.BookingRepository;
import com.eventra.backend.module.event.enums.EventStatus;
import com.eventra.backend.module.event.repository.EventRepository;
import com.eventra.backend.module.wallet.enums.PayoutStatus;
import com.eventra.backend.module.wallet.repository.PayoutRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Direct aggregate queries against existing normalized tables — no revival of the
// empty event_analytics/predicted_attendance/user_profiles/recommendations stub
// tables, since those have no writers and a direct query service is simpler and
// always reflects real, current data.
@Service
public class AnalyticsService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final PayoutRequestRepository payoutRequestRepository;

    public AnalyticsService(UserRepository userRepository,
                            EventRepository eventRepository,
                            BookingRepository bookingRepository,
                            PayoutRequestRepository payoutRequestRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.payoutRequestRepository = payoutRequestRepository;
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsOverviewResponse getOverview() {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        return new AdminAnalyticsOverviewResponse(
                userRepository.count(),
                userRepository.countByRole(UserRole.ATTENDEE),
                userRepository.countByRole(UserRole.ORGANIZER),
                userRepository.countByRole(UserRole.ADMIN),
                userRepository.countByStatus(UserStatus.ACTIVE),
                userRepository.countByStatus(UserStatus.SUSPENDED),
                userRepository.countByStatus(UserStatus.BANNED),
                userRepository.findByCreatedAtAfter(thirtyDaysAgo).size(),
                eventRepository.count(),
                eventRepository.countByStatus(EventStatus.PUBLISHED),
                eventRepository.countByStatus(EventStatus.PENDING_APPROVAL),
                eventRepository.countByStatus(EventStatus.DRAFT),
                bookingRepository.countByStatus(BookingStatus.CONFIRMED),
                bookingRepository.countByStatus(BookingStatus.CANCELLED),
                bookingRepository.sumTotalAmountByStatus(BookingStatus.CONFIRMED),
                payoutRequestRepository.countByStatus(PayoutStatus.PENDING),
                payoutRequestRepository.countByStatus(PayoutStatus.APPROVED),
                payoutRequestRepository.countByStatus(PayoutStatus.REJECTED)
        );
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsTrendResponse getTrend(int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);

        List<User> newUsers = userRepository.findByCreatedAtAfter(since);
        List<Booking> newBookings = bookingRepository.findByStatusAndCreatedAtAfter(BookingStatus.CONFIRMED, since);

        return new AdminAnalyticsTrendResponse(
                bucketByDay(newUsers.stream().map(User::getCreatedAt).toList(), days),
                bucketByDay(newBookings.stream().map(Booking::getCreatedAt).toList(), days)
        );
    }

    // Real day-by-day counts from actual created_at timestamps — on a freshly
    // seeded demo database most rows share the same seed timestamp, so this may
    // look like a single spike rather than a smooth trend; it's genuine computed
    // data either way, not fabricated.
    private List<DailyCountPoint> bucketByDay(List<Instant> timestamps, int days) {
        Map<LocalDate, Long> counts = timestamps.stream()
                .map(t -> t.atZone(ZoneOffset.UTC).toLocalDate())
                .collect(Collectors.groupingBy(d -> d, Collectors.counting()));

        LocalDate today = Instant.now().atZone(ZoneOffset.UTC).toLocalDate();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;

        return java.util.stream.IntStream.rangeClosed(0, days - 1)
                .mapToObj(i -> today.minusDays(days - 1L - i))
                .map(date -> new DailyCountPoint(date.format(fmt), counts.getOrDefault(date, 0L)))
                .toList();
    }
}
