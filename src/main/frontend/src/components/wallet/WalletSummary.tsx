import { useState } from "react";
import { Eye, EyeOff, ArrowDownToLine, ArrowLeftRight } from "lucide-react";
import { cn } from "@/lib/utils";
import { formatCurrency } from "@/lib/formatters";
import type { WalletCoinBalance } from "@/mocks/wallet";

interface WalletSummaryProps {
  balances: WalletCoinBalance[];
  baseCurrency: string;
  exchangeName: string;
}

const HIDDEN = "••••••••";

export function WalletSummary({ balances, baseCurrency, exchangeName }: WalletSummaryProps) {
  const [visible, setVisible] = useState(true);

  const totalAsset = balances.reduce(
    (sum, b) => sum + (b.available + b.locked) * b.currentPrice,
    0,
  );

  const baseCoin = balances.find((b) => b.coinSymbol === baseCurrency);
  const baseTotal = baseCoin ? baseCoin.available + baseCoin.locked : 0;
  const baseAvailable = baseCoin ? baseCoin.available : 0;
  const baseLocked = baseCoin ? baseCoin.locked : 0;

  const actionButtons = [
    { label: "입금", icon: ArrowDownToLine },
    { label: "송금", icon: ArrowLeftRight },
  ];

  return (
    <div className="rounded-2xl bg-card p-6 shadow-card">
      <div className="flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
        {/* Left — balance info */}
        <div>
          <div className="flex items-center gap-2">
            <p className="text-sm font-medium text-muted-foreground">
              {exchangeName} 총 자산
            </p>
            <button
              onClick={() => setVisible((v) => !v)}
              aria-label={visible ? "잔액 숨기기" : "잔액 보기"}
              className="text-muted-foreground/60 transition-colors hover:text-foreground"
            >
              {visible ? <Eye className="h-4 w-4" /> : <EyeOff className="h-4 w-4" />}
            </button>
          </div>

          <p className="mt-1 font-mono text-3xl font-extrabold tabular-nums tracking-tight">
            {visible ? formatCurrency(totalAsset, baseCurrency) : HIDDEN}
          </p>

          <div className="mt-1.5 flex items-center gap-3 text-sm text-muted-foreground">
            <span>
              <span className="text-xs">보유 {baseCurrency}</span>{" "}
              <span className={cn("font-mono font-semibold tabular-nums", !visible && "blur-sm select-none")}>
                {visible ? formatCurrency(baseTotal, baseCurrency) : HIDDEN}
              </span>
            </span>
            {baseLocked > 0 && visible && (
              <span className="text-xs text-muted-foreground/60">
                (사용 가능 {formatCurrency(baseAvailable, baseCurrency)} / 잠금 {formatCurrency(baseLocked, baseCurrency)})
              </span>
            )}
          </div>
        </div>

        {/* Right — action buttons */}
        <div className="flex gap-2">
          {actionButtons.map((btn) => (
            <button
              key={btn.label}
              className="flex items-center gap-1.5 rounded-xl bg-primary px-4 py-2.5 text-sm font-semibold text-primary-foreground shadow-sm transition-all hover:shadow-md hover:brightness-110 active:scale-[0.97]"
            >
              <btn.icon className="h-4 w-4" />
              {btn.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
