# System Architecture

This document describes the architecture, request flow, caching strategy, and design decisions for the URL Shortener application.

---

# Overview

The application follows a layered architecture based on Spring Boot best practices.

Each layer has a single responsibility:

* **Controller** – Handles HTTP requests and responses.
* **Service** – Contains business logic.
* **Repository** – Performs database operations.
* **Redis** – Caches frequently accessed URLs and implements distributed rate limiting.
* **PostgreSQL** – Stores persistent URL data.

The application is deployed on **Render**, uses **PostgreSQL** for persistence, **Upstash Redis** for caching and rate limiting, and is monitored using **Spring Boot Actuator** and **UptimeRobot**.

---

# High-Level Architecture


The overall architecture consists of:

```text
Client
    │
    ▼
Spring Boot Application (Render)
    │
 ┌──┴──────────────┐
 │                 │
 ▼                 ▼
Redis Cache     PostgreSQL
(Upstash)        (Render)
```

---

# Layered Architecture

```text
Client
   │
   ▼
Controller
   │
   ▼
Service
   │
   ▼
Repository
   │
   ▼
PostgreSQL
```

### Controller Layer

Responsibilities:

* Accept HTTP requests
* Validate input
* Return HTTP responses
* Handle redirects

---

### Service Layer

Responsibilities:

* Generate short URLs
* Perform business logic
* Cache frequently accessed URLs
* Increment click counts
* Retrieve analytics

---

### Repository Layer

Responsibilities:

* Persist URL mappings
* Retrieve original URLs
* Update click counts
* Fetch statistics

---

# URL Shortening Flow


The URL shortening process follows these steps:

1. Client sends a `POST /shorten` request.
2. Controller validates the request.
3. Service generates a unique Base62 short code.
4. URL mapping is stored in PostgreSQL.
5. Short code is returned to the client.

```text
Client
    │
POST /shorten
    │
Controller
    │
Service
    │
Generate Base62 Code
    │
Save in PostgreSQL
    │
Return Short Code
```

---

# URL Redirect Flow


Redirect requests are optimized using Redis.

Flow:

1. Client requests the short URL.
2. Redis is checked first.
3. If present, the long URL is returned immediately.
4. If absent, PostgreSQL is queried.
5. Redis cache is updated.
6. HTTP 302 redirect is returned.

```text
Client
    │
GET /{code}
    │
Controller
    │
Redis Cache
    │
Cache Hit?
 │───────────────│
 │               │
Yes             No
 │               │
 ▼               ▼
Redirect     PostgreSQL
                  │
                  ▼
           Update Redis
                  │
                  ▼
              Redirect
```

---

# Cache Strategy


The application uses the **Cache-Aside Pattern**.

Workflow:

1. Look up Redis.
2. If data exists, return it.
3. Otherwise fetch from PostgreSQL.
4. Store result in Redis.
5. Return response.

Benefits:

* Reduced database traffic
* Faster redirects
* Improved scalability
* Lower response latency

---

# Database Design

The application stores URL mappings in PostgreSQL.

Each record contains:

| Field      | Description           |
| ---------- | --------------------- |
| id         | Primary Key           |
| shortCode  | Generated Base62 code |
| longUrl    | Original URL          |
| clickCount | Number of redirects   |
| createdAt  | Creation timestamp    |

Indexes on `shortCode` enable fast lookups during redirects.

---

# Redis Usage

Redis is used for two purposes:

## URL Caching

Frequently accessed URLs are cached to reduce database queries.

Benefits:

* Lower latency
* Reduced PostgreSQL load
* Better performance under concurrent traffic

---

## Distributed Rate Limiting

Redis implements a Token Bucket algorithm.

Workflow:

```text
Incoming Request
        │
        ▼
Redis Token Bucket
        │
Token Available?
   │────────────│
   │            │
 Yes           No
   │            │
   ▼            ▼
Process      HTTP 429
Request      Too Many Requests
```

Benefits:

* Prevents abuse
* Protects backend resources
* Works across multiple application instances

---

# Deployment Architecture


```text
Internet
     │
     ▼
Render
(Spring Boot)
     │
 ┌───┴────────┐
 │            │
 ▼            ▼
Upstash    PostgreSQL
 Redis       Render
```

Monitoring:

* Spring Boot Actuator
* UptimeRobot

---

# Performance Optimization

Initially, every redirect queried PostgreSQL directly.

After integrating Redis:

* Frequently accessed URLs are served from cache.
* Database reads are reduced.
* Redirect latency improved significantly (approximately **1.26 s → 500 ms** under your benchmark).

---

# Design Decisions

## Why Spring Boot?

* Mature ecosystem
* Dependency Injection
* Production-ready features
* Easy REST API development

---

## Why PostgreSQL?

* ACID compliance
* Reliable persistence
* Excellent relational database support

---

## Why Redis?

* Extremely low latency
* High throughput
* Ideal for caching
* Supports distributed rate limiting

---

## Why Base62 Encoding?

* URL-safe characters
* Compact identifiers
* Easy to generate and share

---

## Why Render?

* Simple cloud deployment
* Automatic HTTPS
* Easy integration with GitHub

---

## Why Upstash Redis?

* Managed Redis service
* Serverless
* Minimal operational overhead

---

# Scalability Considerations

The current architecture can be extended by:

* Running multiple Spring Boot instances behind a load balancer.
* Sharing Redis for distributed caching and rate limiting.
* Using PostgreSQL read replicas for analytics workloads.
* Introducing asynchronous processing (e.g., Kafka) for click analytics.
* Adding Prometheus and Grafana for advanced monitoring.

---

# Summary

The URL Shortener follows a clean layered architecture with separated responsibilities.

Key architectural characteristics:

* RESTful API design
* Layered architecture
* PostgreSQL persistence
* Redis Cache-Aside strategy
* Redis Token Bucket rate limiting
* Docker support
* CI/CD using GitHub Actions
* Cloud deployment on Render
* Health monitoring with Spring Boot Actuator and UptimeRobot
* Performance validation using k6 load testing

This architecture provides a maintainable, scalable, and production-ready foundation while keeping the implementation simple enough for an SDE-1 backend project.
