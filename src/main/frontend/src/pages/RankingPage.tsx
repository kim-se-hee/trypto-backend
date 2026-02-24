import { useState, useMemo } from "react";
import { useSearchParams } from "react-router-dom";
import { Header } from "@/components/layout/Header";
import { Trophy, ChevronDown, Lock } from "lucide-react";
import { CoinIcon } from "@/components/market/CoinIcon";
import { rankingData } from "@/mocks/ranking";
import type { RankingPeriod, RankingEntry } from "@/mocks/ranking";
import { cn } from "@/lib/utils";

const PERIOD_TABS: { key: RankingPeriod; label: string }[] = [
  { key: "daily", label: "일간" },
  { key: "weekly", label: "주간" },
  { key: "monthly", label: "월간" },
];

const MEDAL_COLORS: Record<number, { bg: string; text: string; ring: string }> = {
  1: { bg: "bg-amber-400", text: "text-amber-900", ring: "ring-amber-300" },
  2: { bg: "bg-gray-300", text: "text-gray-700", ring: "ring-gray-200" },
  3: { bg: "bg-amber-600", text: "text-amber-100", ring: "ring-amber-500" },
};

function RankBadge({ rank }: { rank: number }) {
  const medal = MEDAL_COLORS[rank];
  if (medal) {
    return (
      <span
        className={cn(
          "inline-flex h-7 w-7 items-center justify-center rounded-full text-xs font-extrabold ring-2",
          medal.bg,
          medal.text,
          medal.ring,
        )}
      >
        {rank}
      </span>
    );
  }
  return (
    <span className="inline-flex h-7 w-7 items-center justify-center rounded-full text-xs font-bold text-muted-foreground">
      {rank}
    </span>
  );
}

function PortfolioPanel({ entry }: { entry: RankingEntry }) {
  if (!entry.portfolioPublic) {
    return (
      <div className="flex items-center gap-2 px-4 py-5 text-xs text-muted-foreground">
        <Lock className="h-3.5 w-3.5" />
        비공개 포트폴리오입니다.
      </div>
    );
  }

  return (
    <div className="px-4 pb-4 pt-2">
      <p className="mb-2.5 text-[11px] font-medium text-muted-foreground">보유 코인 비중</p>
      <div className="space-y-2">
        {entry.portfolio.map((item) => {
          const pct = item.ratio * 100;
          return (
            <div key={item.coinSymbol} className="flex items-center gap-3">
              <CoinIcon symbol={item.coinSymbol} size={24} />
              <div className="min-w-0 flex-1">
                <div className="mb-1 flex items-center justify-between">
                  <span className="text-xs font-semibold">{item.coinSymbol}</span>
                  <span className="font-mono text-xs font-semibold tabular-nums">
                    {pct.toFixed(1)}%
                  </span>
                </div>
                <div className="h-1.5 w-full overflow-hidden rounded-full bg-secondary">
                  <div
                    className="h-full rounded-full bg-primary transition-all duration-500"
                    style={{ width: `${pct}%` }}
                  />
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

function RankingRow({
  entry,
  isExpanded,
  onToggle,
}: {
  entry: RankingEntry;
  isExpanded: boolean;
  onToggle: () => void;
}) {
  const isPositive = entry.profitRate >= 0;

  return (
    <div
      className={cn(
        "overflow-hidden rounded-2xl bg-card transition-shadow",
        isExpanded
          ? "shadow-card-active"
          : "shadow-card hover:shadow-card-hover",
      )}
    >
      {/* Clickable row */}
      <button
        onClick={onToggle}
        className="flex w-full items-center gap-4 px-4 py-3.5 text-left transition-colors hover:bg-primary/[0.03]"
      >
        <RankBadge rank={entry.rank} />

        <div className="min-w-0 flex-1">
          <span className="text-sm font-semibold">{entry.nickname}</span>
          <span className="ml-2 text-[11px] text-muted-foreground">
            {entry.tradeCount}회 거래
          </span>
        </div>

        <span
          className={cn(
            "font-mono text-sm font-bold tabular-nums",
            isPositive ? "text-positive" : "text-negative",
          )}
        >
          {isPositive ? "+" : ""}
          {entry.profitRate.toFixed(2)}%
        </span>

        <ChevronDown
          className={cn(
            "h-4 w-4 shrink-0 text-muted-foreground/50 transition-transform duration-200",
            isExpanded && "rotate-180",
          )}
        />
      </button>

      {/* Expandable portfolio panel */}
      <div
        className={cn(
          "grid transition-[grid-template-rows] duration-300 ease-in-out",
          isExpanded ? "grid-rows-[1fr]" : "grid-rows-[0fr]",
        )}
      >
        <div className="overflow-hidden">
          {isExpanded && (
            <div className="border-t border-border/60">
              <PortfolioPanel entry={entry} />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export function RankingPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const period = (searchParams.get("period") ?? "daily") as RankingPeriod;
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const entries = useMemo(() => rankingData[period] ?? rankingData.daily, [period]);

  const handleToggle = (userId: number) => {
    setExpandedId((prev) => (prev === userId ? null : userId));
  };

  const periodLabel = PERIOD_TABS.find((t) => t.key === period)?.label ?? "";

  return (
    <div className="min-h-screen bg-background">
      <Header />

      {/* Hero section */}
      <section className="bg-gradient-to-r from-primary/8 via-chart-4/6 to-primary/4 pb-8 pt-8">
        <div className="mx-auto max-w-6xl px-4">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <div className="mb-1 flex items-center gap-2.5">
                <Trophy className="h-7 w-7 text-primary" />
                <h1 className="text-3xl font-extrabold tracking-tight">랭킹</h1>
              </div>
              <p className="mt-1.5 text-sm font-medium text-muted-foreground">
                {periodLabel} 수익률 기준 · 상위 100명
              </p>
            </div>

            {/* Period tabs */}
            <div className="flex gap-1.5 rounded-xl bg-white/60 p-1 backdrop-blur-sm">
              {PERIOD_TABS.map((tab) => (
                <button
                  key={tab.key}
                  onClick={() => {
                    setSearchParams({ period: tab.key });
                    setExpandedId(null);
                  }}
                  className={cn(
                    "rounded-lg px-4 py-1.5 text-sm font-semibold transition-all",
                    period === tab.key
                      ? "bg-primary text-primary-foreground shadow-sm"
                      : "text-muted-foreground hover:text-foreground",
                  )}
                >
                  {tab.label}
                </button>
              ))}
            </div>
          </div>
        </div>
      </section>

      <main className="mx-auto max-w-2xl px-4 py-6">
        {/* All 100 rankings — unified expand/collapse rows */}
        <div className="space-y-2">
          {entries.map((entry) => (
            <RankingRow
              key={entry.userId}
              entry={entry}
              isExpanded={expandedId === entry.userId}
              onToggle={() => handleToggle(entry.userId)}
            />
          ))}
        </div>

        {/* Footer */}
        <p className="mt-3 text-[11px] text-muted-foreground/60">
          * 모의투자 데이터입니다. 배치 집계 기준.
        </p>
      </main>
    </div>
  );
}
