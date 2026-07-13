"use client";

import { cn } from "@/lib/utils";

export type RoutePoint = {
  lat: number;
  lng: number;
  label?: string;
};

type RouteCanvasProps = {
  points: RoutePoint[];
  className?: string;
  title?: string;
  live?: boolean;
};

/** Full-bleed stylized route plane — unique BoxCars mobility canvas (not a map SDK). */
export function RouteCanvas({
  points,
  className,
  title = "Live route",
  live = false,
}: RouteCanvasProps) {
  const from = points[0];
  const to = points[1] ?? points[0];

  return (
    <div
      className={cn(
        "relative isolate min-h-[420px] overflow-hidden bg-ink text-white",
        className
      )}
    >
      <div
        className="pointer-events-none absolute inset-0 opacity-90"
        style={{
          background:
            "radial-gradient(ellipse 80% 60% at 20% 30%, rgba(0,194,255,0.22), transparent 55%), radial-gradient(ellipse 70% 50% at 80% 70%, rgba(0,144,191,0.18), transparent 50%), linear-gradient(160deg, #0B1220 0%, #121A2B 45%, #0B1220 100%)",
        }}
      />

      <svg
        className="pointer-events-none absolute inset-0 h-full w-full opacity-[0.12]"
        aria-hidden
      >
        <defs>
          <pattern id="grid" width="48" height="48" patternUnits="userSpaceOnUse">
            <path
              d="M 48 0 L 0 0 0 48"
              fill="none"
              stroke="#5AD7FF"
              strokeWidth="0.5"
            />
          </pattern>
        </defs>
        <rect width="100%" height="100%" fill="url(#grid)" />
      </svg>

      <div className="relative z-10 flex h-full min-h-[420px] flex-col p-5 md:p-8">
        <div className="flex items-center justify-between gap-3">
          <p className="font-display text-xs font-semibold uppercase tracking-[0.2em] text-volt-soft">
            BoxCars · {title}
          </p>
          {live && (
            <span className="inline-flex items-center gap-2 rounded-full border border-volt/30 bg-ink-mist/80 px-3 py-1 text-xs text-volt-soft">
              <span className="relative flex h-2 w-2">
                <span className="absolute inline-flex h-full w-full animate-pulse-ring rounded-full bg-volt" />
                <span className="relative inline-flex h-2 w-2 rounded-full bg-volt" />
              </span>
              Tracking
            </span>
          )}
        </div>

        <div className="relative mx-auto mt-10 w-full max-w-lg flex-1">
          <svg viewBox="0 0 320 180" className="h-auto w-full" aria-hidden>
            <path
              d="M40 140 C 90 40, 220 40, 280 40"
              fill="none"
              stroke="#00C2FF"
              strokeWidth="3"
              strokeDasharray="8 6"
              className="animate-route-dash opacity-80"
            />
            <circle cx="40" cy="140" r="10" fill="#00C2FF" />
            <circle cx="280" cy="40" r="10" fill="#F8FAFC" />
          </svg>

          <div className="mt-6 grid gap-3 sm:grid-cols-2">
            {from && (
              <div className="rounded-2xl border border-white/10 bg-ink-soft/80 px-4 py-3 backdrop-blur">
                <p className="text-[11px] uppercase tracking-wider text-volt-soft">
                  {from.label || "Pickup"}
                </p>
                <p className="mt-1 font-display text-sm text-white">
                  {from.lat.toFixed(4)}, {from.lng.toFixed(4)}
                </p>
              </div>
            )}
            {to && (
              <div className="rounded-2xl border border-white/10 bg-ink-soft/80 px-4 py-3 backdrop-blur">
                <p className="text-[11px] uppercase tracking-wider text-slate-400">
                  {to.label || "Dropoff"}
                </p>
                <p className="mt-1 font-display text-sm text-white">
                  {to.lat.toFixed(4)}, {to.lng.toFixed(4)}
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
