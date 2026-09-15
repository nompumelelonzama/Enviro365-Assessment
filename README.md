# Enviro365 Investments Ã¢â‚¬â€ Withdrawal Notice System

Junior Developer Assessment (eTalente, June 2026) Ã¢â‚¬â€ full-stack submission.

Investors select their profile, view their own portfolio, submit withdrawal
notices against the business rules below, review their withdrawal history,
and download a CSV statement.

## Folder structure

## Quick start

Start the backend first:

```bash
cd NompumeleloNzama
./mvnw spring-boot:run
```

Confirm it's running at `http://localhost:8080`. On startup, the console
will print a confirmation that the seed investors were loaded.

In a separate terminal, start the frontend:

```bash
cd frontend
npm install
npm run dev
```

Open the URL it prints (usually `http://localhost:5173`) in your browser.

Use the **Investor** dropdown at the top of the dashboard to switch between
seeded investors. The dashboard updates to show only that investor's own
portfolio.

## Investor test data at a glance

| Investor         | Age | Product mix                                | What it tests |
|-------------------|-----|---------------------------------------------|----------------|
| Thabo Nkosi       | 35  | Flexible Unit Trust, Retirement Annuity Fund | Retirement withdrawal rejected (under 65) |
| Naledi Dube       | 70  | Flexible Unit Trust, Retirement Annuity Fund | Retirement withdrawal allowed (over 65) |
| Lindiwe Khumalo   | 40  | Flexible Unit Trust, Retirement Annuity Fund | Retirement withdrawal rejected; also demonstrates the 90% withdrawal cap |

## Demo walkthrough (for someone reviewing this for the first time)

1. Start the backend (`./mvnw spring-boot:run`) and confirm the console
   shows the seed message.
2. Start the frontend (`npm run dev` inside `frontend/`).
3. Use the **Investor** dropdown to select a profile Ã¢â‚¬â€ e.g. Thabo Nkosi, to
   see the portfolio and rejection case for an under-65 retirement
   withdrawal.
4. On the dashboard, review the portfolio Ã¢â‚¬â€ you should see the selected
   investor's products and balances only (not any other investor's).
5. Submit a withdrawal notice against one of the listed products.
   - If the product is a Retirement Annuity and the investor is 65 or
     under, the request is rejected with a clear error message.
   - If the amount exceeds 90% of the portfolio balance, it's rejected
     regardless of product type.
   - Otherwise, it succeeds and appears in the withdrawal history table.
6. Use the **Download CSV** button to export the withdrawal history Ã¢â‚¬â€
   optionally filter by status first.
7. Switch to a different investor in the dropdown to see the other cases.

## What's included

### Backend
- Retrieves an investor's own portfolio (details + products)
- Creates withdrawal notices with full balance calculations
- Exports CSV statements with optional status filtering
- Enforces all business rules: sufficient balance, 90% withdrawal cap,
  and the retirement age restriction (over 65 only)
- Centralized input validation and global exception handling

### Frontend
- Portfolio dashboard (React + Vite) with an investor selector
- Withdrawal form, withdrawal history table, and a CSV download button Ã¢â‚¬â€
  all scoped to the selected investor

**Advanced requirements implemented (3 of 5):** global exception handling,
input validation, unit tests. See `DOCUMENTATION.md` for full detail.

## API overview

See `DOCUMENTATION.md` for the full endpoint list, request/response shapes,
and business rule breakdown. In brief:

- `GET /api/portfolio/investor/{investorId}` Ã¢â‚¬â€ fetch an investor's products
- `GET /api/withdrawals/history/{portfolioId}` Ã¢â‚¬â€ fetch withdrawal history
- `POST /api/withdrawals` Ã¢â‚¬â€ submit a new withdrawal notice
- `GET /api/withdrawals/export/{portfolioId}` Ã¢â‚¬â€ download a CSV statement

## Screenshots

**Portfolio dashboard**
![Portfolio dashboard](screenshots/Dashboard.png)

**Successful withdrawal (Thabo Nkosi, Unit Trust)**
![Approved withdrawal](screenshots/Approved_Status.png)

**Age restriction rejection (Thabo Nkosi, age 35, Retirement Annuity)**
![Age restriction rejected](screenshots/Age_Restriction.png)

**90% withdrawal limit exceeded (Lindiwe Khumalo)**
![Withdrawal limit exceeded](screenshots/Withdrawal_Exception.png)

**Withdrawal history Ã¢â‚¬â€ approved and rejected notices**
![Approved and rejected history](screenshots/Approved_and_Reject_Status.png)

**CSV statement download**
![CSV download](screenshots/CSV_download.png)

**CSV opened in Excel**
![CSV opened in Excel](screenshots/CSV_Download_Example.png)

**H2 console Ã¢â‚¬â€ seeded data**
![H2 database console](screenshots/H2_Database.png)

## AI usage disclosure

AI assistance was used to help draft boilerplate, documentation, input
validation, and to review the project against the assessment brief. All
business logic and validation rules were reviewed and verified manually.