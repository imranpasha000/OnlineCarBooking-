"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { AuthGate, MobilityShell, StatusChip } from "@/modules/shared/ui";
import { useAuth } from "@/context/AuthContext";
import { usersApi, type UserProfile } from "@/lib/api/users";

export default function AdminPage() {
  const { user, isAuthenticated } = useAuth();
  const [users, setUsers] = useState<UserProfile[]>([]);
  const [error, setError] = useState<string | null>(null);
  const isAdmin = user?.roles?.includes("ROLE_ADMIN") ?? false;

  useEffect(() => {
    if (!isAuthenticated || !isAdmin) return;
    usersApi
      .listAll()
      .then(setUsers)
      .catch((err) =>
        setError(err instanceof Error ? err.message : "Failed to load users")
      );
  }, [isAuthenticated, isAdmin]);

  return (
    <AuthGate>
      <MobilityShell active="/admin">
        <div className="mx-auto max-w-5xl px-4 py-10">
          <h1 className="font-display text-3xl text-white">Admin</h1>
          <p className="mt-1 text-slate-400">
            Moderate users, oversee trips & rentals, configure pricing later.
          </p>

          {!isAdmin && (
            <div className="mt-8 rounded-2xl border border-amber-400/30 bg-amber-500/10 p-6 text-amber-100">
              Access denied — need <code>ROLE_ADMIN</code>.{" "}
              <Link href="/ride" className="underline">
                Back to ride
              </Link>
            </div>
          )}

          {error && (
            <p className="mt-4 text-sm text-rose-300">{error}</p>
          )}

          {isAdmin && (
            <div className="mt-8 space-y-3">
              <p className="text-xs uppercase tracking-wider text-slate-500">
                Users ({users.length})
              </p>
              {users.map((u) => (
                <div
                  key={u.id}
                  className="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-white/10 bg-ink-soft px-4 py-3"
                >
                  <div>
                    <p className="font-display text-white">
                      {u.displayName || u.username || u.email}
                    </p>
                    <p className="text-xs text-slate-400">{u.email}</p>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {(u.roles ?? []).map((r) => (
                      <StatusChip key={r} status={r.replace("ROLE_", "")} />
                    ))}
                    {u.kycVerified && <StatusChip status="CONFIRMED" />}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </MobilityShell>
    </AuthGate>
  );
}
