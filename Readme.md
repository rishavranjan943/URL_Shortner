# URL Shortener 🚀

A production-ready **URL Shortener** built using **Spring Boot**, **PostgreSQL**, and **Redis** with caching, rate limiting, Docker support, CI/CD, monitoring, and load testing.

> Designed as a scalable backend project demonstrating REST API design, caching strategies, deployment, monitoring, and performance optimization.

---

## 🌐 Live Demo

**Application**

https://url-shortener-latest-ulj8.onrender.com

---

## ✨ Features

* 🔗 Generate short URLs from long URLs
* ↩️ HTTP 302 redirection to the original URL
* 📊 Stats for every shortened URL
* ⚡ Redis cache (Cache-Aside Pattern) for faster redirects
* 🚦 Redis Token Bucket Rate Limiter
* 🐳 Docker support
* 🔄 CI/CD using GitHub Actions
* ❤️ Spring Boot Actuator health monitoring
* 📈 UptimeRobot monitoring
* 🧪 Load testing using k6
* ☁️ Cloud deployment on Render
* 🗄️ PostgreSQL persistent storage

---

## 🏗️ System Architecture

```text
                   ┌──────────────────────┐
                   │       Client         │
                   └──────────┬───────────┘
                              │
                              ▼
                 ┌─────────────────────────┐
                 │ Spring Boot Application │
                 │        (Render)         │
                 └──────────┬──────────────┘
                            │
             ┌──────────────┴──────────────┐
             │                             │
             ▼                             ▼
      Redis Cache                  PostgreSQL Database
       (Upstash)                        (Render)
             │                             │
             └──────────────┬──────────────┘
                            ▼
                    Redirect Response
```

---

## ⚙️ Tech Stack

| Category         | Technology                         |
| ---------------- | ---------------------------------- |
| Language         | Java 17                            |
| Framework        | Spring Boot                        |
| Build Tool       | Maven                              |
| Database         | PostgreSQL                         |
| Cache            | Upstash Redis                      |
| Deployment       | Render                             |
| Monitoring       | Spring Boot Actuator + UptimeRobot |
| Load Testing     | k6                                 |
| Containerization | Docker                             |
| CI/CD            | GitHub Actions                     |

---

## 📂 Project Structure

```text
URLShortener/
│
├── src/java/com/example/URLShortner
│   ├── config/
│   ├── controller/
│   ├── dto/
│   ├── entity/
│   ├── exception/
│   ├── repository/
│   ├── services/
│   ├── utils/
│   └── exception/
│
├── load-tests/
│   ├── scripts/
│   ├── outputs/
│   ├── README.md
│   └── summary.md
│
├── docs/
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🚀 API Overview

### Create Short URL

```http
POST /shorten
```

**Request**

```json
{
  "longUrl": "https://example.com"
}
```

**Response**

```json
{
  "shortCode": "QM7eGB"
}
```

---

### Redirect

```http
GET /{shortCode}
```

Returns an HTTP **302 Found** response and redirects the client to the original URL.

---

### URL Statistics

```http
GET /stats/{shortCode}
```

Returns analytics such as click count and original URL.

---

## ⚡ Caching Strategy

The application uses the **Cache-Aside Pattern** with Redis.

### Redirect Flow

```text
Client
   │
   ▼
Redis Cache
   │
   ├── Cache Hit
   │      │
   │      ▼
   │  Return Long URL
   │
   └── Cache Miss
          │
          ▼
   PostgreSQL
          │
          ▼
   Update Redis
          │
          ▼
      Redirect User
```

### Benefits

* Reduces database load
* Improves redirect latency
* Handles high read traffic efficiently
* Scales better under concurrent requests

---

## 🚦 Rate Limiting

The application uses a **Redis Token Bucket** algorithm to protect the API against excessive traffic.

### Flow

```text
Incoming Request
        │
        ▼
Redis Token Bucket
        │
   ┌────┴─────┐
   │          │
Allowed    Rejected
   │          │
   ▼          ▼
 Process    HTTP 429
 Request   Too Many Requests
```

### Benefits

* Protects the backend from abuse
* Prevents denial-of-service through excessive requests
* Uses Redis for distributed rate limiting across multiple application instances

---

## 🐳 Running Locally

### Clone Repository

```bash
git clone <repository-url>
cd URLShortener
```

### Start Dependencies

```bash
docker compose up -d
```

### Build

```bash
mvn clean install
```

### Run Application

```bash
mvn spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

---

## ☁️ Deployment

### Backend

* Render

### Database

* PostgreSQL (Render)

### Cache

* Upstash Redis

### Monitoring

* Spring Boot Actuator
* UptimeRobot

---

## 🔧 Environment Variables

The application requires the following configuration values.

### Local Development

Update the following properties in src/main/resources/application.properties:

#### PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/urlshortner
spring.datasource.username=<your-postgres-username>
spring.datasource.password=<your-postgres-password>

#### Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

#### JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

#### Spring Boot Actuator
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=always
management.info.env.enabled=true
Production (Render)

### Render Deployment

Configure the following environment variables in your deployment platform.

