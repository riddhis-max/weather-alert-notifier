# Weather Alert Notifier

A **real-time weather alert system** that notifies users when **extreme weather** (HEAT > 30°C or RAIN > 5mm) occurs in their city.

Built with **Spring Boot**, **Open-Meteo API**, **Docker**, and **Thymeleaf Admin UI**.

---

## Features

| Feature | Description |
|-------|-----------|
| **Subscriber API** | `POST /api/subscribe` with email + city |
| **Validation** | `@Email`, `@NotBlank`, unique email |
| **Weather Check** | Open-Meteo API (cached) |
| **Alert Logic** | HEAT > 30°C, RAIN > 5mm |
| **Scheduler** | Quartz (30min + manual trigger) |
| **Admin Dashboard** | List, delete, trigger alerts |
| **Docker** | Multi-stage build + health check |
| **Persistence** | H2 file-based DB

## Tech Stack

| Technology | Version |
|----------|--------|
| **Spring Boot** | 3.3.13 |
| **Java** | 17 |
| **Database** | H2 (persistent) |
| **API** | Open-Meteo |
| **UI** | Thymeleaf |
| **Build** | Maven |
| **Testing** | JUnit 5, MockMvc |
| **Container** | Docker |

## Quick Start

### 1. Clone & Run

```bash
git clone https://github.com/riddhis-max/weather-alert-notifier.git
cd weather-alert-notifier
mvn spring-boot:run
```

### 2. Subscribe Example:
```bash
curl -X POST http://localhost:8080/api/subscribe \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "city": "Berlin"
  }'
```
Response:
```bash
"Subscribed successfully!"
```

3. Admin UI
Open: http://localhost:8080/admin

- View subscribers
- Delete
- Click "Trigger Alert Check Now"

4. Docker
```bash
docker build --platform linux/amd64 -t weather-alert-notifier .
docker run -p 8080:8080 weather-alert-notifier
```
→ Access: http://localhost:8080

5. Development
Run Tests
```bash
mvn test
```
H2 Console
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:file:./data/weatherdb

6. Project Structure
```bash
   src/
├── main/
│   ├── java/com/weatheralert/
│   │   ├── client/          # Open-Meteo API
│   │   ├── controller/      # REST + Admin UI
│   │   ├── entity/          # Subscriber
│   │   ├── scheduler/       # Quartz job
│   │   └── service/         # Weather + Email
│   └── resources/
│       ├── templates/       # admin.html
│       └── application.yml
├── test/                    # 7 P2P/F2P tests
├── Dockerfile
└── pom.xml
```
