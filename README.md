# 🏋️ AI Fitness Recommendation System

A microservices-based fitness tracking backend built with Spring Boot. Users register and log in, track workouts, and receive AI-generated workout recommendations (powered by Google Gemini) processed asynchronously via RabbitMQ.

---

##  Features

-  **JWT-based authentication built from scratch** — BCrypt password hashing, token issuance on login, and centralized token verification at the API Gateway
-  **Service discovery** with Netflix Eureka — no hardcoded service URLs
-  **Centralized configuration** via Spring Cloud Config Server
-  **API Gateway** (Spring Cloud Gateway) — single entry point for all traffic, routing + JWT verification
-  **Activity tracking** (running, walking, cycling, swimming, weight training, yoga, etc.) with flexible per-activity metrics, stored in MongoDB
-  **AI-powered workout recommendations** — every logged activity is asynchronously analyzed by Google Gemini for performance insights, improvement areas, suggestions, and safety guidelines
-  **Event-driven processing** via RabbitMQ — activity tracking and AI recommendation generation are fully decoupled; logging a workout never waits on the AI call
-  **Polyglot persistence** — MySQL for user data, MongoDB for activities and recommendations

---

##  Architecture

```
                Client (Postman / API client)
                          │
              Authorization: Bearer <JWT>
                          ▼
              ┌─────────────────────────┐
              │      API GATEWAY (8080)   │
              │  Routing + JwtAuthFilter   │
              │  → verifies token,         │
              │    injects X-User-ID       │
              └──────┬──────────┬─────────┘
                     │          │          │
        /api/users/**  /api/activities/**  /api/recommendation/**
                     │          │          │
                     ▼          ▼          ▼
         ┌──────────────┐ ┌───────────────┐ ┌──────────────┐
         │ USER-SERVICE │ │ACTIVITY-SERVICE│ │  AI-SERVICE │
         │   (8081)     │ │    (8082)     │ │   (8083)     │
         │   MySQL      │ │   MongoDB     │ │  MongoDB     │
         └──────────────┘ └───────────────┘ └──────▲───────┘
                                   │ publish       │ consume
                                   ▼               │
                          ┌──────────────────────────────┐
                          │ RabbitMQ: fitness.exchange   │
                          │ queue: activity.queue (durable)
                          └──────────────────────────────┘
                                                              
                                                              


  All services register with Eureka (8761) and pull config from Config Server (8888)
```

**Synchronous calls** (WebClient + Eureka load balancing): Activity Service → User Service, to validate a user exists before saving an activity.
**Asynchronous calls** (RabbitMQ): Activity Service → AI Service, so a workout save never waits on the Gemini API call.

---

##  Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7, Spring Cloud 2025.0.0 |
| Service Discovery | Netflix Eureka |
| Config Management | Spring Cloud Config Server (native profile) |
| API Gateway | Spring Cloud Gateway (WebFlux) |
| Authentication | Custom JWT (JJWT library) + BCrypt — built without Spring Security or an external Identity Provider |
| Messaging | RabbitMQ (Spring AMQP) |
| Databases | MySQL (User Service), MongoDB (Activity & AI Service) |
| AI | Google Gemini API |
| Build Tool | Maven |

---

##  Project Structure

```
fitness_microservice/
├── eureka/                  # Service Registry (port 8761)
├── configServer/            # Centralized config (port 8888)
│   └── src/main/resources/config/   # per-service properties files
├── APIGateway/               # API Gateway + JWT verification (port 8080)
├── userservice/              # Registration, login, profile — MySQL (port 8081)
├── activityservice/          # Activity tracking — MongoDB + RabbitMQ producer (port 8082)
└── AIService/                 # AI recommendations — MongoDB + RabbitMQ consumer + Gemini (port 8083)
```

---


##  Running the Project Locally

Start infrastructure first (MySQL, MongoDB, RabbitMQ), then start services **in this order**:

1. **Eureka Server**
   ```bash
   cd eureka/eureka && ./mvnw spring-boot:run
   ```
   → confirm dashboard at `http://localhost:8761`

2. **Config Server**
   ```bash
   cd configServer/configServer && ./mvnw spring-boot:run
   ```

3. **User Service**, **Activity Service**, **AI Service** (any order)
   ```bash
   cd userservice/userservice && ./mvnw spring-boot:run
   cd activityservice/activityservice && ./mvnw spring-boot:run
   cd AIService/AIService && ./mvnw spring-boot:run
   ```

4. **API Gateway**
   ```bash
   cd APIGateway/APIGateway && ./mvnw spring-boot:run
   ```

Verify all services appear as registered instances on the Eureka dashboard before testing.

---

##  API Reference

All requests go through the **API Gateway** at `http://localhost:8080`. Every endpoint except register/login requires `Authorization: Bearer <JWT>`.

### User Service — `/api/users`
| Method | Endpoint | Auth required | Description |
|---|---|---|---|
| `POST` | `/register` | No | Register a new user (idempotent by email); password is BCrypt-hashed |
| `POST` | `/login` | No | Verify credentials and receive a JWT |
| `GET` | `/{userId}` | Yes | Get a user's profile |
| `GET` | `/{userId}/validate` | Yes | Check whether a user exists (used internally by Activity Service) |

### Activity Service — `/api/activities`
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/` | Track a new activity (user ID taken from the Gateway-verified token, not the request body) |
| `GET` | `/` | List the current user's activities |
| `GET` | `/{activityId}` | Get a single activity by ID |

### AI Service — `/api/recommendation`
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/user/{userId}` | All recommendations for a user |
| `GET` | `/activity/{activityId}` | The AI recommendation for one specific activity |

---
# 🔐 How Authentication Works
 
1. `POST /api/users/login` verifies the submitted password against a BCrypt hash and issues a JWT (HMAC-SHA256, 24-hour expiry) containing the user's ID as the subject claim.
2. Every other request must carry that token in the `Authorization` header.
3. A custom Gateway filter (`JwtAuthFilter`) verifies the token's signature and expiry using the same shared secret, then injects a trusted `X-User-ID` header before forwarding the request downstream.
4. Internal services (Activity, AI) never parse or validate the JWT themselves — they trust the Gateway-injected header. This keeps token logic centralized in one place rather than duplicated across services.
This is a deliberately minimal, self-written authentication scheme — no Spring Security framework or external Identity Provider is used.
