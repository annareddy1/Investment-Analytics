import React, { useState } from 'react';
import './App.css';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import LandingPage from './components/LandingPage';
import Dashboard from './components/Dashboard';
import CaseStudy from './components/CaseStudy';
import { generateMockAnalysis } from './data/mockData';

function App() {
  const [analysisData, setAnalysisData] = useState(null);

  const handleRunDemo = (ticker) => {
    // Generate mock analysis for the selected ticker
    const analysis = generateMockAnalysis(ticker);
    setAnalysisData(analysis);
  };

  return (
    <div className="App">
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage onRunDemo={handleRunDemo} />} />
          <Route path="/dashboard" element={<Dashboard analysisData={analysisData} />} />
          <Route path="/case-study" element={<CaseStudy />} />
        </Routes>
      </BrowserRouter>
    </div>
  );
}

export default App;