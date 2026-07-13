"use client";

import Link from "next/link";
import { useAuth } from "@/context/AuthContext";

export function AuthGate({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center bg-ink text-slate-400">
        Loading your session…
      </div>
    );
  }

  if (!isAuthenticated) {
    return (
      <div className="flex min-h-[60vh] flex-col items-center justify-center gap-4 bg-ink px-6 text-center">
        <p className="font-display text-3xl text-white">Sign in to continue</p>
        <p className="max-w-md text-slate-400">
          Ride, drive, and rental flows use the BoxCars gateway APIs. Create an
          account or sign in to start.
        </p>
        <Link
          href="/sign-in"
          className="rounded-full bg-volt px-6 py-3 font-display text-sm font-semibold text-ink transition hover:bg-volt-soft"
        >
          Sign in
        </Link>
      </div>
    );
  }

  return <>{children}</>;
}
