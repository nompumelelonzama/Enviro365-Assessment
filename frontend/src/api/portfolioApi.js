const BASE_URL = "http://localhost:8080/api";

async function parseErrorMessage(response) {
    try {
        const data = await response.json();
        return data.message || `Request failed: ${response.status}`;
    } catch {
        return `Request failed: ${response.status}`;
    }
}

export async function getPortfolioByInvestor(investorId) {
    const response = await fetch(`${BASE_URL}/portfolio/investor/${investorId}`);
    if (!response.ok) {
        throw new Error(await parseErrorMessage(response));
    }
    return response.json();
}

export async function createWithdrawal(portfolioId, productId, amount) {
    const params = new URLSearchParams({ portfolioId, productId, amount });
    const response = await fetch(`${BASE_URL}/withdrawals?${params}`, {
        method: "POST",
    });
    if (!response.ok) {
        throw new Error(await parseErrorMessage(response));
    }
    return response.json();
}

export async function getWithdrawalHistory(portfolioId, { start, end, status } = {}) {
    const params = new URLSearchParams();
    if (start) params.append("start", start);
    if (end) params.append("end", end);
    if (status) params.append("status", status);

    const query = params.toString() ? `?${params}` : "";
    const response = await fetch(`${BASE_URL}/withdrawals/history/${portfolioId}${query}`);
    if (!response.ok) {
        throw new Error(await parseErrorMessage(response));
    }
    return response.json();
}

export function getExportCsvUrl(portfolioId, { start, end, status } = {}) {
    const params = new URLSearchParams();
    if (start) params.append("start", start);
    if (end) params.append("end", end);
    if (status) params.append("status", status);

    const query = params.toString() ? `?${params}` : "";
    return `${BASE_URL}/withdrawals/export/${portfolioId}${query}`;
}