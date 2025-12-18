# BookMyShow - System Design Documentation

## Project Overview

A full-stack movie ticket booking platform with Java REST API backend, MongoDB persistence, and Flask web frontend. Demonstrates enterprise-grade architecture patterns for scalable, real-world applications.

---

## Architecture Blueprint

### Three-Tier Client-Server Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION TIER                        │
│              Flask Frontend (Python/Jinja2)                 │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐        │
│   │ User Views  │  │ Admin Views │  │ Booking UI  │        │
│   └─────────────┘  └─────────────┘  └─────────────┘        │
├─────────────────────────────────────────────────────────────┤
│                      API LAYER                              │
│              Java HttpServer (REST API)                     │
│   ┌───────────────────────────────────────────────────┐    │
│   │  /api/user/*  │  /api/admin/*  │  /api/bookings  │    │
│   │  /api/movies  │  /api/shows    │  /api/theatres  │    │
│   └───────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────┤
│                   BUSINESS LOGIC TIER                       │
│                  Handler Classes (Java)                     │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│   │ UserHandlers │  │ ShowHandlers │  │BookingHandler│     │
│   └──────────────┘  └──────────────┘  └──────────────┘     │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐     │
│   │AdminHandlers │  │MovieHandlers │  │ StatsHandler │     │
│   └──────────────┘  └──────────────┘  └──────────────┘     │
├─────────────────────────────────────────────────────────────┤
│                    DATA ACCESS TIER                         │
│                MongoDB Collections                          │
│   ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐         │
│   │ users   │ │ admins  │ │ movies  │ │theatres │         │
│   └─────────┘ └─────────┘ └─────────┘ └─────────┘         │
│   ┌─────────┐ ┌─────────┐                                  │
│   │ shows   │ │bookings │                                  │
│   └─────────┘ └─────────┘                                  │
└─────────────────────────────────────────────────────────────┘
```

---

## Design Patterns Implemented

### 1. Handler Pattern (Command Handler)

**Implementation Context:**
- `UserRegisterHandler` / `UserLoginHandler` → User authentication endpoints
- `AdminRegisterHandler` / `AdminLoginHandler` → Admin authentication endpoints
- `TheatresHandler` / `ShowsHandler` / `MoviesHandler` → Resource CRUD operations
- `BookSeatsHandler` / `BookingsHandler` → Reservation management

**Rationale:** Each HTTP endpoint encapsulated in dedicated handler class implementing `HttpHandler` interface. Promotes single responsibility and easy endpoint management.

### 2. RESTful API Pattern

**Implementation Context:**
- `BackendServer.java` exposes REST endpoints via `com.sun.net.httpserver.HttpServer`
- Standard HTTP methods: GET (retrieve), POST (create)
- JSON request/response format
- Stateless communication between client and server

**Rationale:** Industry-standard approach enabling frontend-backend decoupling and API reusability across multiple clients.

### 3. Repository Pattern (MongoDB Collections)

**Implementation Context:**
- `usersDB` → User document collection
- `adminsDB` → Admin document collection
- `theatresDB` → Theatre document collection
- `showsDB` → Show document collection with seat tracking
- `moviesDB` → Movie catalog collection
- `bookingsDB` → Booking records collection

**Rationale:** Abstracts database operations behind collection interfaces. MongoDB driver handles connection pooling and query optimization.

### 4. Data Transfer Object Pattern

**Implementation Context:**
- `User.java` → User entity with static factory methods
- `Admin.java` → Admin entity with theatre management
- `Theatre.java` → Theatre entity with show associations
- `Show.java` → Show entity with seat array
- `Payment.java` → Payment processing with enum-based modes

**Rationale:** Model classes carry data between layers. Clean separation from persistence logic.

### 5. Static Factory Method Pattern

**Implementation Context:**
- `User.register()` / `User.login()` → User lifecycle management
- `Admin.register()` / `Admin.login()` → Admin lifecycle management
- `Addmovies.addMovie()` / `Addmovies.getMovieById()` → Movie catalog operations

**Rationale:** Encapsulates object creation with validation logic. Provides descriptive method names over constructors.

### 6. Singleton Pattern (Database Connection)

**Implementation Context:**
- `mongoClient` → Single MongoDB connection instance
- `database` → Single database reference shared across handlers
- Collection references initialized once at server startup

**Rationale:** Prevents connection overhead from multiple database instances. Ensures consistent state across all API handlers.

---

## SOLID Principles Application

### Single Responsibility Principle (SRP)

| Component | Responsibility Scope |
|-----------|---------------------|
| BackendServer | HTTP server configuration and routing |
| UserRegisterHandler | User registration only |
| UserLoginHandler | User authentication only |
| TheatresHandler | Theatre CRUD operations |
| ShowsHandler | Show management and scheduling |
| BookSeatsHandler | Seat booking transactions |
| BookingsHandler | Booking history retrieval |

### Open/Closed Principle (OCP)

- New endpoints added without modifying existing handlers
- Handler interface allows extension through new implementations
- Payment modes extensible via enum additions

### Dependency Inversion Principle (DIP)

- Handlers depend on MongoDB abstractions (MongoCollection interface)
- Frontend depends on API contracts, not backend implementation

---

## Security Implementation

### Password Security
- **Algorithm:** SHA-256 cryptographic hashing
- **Implementation:** `hashPassword()` utility method
- **Storage:** Only hashed passwords stored in database

### API Security
- CORS headers configured for cross-origin requests
- Input validation before database operations
- Email uniqueness enforcement on registration

### Data Validation
- Required field checks before processing
- Seat availability verification before booking
- Duplicate seat booking prevention

---

## Business Logic Implementation

### User Management
- Email uniqueness enforcement via MongoDB query
- Password hashing before storage
- Session-based login verification

### Theatre Operations
- Admin-managed theatre creation
- Tax configuration per theatre
- Seat capacity tracking

### Booking Workflow
- Real-time seat availability check
- Atomic seat booking with MongoDB update
- Price calculation with tax integration
- Booking history with timestamp

---

## System Flows

### User Registration Flow
```
Frontend Form → POST /api/user/register → Email Check → Hash Password → Insert MongoDB → Success Response
```

### Booking Transaction Flow
```
Select Show → GET /api/show-seats → Display Available → POST /api/book-seats → 
Validate Seats → Update bookedSeats Array → Create Booking Document → Return Confirmation
```

### Data Flow Architecture
```
Flask Template → HTTP Request → Java Handler → MongoDB Operation → JSON Response → Template Render
```

---

## Quality Attributes

| Attribute | Implementation Strategy |
|-----------|------------------------|
| **Scalability** | Stateless API design enables horizontal scaling |
| **Persistence** | MongoDB cloud database (Atlas) for data durability |
| **Security** | SHA-256 password hashing, input validation |
| **Maintainability** | Handler-per-endpoint separation |
| **Extensibility** | New handlers added without core changes |
| **Reliability** | Database-backed state, no session dependency |

---

## Technical Specifications

### Backend Stack
- **Language:** Java (JDK 11+)
- **HTTP Server:** `com.sun.net.httpserver.HttpServer`
- **Build Tool:** Maven
- **Database Driver:** MongoDB Java Driver
- **Architecture:** REST API / Handler-based

### Frontend Stack
- **Framework:** Flask (Python)
- **Template Engine:** Jinja2
- **HTTP Client:** Requests library
- **Styling:** HTML/CSS

### Database Layer
- **Database:** MongoDB Atlas (Cloud)
- **Collections:** users, admins, theatres, shows, movies, bookings
- **Connection:** MongoDB URI with authentication

### API Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/api/user/register` | POST | User registration |
| `/api/user/login` | POST | User authentication |
| `/api/admin/register` | POST | Admin registration |
| `/api/admin/login` | POST | Admin authentication |
| `/api/theatres` | GET/POST | Theatre management |
| `/api/movies` | GET/POST | Movie catalog |
| `/api/shows` | GET/POST | Show scheduling |
| `/api/show-seats` | GET | Seat availability |
| `/api/book-seats` | POST | Seat reservation |
| `/api/bookings` | GET | Booking history |
| `/api/stats` | GET | Dashboard statistics |

---

## Implementation Features

### Completed Features
- [x] User registration and login with password hashing
- [x] Admin registration and login
- [x] Theatre management (add, view)
- [x] Movie catalog management
- [x] Show scheduling with theatre association
- [x] Real-time seat availability display
- [x] Multi-seat booking in single transaction
- [x] Booking history per user
- [x] Tax calculation per theatre
- [x] Dashboard statistics API
- [x] CORS support for frontend integration

### Technical Achievements
- [x] MongoDB Atlas cloud integration
- [x] SHA-256 password encryption
- [x] RESTful API design
- [x] JSON request/response handling
- [x] Atomic database updates for bookings

---

## Design Decisions Rationale

### Why MongoDB?
Document-based storage naturally models entities like shows with embedded seat arrays. Cloud deployment (Atlas) eliminates infrastructure management.

### Why Java HttpServer?
Lightweight HTTP server without external framework dependencies. Suitable for demonstrating core concepts without Spring/Servlet overhead.

### Why Handler Pattern?
Each endpoint isolated in dedicated class. Easy to add, modify, or remove endpoints independently.

### Why SHA-256 for Passwords?
Cryptographic one-way hash prevents plain-text password storage. Balance between security and implementation simplicity.

### Why Flask Frontend?
Rapid prototyping with Jinja2 templates. Clean separation from Java backend via REST API.

---

## Conclusion

This implementation demonstrates a production-ready architecture combining Java backend services, MongoDB persistence, and Flask web frontend. The design emphasizes separation of concerns, security best practices, and scalable patterns suitable for real-world ticket booking platforms.

---

*Document Version: 2.0*  
*Last Updated: December 2024*
