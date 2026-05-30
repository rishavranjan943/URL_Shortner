# URL Shortener

A URL Shortener built using Spring Boot, PostgreSQL, and Spring Data JPA.

## Features

* Generate short URLs from long URLs
* Redirect users using short URLs
* Track click counts
* Prevent duplicate URL entries
* URL validation
* RESTful APIs

## Tech Stack

* Java 21
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Maven
* Postman

## Project Structure

src/main/java

* controller
* service
* repository
* entity
* dto

## APIs

### Create Short URL

POST /api/shorten

Request

```json
{
  "longUrl":"https://example.com"
}
```

Response

```json
{
  "shortCode":"abc123"
}
```

### Redirect URL

GET /api/{code}

Redirects user to original URL.

### URL Statistics

GET /api/stats/{code}

Response

```json
{
  "shortUrl":"abc123",
  "longUrl":"https://example.com",
  "clickCount":10
}
```

## Database Configuration

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/urlshortner
spring.datasource.username=postgres
spring.datasource.password=your_password
```

## Run Locally

```bash
git clone <repository-url>

cd URLShortner

mvn spring-boot:run
```

Application runs at:

```text
http://localhost:8080
```

## Future Improvements

* Custom aliases
* User authentication
* Redis caching
* Docker deployment
* Analytics dashboard

```
```
