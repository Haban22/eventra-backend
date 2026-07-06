# Eventra Backend

Welcome to the **Eventra Backend**. This project serves as the core backend engine for the Eventra Event Management Platform. It is a modular, high-performance architecture comprising a **Spring Boot** application and a **FastAPI Python AI Microservice**.

---

## 🚀 Key Features

*   **User Management & Auth**: JWT authentication, Google OAuth2, email verification, OTP, and admin security controls.
*   **Event & Venue Management**: Multi-session scheduling, category organization, venue reservations, and admin approval workflows.
*   **Booking & Ticketing**: Seat reservation, ticket generation, and refund processing.
*   **Wallet & Financials**: Transactions, wallet balances, and organizer payout requests.
*   **Community & Discussions**: Discussion threads, message boards, direct messaging, and content moderation/flagging.
*   **Gamification**: Reward systems, user achievements, and badges.
*   **Real-Time Notifications**: WebSocket-based in-app notifications and email alerts.
*   **AI Microservice**: Content-based recommendations, attendee sentiment analysis, and search matching.

---

## 🛠️ System Requirements

Before running the application, make sure you have the following installed:

*   **Java 21 LTS**
*   **PostgreSQL 13+** (running locally or remotely)
*   **Redis 6.0+** (used for caching and OTP)
*   **Python 3.8+** (for the AI microservice)
*   **Maven 3.8+** (a wrapper `mvnw` is included in this repository)

---

## ⚙️ Configuration & Setup

### 1. Database Setup
Create a PostgreSQL database named `eventra_db`:
```sql
CREATE DATABASE eventra_db;
```

### 2. Environment Variables
1. Navigate to the `backend/` directory.
2. Copy the template file `.env.example` and rename it to `.env`:
   ```bash
   cp .env.example .env
   ```
3. Open `.env` and fill in your credentials (database username/password, Redis host, JWT secret, and email credentials).

---

## 🏃 Running the Services

For a fully functional platform, start the services in the following order:

### 1. Main Backend (Spring Boot)
From the `backend/` directory, run the appropriate command for your OS:

*   **Windows (Command Prompt / PowerShell)**:
    ```bash
    mvnw.cmd spring-boot:run
    ```
*   **Linux / macOS**:
    ```bash
    chmod +x mvnw
    ./mvnw spring-boot:run
    ```
*   **Runs on**: [http://localhost:8080](http://localhost:8080)
*   **Swagger API Docs**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

### 2. AI Microservice (FastAPI + Python)
From the `backend/ai-service/` directory, set up your Python virtual environment and run the service:

1.  **Create & activate virtual environment**:
    *   **Windows**:
        ```bash
        python -m venv venv
        venv\Scripts\activate
        ```
    *   **Linux / macOS**:
        ```bash
        python3 -m venv venv
        source venv/bin/activate
        ```
2.  **Install dependencies**:
    ```bash
    pip install -r requirements.txt
    ```
3.  **Run the service**:
    ```bash
    uvicorn main:app --reload
    ```
*   **Runs on**: [http://localhost:8000](http://localhost:8000)
*   **Swagger API Docs**: [http://localhost:8000/docs](http://localhost:8000/docs)

---

## 🧪 Testing

*   **Spring Boot Tests**: Run `./mvnw test` to execute JUnit tests.
*   **FastAPI Tests**: Activate the python virtual environment and run `pytest`.
