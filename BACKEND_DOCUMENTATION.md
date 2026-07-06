# EVENTRA BACKEND - COMPREHENSIVE DOCUMENTATION

## Quick Overview

**Eventra Backend** is a Spring Boot 3.5.14 microservices application for event management, with real implementations for:
- ✅ User authentication (JWT, OAuth2, email verification, OTP)
- ✅ Event management (CRUD, approval workflow, scheduling, bookmarks, venues)
- ✅ Booking system (ticket reservations, refunds)
- ✅ Gamification (rewards, achievements)
- ✅ Community (discussions, messaging, moderation)
- ✅ Wallet & Payouts (transaction management)
- ✅ Notifications (in-app alerts)
- ✅ Admin controls (audit logs, user suspension, event approval)
- ✅ Python FastAPI AI Microservice (recommendations, sentiment analysis, attendance prediction)

---

## Table of Contents

1. [Stack & Architecture](#stack--architecture)
2. [Project Structure](#project-structure)
3. [Module Implementation Status](#module-implementation-status)
4. [How to Run](#how-to-run)
5. [API Endpoints](#api-endpoints)
6. [Database Schema](#database-schema)
7. [Testing](#testing)

---

## Stack & Architecture

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Language** | Java | 21 LTS |
| **Framework** | Spring Boot | 3.5.14 |
| **Build** | Maven | 3.8+ |
| **Database** | PostgreSQL | 13+ |
| **Cache** | Redis | 6.0+ |
| **Migrations** | Flyway | (Latest) |
| **Auth** | JWT (JJWT) | 0.12.6 |
| **OAuth2** | Google | Client 2.7.2 |
| **Testing** | JUnit 5 + Testcontainers | (Latest) |
| **AI Service** | FastAPI + Python | 3.8+ |
| **AI Libraries** | sentence-transformers, scikit-learn, torch | (Latest) |

### Core Dependencies

```xml
<!-- Spring Boot Starters -->
spring-boot-starter-web              <!-- REST APIs -->
spring-boot-starter-data-jpa         <!-- Database ORM -->
spring-boot-starter-security         <!-- Authentication -->
spring-boot-starter-websocket        <!-- Real-time messaging -->
spring-boot-starter-mail             <!-- Email notifications -->
spring-boot-starter-data-redis       <!-- Caching -->
spring-boot-starter-actuator         <!-- Health monitoring -->

<!-- JWT & OAuth2 -->
jjwt-api, jjwt-impl, jjwt-jackson   <!-- JWT token management -->
spring-boot-starter-oauth2-resource-server  <!-- OAuth2 integration -->
google-api-client                    <!-- Google API client -->

<!-- Database -->
flyway-core, flyway-database-postgresql  <!-- Schema migrations -->
postgresql (driver)                  <!-- PostgreSQL JDBC -->

<!-- Documentation -->
springdoc-openapi-starter-webmvc-ui  <!-- Swagger/OpenAPI -->
```

### Architecture Pattern

- **Modular Structure:** Each feature (auth, event, booking, etc.) isolated in separate modules
- **Layered Design:** Controller → Service → Repository → Entity pattern
- **Service-Oriented:** Authentication service, booking service, event service, etc.
- **Database-First:** Flyway migrations drive schema evolution
- **Microservices:** Separate FastAPI service for AI/ML operations

---

## Project Structure

```
eventra-backend/
├── src/main/java/com/eventra/backend/
│   ├── BackendApplication.java                # Spring Boot entry point
│   ├── common/
│   │   ├── exception/                        # Custom exceptions & handlers
│   │   ├── response/                         # API response wrappers
│   │   └── websocket/                        # WebSocket configuration
│   │
│   └── module/                               # Feature modules
│       ├── auth/                             # ✅ COMPLETE
│       │   ├── controller/AuthController.java
│       │   ├── service/{AuthService, AuditService, etc.}
│       │   ├── security/{JwtUtil, AuthPrincipal, SecurityConfig}
│       │   ├── entity/{User, OrganizerProfile, RefreshToken, etc.}
│       │   └── repository/{UserRepository, RefreshTokenRepository, etc.}
│       │
│       ├── event/                            # ✅ COMPLETE
│       │   ├── controller/
│       │   │   ├── EventController.java          # Public event access
│       │   │   ├── OrganizerEventController.java # Organizer management
│       │   │   ├── EventApprovalController.java  # Admin approval workflow
│       │   │   ├── EventScheduleController.java  # Multi-session scheduling
│       │   │   ├── CategoryController.java       # Event categories
│       │   │   ├── VenueController.java          # Venue management
│       │   │   └── BookmarkController.java       # Save favorite events
│       │   ├── service/
│       │   │   ├── EventService.java         # Core event CRUD (14.5KB)
│       │   │   ├── EventApprovalService.java # Admin approval logic
│       │   │   ├── ScheduleService.java      # Event scheduling
│       │   │   ├── BookmarkService.java      # User bookmarks
│       │   │   ├── CategoryService.java      # Category management
│       │   │   └── VenueService.java         # Venue operations
│       │   ├── entity/{Event, EventSession, Venue, Category, Bookmark}
│       │   ├── enums/{EventStatus, EventCategory}
│       │   ├── repository/{EventRepository, VenueRepository, etc.}
│       │   └── valueobject/                  # Domain value objects
│       │
│       ├── booking/                          # ✅ COMPLETE
│       │   ├── controller/
│       │   │   ├── BookingController.java     # Ticket reservation
│       │   │   ├── TicketController.java      # Ticket details
│       │   │   └── RefundController.java      # Refund management
│       │   ├── service/{BookingService, RefundService, etc.}
│       │   ├── gateway/                      # Payment gateway integration
│       │   ├── entity/{Booking, Ticket, Payment, Refund}
│       │   ├── enums/{BookingStatus, RefundStatus}
│       │   └── repository/
│       │
│       ├── gamification/                     # ✅ PARTIAL
│       │   ├── controller/RewardsController.java (4.8KB - implemented)
│       │   ├── service/{RewardsService, AchievementService, etc.}
│       │   ├── entity/{Badge, UserAchievement, Leaderboard}
│       │   ├── enums/{AchievementType}
│       │   └── repository/
│       │
│       ├── notification/                     # ✅ PARTIAL
│       │   ├── controller/NotificationController.java
│       │   │   • GET /api/notifications/my
│       │   │   • PATCH /api/notifications/{id}/read
│       │   │   • PATCH /api/notifications/read-all
│       │   │   • DELETE /api/notifications/{id}
│       │   ├── service/NotificationService.java
│       │   ├── entity/Notification
│       │   └── repository/
│       │
│       ├── messaging/                        # ✅ PARTIAL
│       │   ├── controller/BroadcastController.java
│       │   │   • POST /api/broadcasts (send broadcast)
│       │   │   • GET /api/broadcasts (get user broadcasts)
│       │   ├── service/MessagingService.java
│       │   ├── entity/{DirectMessage, BroadcastMessage}
│       │   └── repository/
│       │
│       ├── community/                        # ✅ PARTIAL
│       │   ├── controller/
│       │   │   ├── CommunityController.java      # Join, list, manage
│       │   │   ├── DiscussionController.java     # Threads & replies
│       │   │   └── AdminCommunityController.java # Moderation
│       │   ├── service/{CommunityService, DiscussionService, ModerationService}
│       │   ├── entity/{Community, Discussion, Reply, Flag}
│       │   ├── enums/{ContentType, FlagStatus}
│       │   └── repository/
│       │
│       ├── wallet/                           # ✅ COMPLETE
│       │   ├── controller/WalletController.java
│       │   ├── service/WalletService.java (handles transactions, payouts)
│       │   ├── entity/{Wallet, WalletTransaction, PayoutRequest, PayoutMethod}
│       │   └── repository/
│       │
│       ├── analytics/                        # ✅ PARTIAL
│       │   ├── controller/
│       │   │   ├── RecommendationController.java  (AI endpoints)
│       │   │   ├── AnalyticsController.java       (dashboard)
│       │   │   └── ProfileController.java         (user stats)
│       │   ├── service/{RecommendationService, AnalyticsService, NLPSearchEngine}
│       │   └── dto/{UserRecommendationRequest, SearchRequest, etc.}
│       │
│       ├── calendar/                         # ⚠️ EMPTY STUB
│       │
│       ├── config/                           # ✅ PARTIAL
│       │   ├── controller/SystemConfigController.java
│       │   └── service/SystemConfigService.java
│       │
│       └── admin/                            # ✅ PARTIAL
│           ├── controller/AdminController.java
│           │   • Audit logs retrieval
│           │   • User suspension/ban/reactivation
│           │   • Organizer approval/rejection
│           │   • Password reset forcing
│           └── service/{AdminService, AuditService}
│
├── src/main/resources/
│   ├── application.properties       # Spring configuration
│   └── db/migration/               # Flyway schema versions
│       ├── V2__init.sql            # Core schema (users, events, basic tables)
│       ├── V3__auth_additions.sql  # OTP, sessions, refresh tokens
│       ├── V4__event_schema.sql    # Events, venues, categories
│       ├── V5__booking_schema.sql  # Bookings, payments, refunds
│       ├── V6__community_and_gamification.sql
│       ├── V7__analytics_schema.sql
│       ├── V8__demo_data_seed.sql
│       ├── V9__config_notifications_audit.sql
│       ├── V10__wallet_and_payouts.sql
│       ├── V11__messaging.sql
│       ├── V12__personal_calendar.sql
│       ├── V13__otp_support.sql
│       └── V14__event_schedule.sql
│
├── src/test/java/com/eventra/backend/
│   ├── BackendApplicationTests.java
│   └── module/
│       ├── community/CommunityControllerTest.java (exists with test coverage)
│       └── {auth,booking,event,gamification,etc.}/ (test directories available)
│
├── ai-service/                     # Python FastAPI microservice (separate process)
│   ├── main.py                     # FastAPI app (port 8001)
│   │   • POST /user-recommendations (user-based)
│   │   • GET /recommend/{event_id} (similar events)
│   │   • POST /search (semantic search)
│   │   • POST /sentiment (sentiment analysis)
│   │   • POST /predict-attendance (attendance forecast)
│   │   • POST /events/sync (event state sync)
│   ├── requirements.txt            # Python dependencies
│   ├── models/                     # Pre-trained ML models
│   │   ├── event_size_model.pkl
│   │   └── category_encoder.pkl
│   ├── ai/
│   │   ├── search.py               # Event search engine
│   │   ├── recommender.py          # Recommendation algorithm
│   │   ├── similarity.py           # Similar event recommendation
│   │   ├── sentiment.py            # Sentiment classification
│   │   └── state.py                # Event state management
│   ├── data/                       # Mock user & event data
│   └── tests/                      # Pytest test suite
│
├── pom.xml                         # Maven build configuration
├── mvnw, mvnw.cmd                 # Maven wrapper scripts
├── .env.example                    # Environment template
├── GIT_WORKFLOW.md                 # Team collaboration guidelines
└── Eventra-M{1,2,3}-*-Tests.postman_collection.json  # API test collections
```

---

## Module Implementation Status

| Module | Status | Features | Controllers | Services |
|--------|--------|----------|-------------|----------|
| **Auth (M1)** | ✅ COMPLETE | Register (attendee/organizer), Login, Email verify, OAuth2, OTP, Password reset, Audit logs | AuthController, AdminController | AuthService, AuditService |
| **Event (M2)** | ✅ COMPLETE | CRUD, Approval workflow, Scheduling, Categories, Venues, Bookmarks, Admin approval | EventController, OrganizerEventController, EventApprovalController, EventScheduleController, CategoryController, VenueController, BookmarkController | EventService, EventApprovalService, ScheduleService, CategoryService, VenueService, BookmarkService |
| **Booking (M3)** | ✅ COMPLETE | Reservations, Refunds, Ticket management, Payment integration | BookingController, TicketController, RefundController | BookingService, RefundService, TicketService |
| **Gamification (M5)** | ✅ PARTIAL | Rewards, Achievements, Leaderboards | RewardsController | RewardsService, AchievementService |
| **Notification** | ✅ PARTIAL | In-app notifications (get, mark read, delete) | NotificationController | NotificationService |
| **Messaging** | ✅ PARTIAL | Broadcast messages | BroadcastController | MessagingService |
| **Community** | ✅ PARTIAL | Discussions, Threads, Moderation, Flagging | CommunityController, DiscussionController, AdminCommunityController | CommunityService, DiscussionService, ModerationService |
| **Wallet** | ✅ COMPLETE | Balance tracking, Transactions, Payouts, Payout methods | WalletController | WalletService |
| **Analytics** | ✅ PARTIAL | Recommendations, Dashboard, Sentiment, Search | RecommendationController, AnalyticsController | RecommendationService, AnalyticsService, NLPSearchEngine |
| **Config** | ✅ PARTIAL | System configuration | SystemConfigController | SystemConfigService |
| **Calendar** | ⚠️ STUB | Empty directory | None | None |

---

## How to Run

### Prerequisites

```bash
# System requirements
- Java 21 JDK
- Maven 3.8+
- PostgreSQL 13+
- Redis 6.0+
- Python 3.8+ (for AI service only)
```

### Backend Setup

```bash
# 1. Clone repository
git clone https://github.com/Haban22/eventra-backend.git
cd eventra-backend

# 2. Configure environment
cp .env.example .env
# Edit .env with:
#   - DB credentials
#   - JWT secret (min 32 chars): openssl rand -base64 32
#   - Google OAuth client ID (optional)
#   - Email SMTP credentials

# 3. Install & migrate
mvn clean install
./mvnw spring-boot:run

# Server starts on http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui.html
# Actuator health: http://localhost:8080/actuator/health
```

### AI Microservice Setup

```bash
# 1. Navigate to AI service
cd ai-service

# 2. Create Python environment
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 3. Install dependencies
pip install -r requirements.txt

# 4. Run service
uvicorn main:app --host 0.0.0.0 --port 8001 --reload

# Service: http://localhost:8001
# Health: http://localhost:8001/health
# Docs: http://localhost:8001/docs
```

### Database Initialization

```bash
# Flyway automatically runs on startup
# Applies migrations from V2 to V14 sequentially
# Connection: jdbc:postgresql://localhost:5432/eventra_db
# User: postgres (configurable in .env)
```

---

## API Endpoints

### Authentication (`/api/auth`)

```
POST   /register/attendee              Register as attendee
POST   /register/organizer             Register as organizer
GET    /verify-email?token=X           Confirm email
POST   /resend-verification            Resend verification email
POST   /login                          Login (returns JWT)
POST   /refresh                        Refresh access token
POST   /logout                         Logout (invalidate token)
POST   /forgot-password                Request password reset
POST   /reset-password                 Reset with token
POST   /google                         Google OAuth2 login
POST   /admin/otp/request              Request admin OTP
POST   /admin/otp/verify               Verify OTP for admin
```

### Events (`/api/events`)

```
POST   /                               Create event (organizer)
GET    /                               List public events
GET    /{id}                           Get event details
PUT    /{id}                           Update event (organizer)
DELETE /{id}                           Delete event (organizer)
POST   /schedule/{eventId}             Add event session
GET    /categories                     List categories
POST   /bookmarks/{eventId}            Save event to bookmarks
GET    /bookmarks/my                   Get bookmarked events
```

### Bookings (`/api/bookings`)

```
POST   /                               Create booking
GET    /my                             Get user's bookings
GET    /{id}                           Get booking details
GET    /{id}/tickets                   Get tickets for booking
POST   /{id}/refund                    Request refund
```

### Wallet (`/api/wallet`)

```
GET    /                               Get wallet balance
POST   /topup                          Add funds to wallet
GET    /transactions                   Transaction history
POST   /payout-methods                 Register payout method
GET    /payouts                        Payout history
```

### Notifications (`/api/notifications`)

```
GET    /my                             Get user notifications
PATCH  /{id}/read                      Mark as read
PATCH  /read-all                       Mark all as read
DELETE /{id}                           Delete notification
```

### Community (`/api/communities`)

```
GET    /                               List communities
POST   /                               Create community (organizer)
POST   /{id}/join                      Join community
POST   /{id}/discussions               Create discussion
GET    /{id}/discussions               List discussions
POST   /{discussionId}/replies         Reply to discussion
```

### Gamification (`/api/rewards`)

```
GET    /my                             Get user rewards
GET    /leaderboard                    Global leaderboard
POST   /claim/{badgeId}                Claim badge reward
```

### Admin (`/api/admin`)

```
GET    /audit-logs                     View audit trail
GET    /users                          List users
GET    /organizers/pending             List pending organizers
PATCH  /organizers/{id}/approve        Approve organizer
PATCH  /organizers/{id}/reject         Reject organizer
PATCH  /users/{id}/suspend             Suspend user
PATCH  /users/{id}/ban                 Ban user
PATCH  /users/{id}/reactivate          Reactivate user
GET    /analytics/overview             Analytics dashboard
GET    /analytics/trend                Trend analysis
GET    /config                         Get system config
PATCH  /config                         Update config (admin only)
```

### AI (`/api/ai`)

```
POST   /recommendations/user           Get user recommendations
GET    /recommendations/events/{id}    Get similar events
POST   /search                         Semantic event search
POST   /sentiment                      Analyze sentiment
POST   /feedback/analyze               Analyze event feedback
POST   /predict-attendance-detailed    Predict attendance
```

---

## Database Schema

### Core Tables (14 Flyway Migrations)

```sql
-- V2__init.sql: Foundation
users (id, email, password_hash, role, status, created_at)
organizer_profiles (id, user_id, company_name, verification_status)

-- V3__auth_additions.sql: Authentication
email_verification_tokens (id, user_id, token, expires_at)
password_reset_tokens (id, user_id, token, expires_at)
refresh_tokens (id, user_id, token, expires_at)
admin_audit_logs (id, admin_user_id, action_type, target_id, created_at)

-- V4__event_schema.sql: Event Management
events (id, organizer_id, title, description, status, capacity, created_at)
event_sessions (id, event_id, start_time, end_time)
categories (id, name)
venues (id, name, location, capacity)

-- V5__booking_schema.sql: Bookings & Payments
bookings (id, user_id, event_id, status, quantity, created_at)
tickets (id, booking_id, ticket_number, check_in_status)
payments (id, booking_id, amount, status, transaction_ref)
refunds (id, booking_id, amount, status, reason)

-- V6__community_and_gamification.sql: Social & Rewards
communities (id, event_id, name, created_at)
discussions (id, community_id, title, content, author_id)
replies (id, discussion_id, content, author_id, created_at)
flags (id, content_id, content_type, reason, status)
badges (id, name, description, icon_url)
user_achievements (id, user_id, badge_id, earned_at)
leaderboard (id, user_id, points, rank)

-- V7__analytics_schema.sql: Metrics
event_metrics (id, event_id, views, bookings, revenue)
user_metrics (id, user_id, bookings, spent, points)

-- V8__demo_data_seed.sql: Test Data
(sample events, users, categories)

-- V9__config_notifications_audit.sql: Config & Alerts
system_config (id, key, value, updated_at)
notifications (id, user_id, type, message, is_read)

-- V10__wallet_and_payouts.sql: Financial
wallets (id, user_id, balance)
wallet_transactions (id, wallet_id, amount, type, created_at)
payout_methods (id, user_id, type, account_details)
payout_requests (id, wallet_id, amount, status)

-- V11__messaging.sql: Messages
direct_messages (id, sender_id, recipient_id, content)
broadcast_messages (id, sender_id, event_id, content, created_at)

-- V12__personal_calendar.sql: User Calendar
user_calendar_events (id, user_id, event_id, added_at)

-- V13__otp_support.sql: 2FA
otp_tokens (id, user_id, code, expires_at)

-- V14__event_schedule.sql: Advanced Scheduling
event_schedule_templates (id, event_id, recurrence_pattern)
```

---

## Testing

### Test Coverage

```bash
# Run all tests
mvn test

# Run specific module tests
mvn test -Dtest=CommunityControllerTest

# With PostgreSQL & Redis (integration tests)
mvn verify

# Run AI service tests
cd ai-service && pytest
```

### Test Collections (Postman)

```
Eventra-M1-Auth-User-Tests.postman_collection.json
  - Register attendee/organizer
  - Email verification
  - Login (JWT)
  - OAuth2 flow
  - OTP admin login
  - Password reset

Eventra-M2-Event-Module-Tests.postman_collection.json
  - Create/list/update events
  - Event approval workflow
  - Categories & venues
  - Bookmarks

Eventra-M3-Booking-Module-Tests.postman_collection.json
  - Create bookings
  - Process payments
  - Refunds
  - Ticket validation
```

### Import into Postman

```bash
# Open Postman → Import → Select .json file
# Configure environment variables:
#   - BASE_URL=http://localhost:8080
#   - JWT_TOKEN=<from login response>
#   - USER_ID=<from register response>
```

---

## Key Features Summary

### ✅ Fully Implemented

1. **Complete Authentication System**
   - JWT token generation & refresh
   - Email verification with tokens
   - OAuth2 integration (Google)
   - OTP-based admin 2FA
   - Bcrypt password hashing

2. **Event Management Suite**
   - Full CRUD for events
   - Event approval workflow (admin)
   - Multi-session scheduling
   - Venue management
   - Category taxonomy
   - Event bookmarking

3. **Booking & Payment**
   - Ticket reservation system
   - Payment gateway integration
   - Refund processing
   - Capacity management

4. **Financial Management**
   - User wallet accounts
   - Transaction tracking
   - Payout method management
   - Payout requests & approvals

5. **Community & Social**
   - Discussion forums per event
   - Reply threads
   - Content moderation & flagging
   - Admin moderation tools

6. **Gamification**
   - Badge system
   - Achievement tracking
   - Leaderboard rankings
   - Reward redemption

7. **Admin Controls**
   - Audit logging for all sensitive actions
   - User suspension/ban/reactivation
   - Organizer approval workflow
   - System configuration management
   - Analytics dashboard

### ✅ Partial (Core logic present, edge cases pending)

- Notification delivery (in-app only; email queue pending)
- Direct messaging (WebSocket groundwork present)
- User profiles (basic implementation)

### ⚠️ Not Yet Implemented

- Calendar module (directory structure only)
- Advanced reporting/analytics UI
- Mobile push notifications
- Event live streaming integration

---

## Environment Configuration (.env)

```bash
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/eventra_db
spring.datasource.username=postgres
spring.datasource.password=your_password

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT
app.jwt-secret=your-secret-at-least-32-characters-long
app.jwt-access-expiry-seconds=86400
app.jwt-refresh-expiry-seconds=604800

# App
app.frontend-url=http://localhost:5173
app.bcrypt-strength=10
app.google-client-id=your-google-oauth-client-id (optional)

# Email
app.mail-from=noreply@eventra.com
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# CORS
app.cors-allowed-origins=http://localhost:5173

# AI Service
eventra.ai.url=http://localhost:8001
```

---

## Tech Decisions & Rationale

| Decision | Rationale |
|----------|-----------|
| **Spring Boot 3.5** | Latest LTS; strong ecosystem for enterprise Java |
| **PostgreSQL** | ACID compliance; complex relational queries needed |
| **Flyway** | Version-controlled schema; safe migrations |
| **JWT Stateless Auth** | Scalable; no session server needed; mobile-friendly |
| **Separate FastAPI Service** | Python ML libraries; independent scaling; language flexibility |
| **Module-per-feature** | Parallel development; independent deployment potential |
| **Layers (Controller→Service→Repo)** | Clean architecture; testability; separation of concerns |

---

## Next Steps & Roadmap

### Immediate (v1.1)
- [ ] Email notification queue (RabbitMQ/Kafka)
- [ ] WebSocket real-time messaging
- [ ] Event live streaming integration
- [ ] API rate limiting
- [ ] Comprehensive test suite expansion

### Short-term (v1.5)
- [ ] Multi-language support
- [ ] Advanced analytics reporting
- [ ] Mobile app API optimizations
- [ ] Payment method diversification
- [ ] Event categorization AI

### Medium-term (v2.0)
- [ ] Multi-region deployment
- [ ] Event recommendation model improvements
- [ ] Advanced gamification (tier progression, challenges)
- [ ] Team collaboration features for organizers
- [ ] API versioning & backward compatibility

---

## Support & Contributing

See `GIT_WORKFLOW.md` for branching strategy and team guidelines.

### Module Ownership

- **M1 - Auth:** Primary module; foundational
- **M2 - Event:** Core feature; enable organizers
- **M3 - Booking:** Revenue stream; payment integration critical
- **M5 - Gamification:** User engagement; retention focus
- **AI Service:** Recommendations; continuous model improvement

---

**Last Updated:** July 6, 2026  
**Status:** Production-Ready (Core Features), Beta (Edge Cases)  
**Maintained By:** Haban22 & Team
