import { Routes, Route, Navigate } from "react-router-dom";
import { MarketPage } from "@/pages/MarketPage";
import { PortfolioPage } from "@/pages/PortfolioPage";
import { WalletPage } from "@/pages/WalletPage";
import { RankingPage } from "@/pages/RankingPage";

function App() {
  return (
    <Routes>
      <Route path="/market" element={<MarketPage />} />
      <Route path="/portfolio" element={<PortfolioPage />} />
      <Route path="/wallet" element={<WalletPage />} />
      <Route path="/ranking" element={<RankingPage />} />
      <Route path="*" element={<Navigate to="/market" replace />} />
    </Routes>
  );
}

export default App;
