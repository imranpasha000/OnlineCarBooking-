"use client";

import Link from "next/link";
import { cn } from "@/lib/utils";

const tabs = [
  { href: "/ride", label: "Ride" },
  { href: "/drive", label: "Drive" },
  { href: "/rent", label: "Rent" },
  { href: "/owner", label: "Fleet" },
  { href: "/admin", label: "Admin" },
] as const;

export function MobilityShell({
  children,
  active,
  className,
}: {
  children: React.ReactNode;
  active: (typeof tabs)[number]["href"];
  className?: string;
}) {
  return (
    <div className={cn("min-h-[calc(100vh-4rem)] bg-ink font-body text-white", className)}>
      <div className="border-b border-white/10 bg-ink-soft/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl gap-1 overflow-x-auto px-4 py-2">
          {tabs.map((tab) => (
            <Link
              key={tab.href}
              href={tab.href}
              className={cn(
                "rounded-full px-4 py-2 font-display text-sm font-medium transition",
                active === tab.href
                  ? "bg-volt text-ink"
                  : "text-slate-300 hover:bg-white/5 hover:text-white"
              )}
            >
              {tab.label}
            </Link>
          ))}
        </div>
      </div>
      {children}
    </div>
  );
}
