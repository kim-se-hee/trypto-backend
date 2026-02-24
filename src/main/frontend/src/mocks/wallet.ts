export interface WalletCoinBalance {
  coinSymbol: string;
  coinName: string;
  available: number;
  locked: number;
  currentPrice: number;
}

export interface WalletData {
  exchangeId: string;
  exchangeName: string;
  baseCurrency: string;
  walletAddress: string;
  chain: string;
  balances: WalletCoinBalance[];
}

export const walletData: WalletData[] = [
  {
    exchangeId: "upbit",
    exchangeName: "업비트",
    baseCurrency: "KRW",
    walletAddress: "",
    chain: "",
    balances: [
      { coinSymbol: "KRW", coinName: "원화", available: 2_450_000, locked: 500_000, currentPrice: 1 },
      { coinSymbol: "BTC", coinName: "비트코인", available: 0.04234100, locked: 0.01000000, currentPrice: 143_250_000 },
      { coinSymbol: "ETH", coinName: "이더리움", available: 1.14500000, locked: 0.10000000, currentPrice: 4_821_000 },
      { coinSymbol: "XRP", coinName: "리플", available: 12_420.00000000, locked: 3_000.00000000, currentPrice: 3_456 },
      { coinSymbol: "SOL", coinName: "솔라나", available: 10.34000000, locked: 2.00000000, currentPrice: 287_400 },
      { coinSymbol: "DOGE", coinName: "도지코인", available: 85_000.00000000, locked: 0, currentPrice: 542 },
      { coinSymbol: "ADA", coinName: "에이다", available: 8_500.00000000, locked: 0, currentPrice: 1_234 },
      { coinSymbol: "AVAX", coinName: "아발란체", available: 45.60000000, locked: 0, currentPrice: 62_300 },
      { coinSymbol: "LINK", coinName: "체인링크", available: 120.00000000, locked: 0, currentPrice: 28_900 },
    ],
  },
  {
    exchangeId: "bithumb",
    exchangeName: "빗썸",
    baseCurrency: "KRW",
    walletAddress: "",
    chain: "",
    balances: [
      { coinSymbol: "KRW", coinName: "원화", available: 1_230_000, locked: 200_000, currentPrice: 1 },
      { coinSymbol: "BTC", coinName: "비트코인", available: 0.01800000, locked: 0.00300000, currentPrice: 143_180_000 },
      { coinSymbol: "ETH", coinName: "이더리움", available: 0.85000000, locked: 0, currentPrice: 4_815_000 },
      { coinSymbol: "ADA", coinName: "에이다", available: 10_000.00000000, locked: 2_000.00000000, currentPrice: 1_230 },
      { coinSymbol: "DOT", coinName: "폴카닷", available: 350.00000000, locked: 0, currentPrice: 12_420 },
    ],
  },
  {
    exchangeId: "binance",
    exchangeName: "바이낸스",
    baseCurrency: "USDT",
    walletAddress: "0x7a3B...F91e",
    chain: "ERC-20",
    balances: [
      { coinSymbol: "USDT", coinName: "테더", available: 4_920.50, locked: 500.00, currentPrice: 1 },
      { coinSymbol: "BTC", coinName: "Bitcoin", available: 0.10500000, locked: 0.01500000, currentPrice: 97_842.50 },
      { coinSymbol: "ETH", coinName: "Ethereum", available: 3.20000000, locked: 0.30000000, currentPrice: 3_298.75 },
      { coinSymbol: "SOL", coinName: "Solana", available: 22.00000000, locked: 3.00000000, currentPrice: 196.45 },
      { coinSymbol: "LINK", coinName: "Chainlink", available: 180.00000000, locked: 20.00000000, currentPrice: 19.78 },
      { coinSymbol: "ARB", coinName: "Arbitrum", available: 1_500.00000000, locked: 0, currentPrice: 1.8934 },
    ],
  },
];
