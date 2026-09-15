import { useState, useEffect } from "react";
import {
  getPortfolioByInvestor,
  createWithdrawal,
  getWithdrawalHistory,
  getExportCsvUrl,
} from "./api/portfolioApi";
import "./App.css";

const TEST_INVESTORS = [
  { id: 1, label: "Thabo Nkosi" },
  { id: 2, label: "Naledi Dube" },
  { id: 3, label: "Lindiwe Khumalo" },
];

function App() {
  const [investorId, setInvestorId] = useState(1);
  const [portfolio, setPortfolio] = useState(null);
  const [history, setHistory] = useState([]);
  const [amount, setAmount] = useState("");
  const [productId, setProductId] = useState("");
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(true);

  async function loadPortfolio(id) {
    try {
      const data = await getPortfolioByInvestor(id);
      setPortfolio(data);
      setProductId(data.products?.[0]?.id ?? "");
      return data;
    } catch (err) {
      setError("Could not load portfolio: " + err.message);
      setPortfolio(null);
      return null;
    }
  }

  async function loadHistory(portfolioId) {
    try {
      const data = await getWithdrawalHistory(portfolioId);
      setHistory(data);
    } catch (err) {
      setError("Could not load withdrawal history: " + err.message);
    }
  }

  useEffect(() => {
    (async () => {
      setLoading(true);
      setError(null);
      setSuccess(null);
      const data = await loadPortfolio(investorId);
      if (data) await loadHistory(data.id);
      setLoading(false);
    })();
  }, [investorId]);

  async function handleWithdraw(e) {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!productId) {
      setError("Please select a product to withdraw from.");
      return;
    }

    const numericAmount = parseFloat(amount);
    if (!numericAmount || numericAmount <= 0) {
      setError("Please enter a valid withdrawal amount.");
      return;
    }

    try {
      await createWithdrawal(portfolio.id, productId, numericAmount);
      setSuccess(`Withdrawal of R${numericAmount.toFixed(2)} submitted successfully.`);
      setAmount("");
      const updated = await loadPortfolio(investorId);
      if (updated) await loadHistory(updated.id);
    } catch (err) {
      setError(err.message || "Withdrawal failed. Please try again.");
    }
  }

  function handleExport() {
    if (!portfolio) return;
    window.location.href = getExportCsvUrl(portfolio.id);
  }

  return (
      <div className="dashboard">
        <h1>Portfolio Dashboard</h1>
        {portfolio?.investor && (
            <p className="investor-name">
              {portfolio.investor.firstName} {portfolio.investor.lastName} (age {portfolio.investor.age})
            </p>
        )}

        <div className="investor-switch">
          <label>Investor: </label>
          <select value={investorId} onChange={(e) => setInvestorId(Number(e.target.value))}>
            {TEST_INVESTORS.map((inv) => (
                <option key={inv.id} value={inv.id}>{inv.label}</option>
            ))}
          </select>
        </div>

        {loading && <p className="status">Loading portfolio...</p>}

        {!loading && portfolio && (
            <div className="dashboard-grid">
              <div className="dashboard-left">
                <section className="card">
                  <h2>Balance</h2>
                  <p className="balance">R{Number(portfolio.balance).toLocaleString()}</p>
                </section>

                <section className="card">
                  <h2>Product Balances</h2>
                  {portfolio.products?.length ? (
                      <ul className="product-balances">
                        {portfolio.products.map((p) => {
                          const initial = Number(p.initialAmount ?? p.amount);
                          const remaining = Number(p.amount);
                          const withdrawn = initial - remaining;
                          return (
                              <li key={p.id} className="product-balance-item">
                                <div className="product-name">
                                  {p.name} ({p.type === "UNIT_TRUST" ? "Unit Trust" : "Retirement Annuity"})
                                </div>
                                <div className="product-figures">
                                  <span>Initial: R{initial.toLocaleString()}</span>
                                  <span>Withdrawn: R{withdrawn.toLocaleString()}</span>
                                  <span className="product-amount">Remaining: R{remaining.toLocaleString()}</span>
                                </div>
                              </li>
                          );
                        })}
                      </ul>
                  ) : (
                      <p className="muted">No products linked to this portfolio.</p>
                  )}
                </section>

                <section className="card">
                  <h2>Request a Withdrawal</h2>
                  <form onSubmit={handleWithdraw}>
                    <select
                        value={productId}
                        onChange={(e) => setProductId(e.target.value)}
                    >
                      {portfolio.products?.length ? (
                          portfolio.products.map((p) => (
                              <option key={p.id} value={p.id}>
                                {p.name} ({p.type === "UNIT_TRUST" ? "Unit Trust" : "Retirement Annuity"})
                              </option>
                          ))
                      ) : (
                          <option value="">No products available</option>
                      )}
                    </select>
                    <input
                        type="number"
                        step="0.01"
                        placeholder="Amount"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                    />
                    <button type="submit">Submit Withdrawal</button>
                  </form>
                  {error && <p className="status error">{error}</p>}
                  {success && <p className="status success">{success}</p>}
                </section>
              </div>

              <div className="dashboard-right">
                <section className="card history-card">
                  <div className="history-header">
                    <h2>Withdrawal History</h2>
                    <button onClick={handleExport}>Download CSV</button>
                  </div>
                  {history.length ? (
                      <table>
                        <thead>
                        <tr>
                          <th>Date</th>
                          <th>Amount</th>
                          <th>Balance After</th>
                          <th>Status</th>
                        </tr>
                        </thead>
                        <tbody>
                        {history.map((w) => (
                            <tr key={w.id}>
                              <td>{new Date(w.requestDate).toLocaleString()}</td>
                              <td>R{Number(w.amount).toLocaleString()}</td>
                              <td>R{Number(w.balanceAfterWithdrawal).toLocaleString()}</td>
                              <td>{w.status}</td>
                            </tr>
                        ))}
                        </tbody>
                      </table>
                  ) : (
                      <p className="muted">No withdrawals yet.</p>
                  )}
                </section>
              </div>
            </div>
        )}

        {!loading && !portfolio && (
            <p className="status error">{error || "No portfolio found."}</p>
        )}
      </div>
  );
}

export default App;