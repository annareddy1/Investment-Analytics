import axios from 'axios';

const API_BASE = `${process.env.REACT_APP_BACKEND_URL}/api`;

// Run analysis for a ticker
export const runAnalysis = async (ticker, period = '1Y') => {
  try {
    const response = await axios.post(`${API_BASE}/analysis/run`, {
      ticker,
      period
    });
    return response.data;
  } catch (error) {
    console.error('Error running analysis:', error);
    throw error;
  }
};

// Get analysis by ID
export const getAnalysis = async (analysisId) => {
  try {
    const response = await axios.get(`${API_BASE}/analysis/${analysisId}`);
    return response.data;
  } catch (error) {
    console.error('Error fetching analysis:', error);
    throw error;
  }
};

// Poll analysis until complete
export const pollAnalysis = async (analysisId, maxAttempts = 60, interval = 2000) => {
  for (let i = 0; i < maxAttempts; i++) {
    try {
      const result = await getAnalysis(analysisId);
      
      if (result.status === 'COMPLETED') {
        return result;
      }
      
      if (result.status === 'FAILED') {
        throw new Error(result.message || 'Analysis failed');
      }
      
      // Wait before polling again
      await new Promise(resolve => setTimeout(resolve, interval));
    } catch (error) {
      if (i === maxAttempts - 1) throw error;
    }
  }
  
  throw new Error('Analysis timed out after 2 minutes');
};

// Get latest analysis for a ticker
export const getLatestAnalysis = async (ticker) => {
  try {
    const response = await axios.get(`${API_BASE}/analysis/ticker/${ticker}/latest`);
    return response.data;
  } catch (error) {
    console.error('Error fetching latest analysis:', error);
    throw error;
  }
};

// Get preset tickers
export const getPresetTickers = async () => {
  try {
    const response = await axios.get(`${API_BASE}/tickers/presets`);
    return response.data.tickers;
  } catch (error) {
    console.error('Error fetching preset tickers:', error);
    throw error;
  }
};
