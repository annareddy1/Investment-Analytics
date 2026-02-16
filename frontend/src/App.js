import React, { useState } from "react";
import "./App.css";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import LandingPage from "./components/LandingPage";
import Dashboard from "./components/Dashboard";
import CaseStudy from "./components/CaseStudy";
import { runAnalysis, pollAnalysis } from "./services/api";
import { Toaster } from "./components/ui/sonner";
import { toast } from "sonner";

function App() {
  const [analysisData, setAnalysisData] = useState(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleRunDemo = async (ticker, navigate) => {
    setIsLoading(true);
    toast.loading(`Analyzing ${ticker}...`, { id: "analysis" });

    try {
      // Start analysis
      const start = await runAnalysis(ticker, "1Y");

      // Defensive: ensure analysisId exists
      if (!start || !start.analysisId) {
        throw new Error(
          `Backend did not return analysisId. Response: ${JSON.stringify(start)}`
        );
      }

      const { analysisId } = start;

      // Poll for completion
      const result = await pollAnalysis(analysisId);

      // Defensive: ensure expected fields exist
      if (!result || !result.analytics || !result.charts) {
        throw new Error(
          `Backend returned unexpected result shape: ${JSON.stringify(result)}`
        );
      }

      // Transform backend data to match frontend format
      const transformedData = {
        ticker: result.ticker,
        period: result.period,
        generatedAt: result.generatedAt,
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
                return: r.returnValue, // matches your backend mapping
              }))
            : [],
          volatility: result.charts.volatility,
          rsi: result.charts.rsi,
        },
      };

      setAnalysisData(transformedData);
      toast.success(`${ticker} analysis complete!`, { id: "analysis" });

      if (navigate) navigate("/dashboard");
    } catch (error) {
      // Axios-friendly error message
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
            element={<Dashboard analysisData={analysisData} isLoading={isLoading} />}
          />
          <Route path="/case-study" element={<CaseStudy />} />
        </Routes>
      </BrowserRouter>
      <Toaster />
    </div>
  );
}

export default App;
