"use client";

import { cn } from "@/lib/utils";

const toneMap: Record<string, string> = {
  REQUESTED: "bg-amber-400/15 text-amber-300 border-amber-400/30",
  OFFERED: "bg-sky-400/15 text-sky-300 border-sky-400/30",
  ASSIGNED: "bg-volt/15 text-volt-soft border-volt/30",
  STARTED: "bg-emerald-400/15 text-emerald-300 border-emerald-400/30",
  COMPLETED: "bg-slate-400/15 text-slate-300 border-slate-400/30",
  CANCELLED: "bg-rose-400/15 text-rose-300 border-rose-400/30",
  CONFIRMED: "bg-volt/15 text-volt-soft border-volt/30",
  PICKED_UP: "bg-emerald-400/15 text-emerald-300 border-emerald-400/30",
  RETURNED: "bg-slate-400/15 text-slate-300 border-slate-400/30",
  AVAILABLE: "bg-emerald-400/15 text-emerald-300 border-emerald-400/30",
  UNAVAILABLE: "bg-rose-400/15 text-rose-300 border-rose-400/30",
  BOOKED: "bg-amber-400/15 text-amber-300 border-amber-400/30",
};

export function StatusChip({
  status,
  className,
}: {
  status: string;
  className?: string;
}) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full border px-2.5 py-0.5 text-[11px] font-semibold uppercase tracking-wide",
        toneMap[status] ?? "bg-white/10 text-white border-white/20",
        className
      )}
    >
      {status.replaceAll("_", " ")}
    </span>
  );
}
