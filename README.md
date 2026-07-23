# Event-Driven Order & Inventory Microservices

![Java](https://img.shields.io/badge/Java-24-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F?logo=springboot&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-Event--Driven-231F20?logo=apachekafka&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?logo=apachemaven&logoColor=white)

An event-driven microservices system built with **Spring Boot** and **Apache Kafka**, simulating a store's order and inventory workflow. Two independently deployable services communicate asynchronously through Kafka topics — they never call each other directly.

---

## Architecture

```
[ Web UI ]
     |
     |  POST /orders  (customer + items)
     v
[ Order Service ]  :8081
     |  - saves order as PENDING (MySQL: siparisdb)
     |  - publishes message
     v
 ( Kafka topic: order-events )
     |
     v
[ Inventory Service ]  :8082
     |  - checks stock for every item (MySQL: stokdb)
     |  - all-or-nothing decision
     |  - deducts stock only if the whole order can be fulfilled
     v
 ( Kafka topic: inventory-events )
     |
     v
[ Order Service ]
     |  - updates order status: APPROVED / REJECTED
     v
[ Web UI ]  --> polls GET /orders/{id} --> shows final result
```

Key point: the two services share no code and no database. Their only contract is the **topic name** and the **message shape** — a loosely coupled, event-driven design.

---

## Tech Stack

- **Java 24**, **Spring Boot 4**
- **Apache Kafka** (KRaft mode, no ZooKeeper)
- **Spring Data JPA** / Hibernate
- **MySQL 8.4** (one database per service)
- **Docker Compose** for Kafka and MySQL
- Vanilla HTML/CSS/JS front end with status polling

---

## Data Model

The Order Service uses a relational model with three tables:

```
customer                orders                    order_item
--------                ------                    ----------
id                      order_id (PK)             id
name                    status                    product
                        reason                    quantity
                        customer_id  ---+         order_id  ---+
                                        |                      |
              (many orders -> one customer)      (many items -> one order)
```

- `Customer` → `Order` : one-to-many (`@OneToMany` / `@ManyToOne`)
- `Order` → `OrderItem` : one-to-many, cascaded so a single `save` persists the whole graph
- Repeat customers are matched by name instead of creating duplicate rows

The Inventory Service keeps its own `stock` table (product, quantity) in a separate database.

---

## Business Rule: All-or-Nothing

An order may contain multiple items. The Inventory Service validates **every** item before touching the database:

1. **Validate phase** — walk through all items; if any product is missing or has insufficient stock, reject the entire order and return immediately.
2. **Apply phase** — only if every item passes, persist the deducted quantities in one batch.

This guarantees that a rejected order never leaves partially consumed stock behind.

---

## Running the Project

**Prerequisites:** Docker, JDK 24, Maven

**1. Start Kafka and MySQL**

```bash
docker compose up -d
docker ps   # verify stok-kafka and stok-mysql are Up
```

Kafka is exposed on `localhost:9094`, MySQL on `localhost:3307`. The `mysql-init` script creates both databases (`siparisdb`, `stokdb`) on first run.

**2. Start the services**

Run each Spring Boot application (from your IDE or with Maven):

```bash
cd siparis-servisi && ./mvnw spring-boot:run    # Order Service    -> :8081
cd stok-servisi   && ./mvnw spring-boot:run    # Inventory Service -> :8082
```

The Inventory Service seeds initial stock on first startup (keyboard: 10, mouse: 3, monitor: 5).

**3. Open the UI**

```
http://localhost:8081/
```

Enter a customer name, add one or more products, and submit. The page shows `PENDING` first, then polls for the final status.

---

## API

**Create an order**

```http
POST /orders
Content-Type: application/json

{
  "customerName": "Ahmet",
  "items": [
    { "product": "keyboard", "quantity": 2 },
    { "product": "mouse",    "quantity": 1 }
  ]
}
```

Responds immediately with the created order and status `BEKLEMEDE` (pending) — stock is checked asynchronously.

**Check order status**

```http
GET /orders/{orderId}
```

Returns the order with its current status (`BEKLEMEDE`, `ONAYLANDI`, `REDDEDILDI`) and a human-readable reason.

---

## Kafka Topics

| Topic              | Producer          | Consumer          | Payload                                   |
|--------------------|-------------------|-------------------|-------------------------------------------|
| `order-events`     | Order Service     | Inventory Service | orderId, customerName, items[]            |
| `inventory-events` | Inventory Service | Order Service     | orderId, status, reason                   |

Messages are serialized as JSON. Each service defines its own message classes; the consumer is configured to ignore type headers and map incoming JSON to its local type, so the services stay independent of each other's packages.

---

## Notes

- Both services run outside Docker and connect to Kafka through its `EXTERNAL` listener (`localhost:9094`). The compose file also defines an `INTERNAL` listener for container-to-container access.
- MySQL is exposed on port `3307` to avoid clashing with a local MySQL instance on the default `3306`.
- Schema is generated by Hibernate (`ddl-auto=update`). For a production setup this would be replaced with a migration tool such as Flyway.
