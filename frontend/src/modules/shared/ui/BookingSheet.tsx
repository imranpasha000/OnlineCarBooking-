"use client";

import { cn } from "@/lib/utils";

/** Floating booking panel — Uber-like sheet, BoxCars ink/volt styling. */
export function BookingSheet({
  children,
  className,
  title,
  subtitle,
}: {
  children: React.ReactNode;
  className?: string;
  title: string;
  subtitle?: string;
}) {
  return (
    <aside
      className={cn(
        "animate-sheet-up rounded-t-3xl border border-white/10 bg-ink-soft/95 text-white shadow-2xl backdrop-blur-xl md:rounded-3xl",
        className
      )}
    >
      <div className="mx-auto mb-3 mt-3 h-1 w-10 rounded-full bg-white/20 md:hidden" />
      <div className="border-b border-white/10 px-5 pb-4 pt-2 md:pt-5">
        <h2 className="font-display text-2xl font-semibold tracking-tight">
          {title}
        </h2>
        {subtitle && (
          <p className="mt-1 text-sm text-slate-400">{subtitle}</p>
        )}
      </div>
      <div className="max-h-[70vh] space-y-4 overflow-y-auto px-5 py-4 md:max-h-none">
        {children}
      </div>
    </aside>
  );
}