#### PostgreSQL 
SPRING_DATASOURCE_URL=jdbc:postgresql://<render-postgres-host>:5432/<database-name> SPRING_DATASOURCE_USERNAME=<postgres-username> SPRING_DATASOURCE_PASSWORD=<postgres-password> 
#### Redis (Upstash) 
SPRING_DATA_REDIS_HOST=<upstash-redis-host> 
SPRING_DATA_REDIS_PORT=6379 
SPRING_DATA_REDIS_PASSWORD=<upstash-redis-password> 
SPRING_DATA_REDIS_SSL_ENABLED=true 
#### Cache 
SPRING_CACHE_TYPE=redis 
SPRING_CACHE_REDIS_TIME_TO_LIVE=3600000 
SPRING_CACHE_REDIS_KEY_PREFIX=urlshortener: 
#### JPA 
SPRING_JPA_HIBERNATE_DDL_AUTO=update 
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect


---


# 📊 Load Testing

Performance testing was performed using **k6** to evaluate application throughput, latency, and stability under concurrent load.

## Test Suite

| Test               | Purpose                                              |
| ------------------ | ---------------------------------------------------- |
| Smoke Test         | Verify application availability                      |
| Redirect Load Test | Measure redirect endpoint performance                |
| Shorten Load Test  | Measure URL creation performance                     |
| Stats Load Test    | Measure analytics endpoint performance               |
| Mixed Load Test    | Simulate production traffic                          |
| Ramp-up Test       | Observe performance under gradually increasing users |
| Stress Test        | Determine maximum supported load                     |
| Spike Test         | Evaluate behavior during sudden traffic bursts       |
| Soak Test          | Verify long-running stability                        |
| Rate Limit Test    | Validate Redis token bucket implementation           |

All scripts and outputs are available in the `load-tests` directory.

---

# ⚡ Performance Optimization

The application was optimized using **Redis** as a distributed cache.

## Before Redis

* Every redirect queried PostgreSQL.
* Average redirect latency was approximately **1.26 seconds** under load.

## After Redis

* Frequently accessed URLs are served directly from Redis.
* Database queries are significantly reduced.
* Redirect latency improved to approximately **500 ms** under similar load.

This demonstrates the effectiveness of the Cache-Aside strategy for read-heavy workloads.

---

# 📈 Performance Results

| Metric                   | Before Redis |           After Redis |
| ------------------------ | -----------: | --------------------: |
| Average Redirect Latency |      ~1.26 s |               ~500 ms |
| Database Reads           |         High | Significantly Reduced |
| Cache Hits               |         None |                  High |
| User Experience          |     Moderate |              Improved |

> Actual benchmark outputs are available in the `load-tests/outputs` directory.

---

# ❤️ Monitoring

Application health is continuously monitored using Spring Boot Actuator and UptimeRobot.

### Spring Boot Actuator

Provides endpoints such as:

```text
/actuator/health

/actuator/info

/actuator/metrics
```

### UptimeRobot

The deployed application is monitored for:

* Availability
* Downtime
* Health checks
* Response time

---

# 🔄 CI/CD Pipeline

The project uses **GitHub Actions** for Continuous Integration.

Pipeline steps:

```text
Developer Push

↓

GitHub Actions

↓

Checkout Repository

↓

Set up Java

↓

Build using Maven

↓

Execute Tests

↓

Build Docker Image

↓

Deploy to Render
```

Benefits:

* Automated builds
* Faster deployments
* Consistent build process
* Reduced manual effort

---

# 🧪 Testing

The application has been tested using multiple approaches.

### Unit Testing

Business logic validation.

### Integration Testing

Database and Redis interaction.

### Manual API Testing

Using Postman and cURL.

### Load Testing

Using k6.

---


# ⚖️ Design Decisions

## Why PostgreSQL?

* ACID compliant
* Reliable persistent storage
* Excellent relational support

---

## Why Redis?

* Extremely low latency
* High throughput
* Ideal for frequently accessed URLs

---

## Why Base62 Encoding?

* URL friendly
* Compact identifiers
* Efficient representation

---

## Why Spring Boot?

* Rapid development
* Production-ready ecosystem
* Excellent community support

---

## Why Render?

* Easy deployment
* Free hosting tier
* Automatic HTTPS
* Simple CI/CD integration

---

## Why Upstash Redis?

* Fully managed Redis
* Serverless architecture
* Easy cloud integration

---

# 🚀 Future Improvements

Potential enhancements include:

* Custom aliases
* QR code generation
* User authentication
* Custom domains
* Click analytics dashboard
* Kafka event streaming
* Prometheus metrics
* Grafana dashboards
* OpenTelemetry tracing
* Bloom Filter for invalid URL detection
* Multi-region Redis deployment
* CDN integration

---

# 📚 Lessons Learned

During development, this project provided hands-on experience with:

* REST API design
* Layered architecture
* Redis caching strategies
* Distributed rate limiting
* PostgreSQL optimization
* Docker containerization
* Cloud deployment
* Monitoring and observability
* Performance optimization
* Load testing using k6
* CI/CD automation

---

# 📄 License

This project is intended for educational and portfolio purposes.

Feel free to fork the repository, experiment with it, and extend its functionality.

---

# 👨‍💻 Author

**Rishav Ranjan**

Software Engineer | Java | Spring Boot | Backend Development

If you found this project helpful, consider giving it a ⭐ on GitHub.
