# API Documentation

This document describes all REST endpoints exposed by the URL Shortener application.

**Base URL (Production)**

```
https://url-shortener-latest-ulj8.onrender.com
```

**Base URL (Local)**

```
http://localhost:8080
```

---

# API Overview

| Method | Endpoint        | Description                       |
| ------ | --------------- | --------------------------------- |
| POST   | `/shorten`      | Creates a short URL               |
| GET    | `/{shortCode}`  | Redirects to the original URL     |
| GET    | `/stats/{code}` | Returns analytics for a short URL |

---

# 1. Create Short URL

## Endpoint

```http
POST /shorten
```

## Description

Creates a shortened URL for the provided long URL.

---

## Request Headers

```http
Content-Type: application/json
```

---

## Request Body

```json
{
    "longUrl":"https://www.example.com"
}
```

---

## Successful Response

**Status Code**

```
200 OK
```

Response

```json
{
    "shortCode":"QM7eGB"
}
```

---

## Validation

The following validations are performed:

* Request body must not be empty.
* URL must be a valid HTTP or HTTPS URL.
* Invalid requests return an appropriate validation error.

---

## Example

### cURL

```bash
curl -X POST \
-H "Content-Type: application/json" \
-d "{\"longUrl\":\"https://www.google.com\"}" \
http://localhost:8080/shorten
```

---

## Postman

Method

```
POST
```

URL

```
http://localhost:8080/shorten
```

Body

```json
{
    "longUrl":"https://www.google.com"
}
```

---

# 2. Redirect URL

## Endpoint

```http
GET /{shortCode}
```

Example

```http
GET /QM7eGB
```

---

## Description

Redirects the client to the original long URL.

Whenever a redirect occurs:

* Original URL is retrieved
* Click count is incremented
* HTTP 302 response is returned

---

## Successful Response

```
302 Found
```

Header

```http
Location: https://www.google.com
```

---

## Example

Request

```http
GET /QM7eGB
```

Response

```http
HTTP/1.1 302 Found

Location: https://www.google.com
```

---

## Possible Errors

### URL Not Found

```
404 Not Found
```

Example

```json
{
    "message":"Short URL not found"
}
```

---

# 3. URL Statistics

## Endpoint

```http
GET /stats/{code}
```

Example

```http
GET /stats/QM7eGB
```

---

## Description

Returns analytics associated with the specified short URL.

The response includes:

* Original URL
* Short Code
* Click Count

---

## Successful Response

```json
{
    "shortCode":"QM7eGB",
    "longUrl":"https://www.google.com",
    "clickCount":125
}
```

---

## Status Codes

```
200 OK
```

```
404 Not Found
```

---

# HTTP Status Codes

| Status Code | Description                             |
| ----------- | --------------------------------------- |
| 200         | Request processed successfully          |
| 302         | Redirect to original URL                |
| 400         | Invalid request payload                 |
| 404         | URL not found                           |
| 429         | Too Many Requests (Rate Limit Exceeded) |
| 500         | Internal Server Error                   |

---

# Request Flow

## URL Shortening

```
Client
   │
   ▼
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

## URL Redirect

```
Client
   │
GET /{code}
   │
Controller
   │
Redis Cache
   │
Cache Hit?
   │
 ┌───────┴────────┐
 │                │
Yes              No
 │                │
 ▼                ▼
Redirect     PostgreSQL
                  │
                  ▼
            Update Redis
                  │
                  ▼
             Redirect User
```

---

# Rate Limiting

The application protects its endpoints using a Redis-based Token Bucket Rate Limiter.

If the request limit is exceeded:

```
HTTP 429
```

Example

```json
{
    "message":"Too Many Requests"
}
```

---

# Monitoring

The application exposes Spring Boot Actuator endpoints.

Health

```
GET /actuator/health
```

Application Information

```
GET /actuator/info
```

Metrics

```
GET /actuator/metrics
```

---

# Notes

* All APIs exchange data in JSON format (except redirects).
* Redirect responses return **HTTP 302 Found** with the `Location` header.
* Frequently accessed URLs are served from Redis to reduce database load and improve response time.
* Production monitoring is performed using Spring Boot Actuator and UptimeRobot.
* Performance testing for these APIs is available in the `load-tests` directory using k6 scripts.

---

# API Summary

| Endpoint            | Method | Purpose                  |
| ------------------- | ------ | ------------------------ |
| `/shorten`          | POST   | Create a short URL       |
| `/{shortCode}`      | GET    | Redirect to original URL |
| `/stats/{code}`     | GET    | Retrieve URL statistics  |
| `/actuator/health`  | GET    | Application health       |
| `/actuator/info`    | GET    | Application information  |
| `/actuator/metrics` | GET    | Application metrics      |
