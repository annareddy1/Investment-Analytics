import axios from "axios";

// ✅ Correct env variable name (consistent)
// In local: REACT_APP_API_BASE_URL=http://localhost:8001
// In prod:  REACT_APP_API_BASE_URL=https://marketlens-backend-3pas.onrender.com

const API_BASE =
  process.env.REACT_APP_API_BASE_URL
    ? `${process.env.REACT_APP_API_BASE_URL}/api`
    : "http://localhost:8001/api";

// ✅ Create axios instance (cleaner + reusable)
const api = axios.create({
  baseURL: API_BASE,
  headers: {
    "Content-Type": "application/json",
  },
});

// Run analysis for a ticker
export const runAnalysis = async (ticker, period = "1Y") => {
  try {
    const response = await api.post("/analysis/run", { ticker, period });
    return response.data;
  } catch (error) {
    console.error("Error running analysis:", error);
    throw error;
  }
};

// Get analysis by ID
export const getAnalysis = async (analysisId) => {
  try {
    const response = await api.get(`/analysis/${analysisId}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching analysis:", error);
    throw error;
  }
};

// Poll analysis until complete
export const pollAnalysis = async (analysisId, maxAttempts = 60, interval = 2000) => {
  for (let i = 0; i < maxAttempts; i++) {
    try {
      const result = await getAnalysis(analysisId);

      if (result.status === "COMPLETED") return result;
      if (result.status === "FAILED") throw new Error(result.message || "Analysis failed");

      await new Promise((resolve) => setTimeout(resolve, interval));
    } catch (error) {
      if (i === maxAttempts - 1) throw error;
    }
  }

  throw new Error("Analysis timed out after 2 minutes");
};

// Get latest analysis for a ticker
export const getLatestAnalysis = async (ticker) => {
  try {
    const response = await api.get(`/analysis/ticker/${ticker}/latest`);
    return response.data;
  } catch (error) {
    console.error("Error fetching latest analysis:", error);
    throw error;
  }
};

// Get preset tickers
export const getPresetTickers = async () => {
  try {
    const response = await api.get("/tickers/presets");
    return response.data.tickers;
  } catch (error) {
    console.error("Error fetching preset tickers:", error);
    throw error;
  }
};