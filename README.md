# Enviro365 Investments — Withdrawal Notice System

Junior Developer Assessment (eTalente, June 2026) — full-stack submission.

Investors select their profile, view their own portfolio, submit withdrawal
notices against the business rules below, review their withdrawal history,
and download a CSV statement.

## Folder structure

```
enviro365-project/
├── backend/          Spring Boot API (Java 17, Maven)
└── frontend/         Plain HTML/CSS/JS, no build step
    ├── index.html    Portfolio dashboard, investor selector, withdrawal form, history, CSV
    └── styles.css    Shared styles
```

## Quick start

Start the backend first:

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Confirm it's running at `http://localhost:8080`. On startup, the console
will print a confirmation that the seed investors were loaded.

Open `frontend/index.html` directly in a browser (just double-click the
file — no build step or local server needed).

Use the **Investor** dropdown at the top of the dashboard to switch between
seeded investors. The dashboard updates to show only that investor's own
portfolio.

## Investor test data at a glance

| Investor         | Age | Product mix                                | What it tests |
|-------------------|-----|---------------------------------------------|----------------|
| Thabo Nkosi       | 35  | Flexible Unit Trust, Retirement Annuity Fund | Retirement withdrawal rejected (under 65) |
| Naledi Dube       | TBD | TBD                                          | TBD |
| Lindiwe Khumalo   | TBD | TBD                                          | TBD |

> Fill in the age, product mix, and test purpose for Naledi Dube and
> Lindiwe Khumalo once confirmed.

## Demo walkthrough (for someone reviewing this for the first time)

1. Start the backend (`mvn spring-boot:run` in `backend/`) and confirm the
   console shows the seed message.
2. Open `frontend/index.html`.
3. Use the **Investor** dropdown to select a profile — e.g. Thabo Nkosi, to
   see the portfolio and rejection case for an under-65 retirement
   withdrawal.
4. On the dashboard, review the portfolio — you should see the selected
   investor's products and balances only (not any other investor's).
5. Submit a withdrawal notice against one of the listed products.
   - If the product is a Retirement Annuity/Living Annuity and the investor
     is 65 or under, the request should be rejected with a clear error
     message.
   - Otherwise, it should succeed and appear in the withdrawal history
     table below.
6. Use the **Download CSV** button to export the withdrawal history —
   optionally filter by status first.
7. Switch to a different investor in the dropdown to see the other cases.

## What's included

### Backend
- Retrieves an investor's own portfolio (details + products)
- Creates withdrawal notices with full balance calculations
- Exports CSV statements with optional status filtering
- Enforces all four required business rules

### Frontend
- Portfolio dashboard (`index.html`) with an investor selector
- Withdrawal form, withdrawal history table, and a CSV download button —
  all scoped to the selected investor

**Advanced requirements chosen (3 of 5):** global exception handling,
input validation, DTO layer. See `backend/README.md` for detail.

## API overview

See `backend/README.md` for the full endpoint list and request/response
shapes. In brief:

- `GET /api/investors/{id}/portfolio` — fetch an investor's products
- `GET /api/withdrawals/investor/{id}` — fetch withdrawal history
- `POST /api/withdrawals` — submit a new withdrawal notice

## Screenshots

Add screenshots of the running app here before submitting:
- Portfolio dashboard (showing an investor's products)
- A successful withdrawal
- A rejected withdrawal (e.g. Thabo Nkosi, age 35, Retirement Annuity)

## AI usage disclosure

AI assistance was used to help draft boilerplate (entity/controller
scaffolding, and this documentation), and to review the project against
the assessment brief. All business logic and validation rules were
reviewed and verified manually.
