# Cedar Market — Store Orders API (Spring Boot)

REST API for the products and orders of a small online store, built with **Java 21 and Spring Boot 3**.

## Features

- Products: list, filter by category, create, update, delete
- Orders: place an order with several items, list by status, change status
- Business rules
  - placing an order checks and reserves stock in one transaction
  - an order line stores the unit price at the time of purchase
  - status workflow: `PENDING → PAID → SHIPPED`; cancel is allowed before shipping and restores stock
  - a product that appears in an order cannot be deleted
- Bean Validation on every request body
- Consistent errors as RFC 7807 problem details (`400`, `404`, `409`)
- Integration tests with MockMvc (8 tests)

## Tech

Java 21 · Spring Boot 3.3 · Spring Web · Spring Data JPA · Hibernate · H2 · Bean Validation · JUnit 5 · Maven

## Run it

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080` with six sample products.

```bash
mvn test
```

## Endpoints

| Method | Path | Description |
| --- | --- | --- |
| `GET` | `/api/products?category=` | List products, optionally by category |
| `GET` | `/api/products/{id}` | Get one product |
| `POST` | `/api/products` | Create a product |
| `PUT` | `/api/products/{id}` | Update a product |
| `DELETE` | `/api/products/{id}` | Delete a product |
| `GET` | `/api/orders?status=` | List orders, optionally by status |
| `GET` | `/api/orders/{id}` | Get one order |
| `POST` | `/api/orders` | Place an order |
| `PATCH` | `/api/orders/{id}/status` | Move an order to the next status |

### Example

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerEmail":"lina@example.com","items":[{"productId":1,"quantity":2},{"productId":2,"quantity":1}]}'
```

```json
{
  "id": 1,
  "customerEmail": "lina@example.com",
  "status": "PENDING",
  "createdAt": "2026-11-15T09:35:01Z",
  "items": [
    { "productId": 1, "productName": "Olive Oil 750ml", "quantity": 2, "unitPrice": 12.50, "lineTotal": 25.00 },
    { "productId": 2, "productName": "Zaatar Mix 250g", "quantity": 1, "unitPrice": 4.75, "lineTotal": 4.75 }
  ],
  "total": 29.75
}
```

Ordering more than the available stock returns `409 Conflict`:

```json
{ "title": "Conflict", "status": 409, "detail": "Not enough stock for Baklava Box 12pc (requested 999, available 20)" }
```

## Project structure

```
src/main/java/com/cedarmarket/orders
  product/   Product entity, repository, controller, request/response records
  order/     CustomerOrder, OrderItem, OrderService (rules), controller
  common/    Exceptions and the global error handler
  SeedData   Sample products loaded on start
```

Data is stored in an in-memory H2 database, so no setup is needed.
The H2 console is available at `/h2-console` (JDBC URL `jdbc:h2:mem:store`).
