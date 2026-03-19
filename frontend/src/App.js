import React, { useEffect, useState } from "react";
import "./App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import LandingPage from "./components/LandingPage";
import Dashboard from "./components/Dashboard";
import { runAnalysis, pollAnalysis } from "./services/api";
import { Toaster } from "./components/ui/sonner";
import { toast } from "sonner";

const STORAGE_KEY = "marketlens_analysis_data";

function App() {
  const [analysisData, setAnalysisData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const savedAnalysis = localStorage.getItem(STORAGE_KEY);

    if (savedAnalysis) {
      try {
        setAnalysisData(JSON.parse(savedAnalysis));
      } catch (error) {
        console.error("Failed to parse saved analysis data:", error);
        localStorage.removeItem(STORAGE_KEY);
      }
    }
  }, []);

  const handleRunDemo = async (ticker, period, navigate) => {
    setIsLoading(true);
    toast.loading(`Analyzing ${ticker}...`, { id: "analysis" });

    try {
      const start = await runAnalysis(ticker, period);

      if (!start || !start.analysisId) {
        throw new Error(
          `Backend did not return analysisId. Response: ${JSON.stringify(start)}`
        );
      }

      const { analysisId } = start;
      const result = await pollAnalysis(analysisId);

      if (!result || !result.analytics || !result.charts) {
        throw new Error(
          `Backend returned unexpected result shape: ${JSON.stringify(result)}`
        );
      }

      const transformedData = {
        ticker: result.ticker,
        period: result.period,
        generatedAt: result.generatedAt,
        analysisId,
        lastUpdated: result.generatedAt
          ? new Date(result.generatedAt).toLocaleDateString("en-US", {
              year: "numeric",
              month: "long",
              day: "numeric",
            })
          : "Unknown",
        analytics: {
          cumulativeReturn: result.analytics.cumulativeReturn,
          maxDrawdown: result.analytics.maxDrawdown,
          latestVolatility: result.analytics.latestVolatility,
          latestRSI: result.analytics.latestRSI,
        },
        charts: {
          prices: result.charts.prices,
          returns: Array.isArray(result.charts.returns)
            ? result.charts.returns.map((r) => ({
                date: r.date,
                return: r.returnValue,
              }))
            : [],
          volatility: result.charts.volatility,
          rsi: result.charts.rsi,
        },
      };

      setAnalysisData(transformedData);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(transformedData));

      toast.success(`${ticker} analysis complete!`, { id: "analysis" });

      if (navigate) {
        navigate("/dashboard");
      }
    } catch (error) {
      const axiosMsg =
        error?.response?.data?.error ||
        error?.response?.data?.message ||
        error?.message ||
        "Unknown error";

      console.error("Analysis error:", error);

      toast.error(`Failed to analyze ${ticker}. ${axiosMsg}`, {
        id: "analysis",
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleClearAnalysis = () => {
    setAnalysisData(null);
    localStorage.removeItem(STORAGE_KEY);
    toast.success("Saved analysis cleared");
  };

  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route
            path="/"
            element={<LandingPage onRunDemo={handleRunDemo} isLoading={isLoading} />}
          />
          <Route
            path="/dashboard"
            element={
              <Dashboard
                analysisData={analysisData}
                isLoading={isLoading}
                onClearAnalysis={handleClearAnalysis}
              />
            }
          />
        </Routes>
      </BrowserRouter>
      <Toaster />
    </div>
  );
}

export default App;