# Dual-product Uber + Rental microservice architecture

## Overview

BoxCars is a dual-domain platform:

| Domain | Side A | Side B |
|--------|--------|--------|
| Ride-hailing | Rider: request → track → pay → rate | Driver: online → accept → complete |
| Car rental | Customer: search → book → pickup/return | Owner: list → confirm → handoff |

## Services & ports

| Service | Port | Database |
|---------|------|----------|
| frontend (Next.js) | 3000 | — |
| api-gateway | 8080 | — |
| auth-service | 8081 | auth_db |
| user-service | 8082 | user_db |
| vehicle-service | 8083 | vehicle_db |
| trip-service | 8084 | trip_db |
| rental-service | 8085 | rental_db |
| matching-service | 8086 | Redis GEO |
| payment-service | 8087 | payment_db |
| notification-service | 8088 | in-memory |
| rating-service | 8089 | rating_db |
| MySQL | 3306 | — |
| Redis | 6379 | — |
| RabbitMQ | 5672 / 15672 | — |

## Request flow

Clients call `http://localhost:8080/api/**`. The API Gateway validates JWT (except public auth/vehicle search routes), injects `X-User-Id` and `X-User-Roles`, and routes to the owning service.

Domain events publish to RabbitMQ topic exchange `boxcars.events`. Notification, matching, and payment services consume lifecycle events.

## Local run

```bash
docker compose up -d mysql redis rabbitmq
cd backend && mvn -pl common,auth-service,user-service,vehicle-service,trip-service,rental-service,matching-service,payment-service,notification-service,rating-service,api-gateway -am package -DskipTests
# start each service jar or via IDE
cd frontend && npm run dev
```

Or: `docker compose up --build` for full stack.

## Frontend modules

See [frontend/MODULES.md](../frontend/MODULES.md). Product UIs live under `src/modules/{ride,drive,rent,owner}` and call the gateway at `/api/**`.