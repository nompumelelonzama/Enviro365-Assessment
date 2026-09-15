# Enviro365 Withdrawal Notice System — Application Documentation

## 1. Overview

A full-stack investor withdrawal system. Investors can view their own
portfolio, submit withdrawal notices against a fixed set of business
rules, review their withdrawal history, and export that history as a
CSV statement.

This document describes the **backend** (Spring Boot API), which is the
source of truth for all business logic, data, and validation.

## 2. Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 4.1.1 |
| Build tool | Maven |
| Persistence | Spring Data JPA / Hibernate |
| Database | H2 (in-memory) |
| Boilerplate reduction | Lombok |
| Testing | JUnit 5 + Mockito |

## 3. Project structure

```
src/main/java/com/enviro/assessment/junior/nompumelelonzama/
├── NompumeleloNzamaApplication.java   Spring Boot entry point
├── config/
│   └── WebConfig.java                 CORS configuration
├── controller/
│   ├── PortfolioController.java       Portfolio endpoints
│   └── WithdrawalController.java      Withdrawal endpoints
├── entity/
│   ├── Investor.java
│   ├── Portfolio.java
│   ├── Product.java
│   ├── ProductType.java               (enum)
│   ├── WithdrawalNotice.java
│   └── WithdrawalStatus.java          (enum)
├── exception/
│   ├── AgeRestrictionException.java
│   ├── InsufficientBalanceException.java
│   ├── WithdrawalLimitExceededException.java
│   ├── ResourceNotFoundException.java
│   ├── ErrorResponse.java             Standard error payload
│   └── GlobalExceptionHandler.java    Maps exceptions -> HTTP responses
├── repository/
│   ├── InvestorRepository.java
│   ├── PortfolioRepository.java
│   ├── ProductRepository.java
│   └── WithdrawalNoticeRepository.java
└── service/
    ├── PortfolioService.java          Portfolio lookups
    ├── WithdrawalService.java         Core withdrawal business logic
    └── WithdrawalHistoryService.java  History filtering + CSV export
```

## 4. Data model

**Investor** — `id`, `firstName`, `lastName`, `age`, `email`
- One-to-one with `Portfolio`

**Portfolio** — `id`, `balance`, `investor`, `products[]`, `withdrawalNotices[]`
- Belongs to one `Investor`
- Has many `Product`s and `WithdrawalNotice`s (cascade on delete)

**Product** — `id`, `name`, `type`, `amount`, `initialAmount`, `portfolio`
- `type` is one of: `RETIREMENT_ANNUITY`, `UNIT_TRUST`, `TAX_FREE_SAVINGS`, `ENDOWMENT`

**WithdrawalNotice** — `id`, `amount`, `balanceAfterWithdrawal`, `requestDate`, `status`, `reason`, `portfolio`
- `status` is `APPROVED` or `REJECTED`
- `reason` is populated only for rejected notices

## 5. Business rules (enforced in `WithdrawalService.createWithdrawal`)

Every withdrawal request is checked against these rules, **in order**.
The first rule that fails stops the request; a rejected `WithdrawalNotice`
is still recorded (with the failure reason) before the exception is thrown.

1. **Sufficient balance** — the requested amount cannot exceed the
   portfolio's current balance.
   → `InsufficientBalanceException`

2. **90% withdrawal cap** — the requested amount cannot exceed 90% of the
   portfolio's current balance (even if the balance itself could cover it).
   → `WithdrawalLimitExceededException`

3. **Retirement age restriction** — if the product being withdrawn from is
   a `RETIREMENT_ANNUITY`, the investor must be **older than 65**
   (age 65 exactly is still rejected).
   → `AgeRestrictionException`

If all three checks pass, the withdrawal is approved:
- The portfolio's `balance` is reduced by the withdrawal amount
- The specific product's `amount` is reduced by the same amount
- An `APPROVED` `WithdrawalNotice` is created and returned

If a requested portfolio or product ID doesn't exist, a
`ResourceNotFoundException` is thrown before any rule is evaluated.

## 6. API reference

Base URL: `http://localhost:8080`

### `GET /api/portfolio/investor/{investorId}`
Returns the portfolio (balance + products) belonging to the given investor.

### `POST /api/withdrawals`
Submits a withdrawal notice.

Query parameters:
| Param | Type | Required |
|---|---|---|
| `portfolioId` | Long | yes |
| `productId` | Long | yes |
| `amount` | BigDecimal | yes |

Returns the created `WithdrawalNotice` (status `APPROVED`), or a `400`
error response if a business rule fails.

### `GET /api/withdrawals/history/{portfolioId}`
Returns the withdrawal history for a portfolio.

Optional query parameters: `start`, `end` (ISO date-time, filters by
`requestDate` range), `status` (`APPROVED` or `REJECTED`).

### `GET /api/withdrawals/export/{portfolioId}`
Same filtering as the history endpoint, but returns a `text/csv` file
(`withdrawal_statement.csv`) with columns: ID, Amount, Balance After
Withdrawal, Request Date, Status.

## 7. Error handling

All exceptions are caught centrally by `GlobalExceptionHandler` and
returned as a consistent JSON shape:

```json
{
  "timestamp": "2026-09-14T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Withdrawal amount exceeds 90% of the portfolio balance."
}
```

| Exception | HTTP status |
|---|---|
| `ResourceNotFoundException` | 404 |
| `InsufficientBalanceException` | 400 |
| `WithdrawalLimitExceededException` | 400 |
| `AgeRestrictionException` | 400 |
| Any other unhandled exception | 500 |

## 8. CORS

`WebConfig` allows requests to `/api/**` from any `http://localhost:*`
origin, covering `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, with all
headers permitted — needed since the frontend is served separately
(e.g. from a dev server on a different port) with no shared origin.

## 9. Seed data

Loaded automatically on startup from `data.sql` (H2 is in-memory, so
this reseeds every restart):

| Investor | Age | Portfolio balance | Products |
|---|---|---|---|
| Thabo Nkosi | 35 | R148,800 | Flexible Unit Trust (R100,000), Retirement Annuity Fund (R48,800) |
| Naledi Dube | 70 | R500,000 | Flexible Unit Trust (R200,000), Retirement Annuity Fund (R300,000) |
| Lindiwe Khumalo | 40 | R90,000 | Flexible Unit Trust (R40,000), Retirement Annuity Fund (R50,000) |

This gives one investor under the retirement age (Thabo), one well
over it (Naledi), and one more under it (Lindiwe) — enough to exercise
both the approval and age-rejection paths.

## 10. Running locally

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. The H2 console is available
at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:enviro365db`,
user `sa`, no password) if you want to inspect the seeded data directly.

## 11. Testing

`WithdrawalServiceTest` covers the service layer with Mockito, including:
- A successful withdrawal with no retirement product involved
- A `ResourceNotFoundException` for a non-existent portfolio
- An `InsufficientBalanceException` for an amount over the balance
- A `WithdrawalLimitExceededException` for an amount over the 90% cap
- An `AgeRestrictionException` for a retirement withdrawal under 65
- A successful retirement withdrawal for an investor over 65

Run with:

```bash
mvn test
```

## 12. Notes / known gaps

- There is no authentication layer in the current backend — investor
  selection happens client-side (see the frontend's investor selector).
  If authentication is added later, this document should be updated
  with the login flow and any session/token handling.
- `ProductType` includes `TAX_FREE_SAVINGS` and `ENDOWMENT`, but the
  seed data only populates `UNIT_TRUST` and `RETIREMENT_ANNUITY`
  products — useful to know if you're testing product-type-specific
  behavior.
