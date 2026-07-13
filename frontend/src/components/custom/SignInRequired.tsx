"use client";

import Link from "next/link";
import { useAuth } from "@/context/AuthContext";

export default function SignInRequired() {
  const { loading } = useAuth();

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-16 text-center text-slate-600">
        Loading…
      </div>
    );
  }

  return (
    <div className="container mx-auto max-w-lg px-4 py-16 text-center">
      <h1 className="mb-3 text-2xl font-semibold text-slate-900">
        Please sign in
      </h1>
      <p className="mb-6 text-slate-600">
        You need an account to use this BoxCars feature.
      </p>
      <Link
        href="/sign-in"
        className="inline-flex rounded-md bg-[#05a8f3] px-5 py-2.5 text-sm font-medium text-white hover:bg-[#0490d1]"
      >
        Sign In
      </Link>
    </div>
  );
}
