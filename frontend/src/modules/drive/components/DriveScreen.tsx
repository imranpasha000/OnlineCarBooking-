"use client";

import {
  AuthGate,
  BookingSheet,
  MobilityShell,
  RouteCanvas,
  StatusChip,
} from "@/modules/shared/ui";
import { useDriverSession } from "../hooks/useDriverSession";

export function DriveScreen() {
  const {
    online,
    pending,
    active,
    lat,
    lng,
    setLat,
    setLng,
    error,
    busy,
    goOnline,
    accept,
    start,
    complete,
  } = useDriverSession();

  return (
    <AuthGate>
      <MobilityShell active="/drive">
        <div className="relative mx-auto grid max-w-6xl lg:grid-cols-[1fr_400px]">
          <RouteCanvas
            className="min-h-[56vh] lg:min-h-[calc(100vh-8rem)]"
            title="Driver view"
            live={online}
            points={[
              { lat, lng, label: "You" },
              active
                ? {
                    lat: active.pickupLat,
                    lng: active.pickupLng,
                    label: "Rider pickup",
                  }
                : { lat: lat + 0.02, lng: lng + 0.02, label: "Zone" },
            ]}
          />
          <div className="relative z-20 -mt-8 px-3 pb-8 lg:mt-0 lg:px-4 lg:py-6">
            <BookingSheet
              title="Drive"
              subtitle="Go online, accept offers, complete the trip."
              className="w-full md:w-[400px]"
            >
              {error && (
                <p className="rounded-xl border border-rose-400/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-200">
                  {error}
                </p>
              )}

              <div className="flex items-center justify-between rounded-2xl border border-white/10 bg-ink-mist/50 p-4">
                <div>
                  <p className="font-display text-lg">
                    {online ? "You're online" : "You're offline"}
                  </p>
                  <p className="text-xs text-slate-400">
                    Location pings matching-service while online
                  </p>
                </div>
                <button
                  type="button"
                  disabled={busy}
                  onClick={() => goOnline(!online)}
                  className={`rounded-full px-4 py-2 text-sm font-semibold ${
                    online
                      ? "bg-emerald-400 text-ink"
                      : "bg-white/10 text-white hover:bg-white/15"
                  }`}
                >
                  {online ? "Go offline" : "Go online"}
                </button>
              </div>

              <div className="grid grid-cols-2 gap-2">
                <label className="text-xs text-slate-500">
                  Lat
                  <input
                    className="mt-1 w-full rounded-xl border border-white/10 bg-ink px-3 py-2 text-sm"
                    value={lat}
                    onChange={(e) => setLat(Number(e.target.value))}
                    type="number"
                    step="0.0001"
                  />
                </label>
                <label className="text-xs text-slate-500">
                  Lng
                  <input
                    className="mt-1 w-full rounded-xl border border-white/10 bg-ink px-3 py-2 text-sm"
                    value={lng}
                    onChange={(e) => setLng(Number(e.target.value))}
                    type="number"
                    step="0.0001"
                  />
                </label>
              </div>

              {active && (
                <div className="space-y-3 rounded-2xl border border-volt/25 bg-ink-mist/60 p-4">
                  <div className="flex items-center justify-between">
                    <p className="font-display">Active #{active.id}</p>
                    <StatusChip status={active.status} />
                  </div>
                  <p className="text-sm text-slate-300">
                    {active.pickupAddress} → {active.dropoffAddress}
                  </p>
                  <div className="flex gap-2">
                    {active.status === "ASSIGNED" && (
                      <button
                        type="button"
                        disabled={busy}
                        onClick={() => start(active.id)}
                        className="flex-1 rounded-full bg-volt py-2.5 text-sm font-semibold text-ink"
                      >
                        Start trip
                      </button>
                    )}
                    {active.status === "STARTED" && (
                      <button
                        type="button"
                        disabled={busy}
                        onClick={() => complete(active.id)}
                        className="flex-1 rounded-full bg-emerald-400 py-2.5 text-sm font-semibold text-ink"
                      >
                        Complete
                      </button>
                    )}
                  </div>
                </div>
              )}

              <div className="space-y-2">
                <p className="text-xs uppercase tracking-wider text-slate-500">
                  Incoming requests
                </p>
                {pending.length === 0 && (
                  <p className="text-sm text-slate-500">
                    No pending rides nearby.
                  </p>
                )}
                {pending.map((trip) => (
                  <div
                    key={trip.id}
                    className="flex items-center justify-between gap-3 rounded-xl bg-white/5 px-3 py-3"
                  >
                    <div className="min-w-0">
                      <p className="truncate text-sm">
                        {trip.pickupAddress} → {trip.dropoffAddress}
                      </p>
                      <p className="text-xs text-volt-soft">
                        ₹{Number(trip.fare ?? 0).toFixed(0)} ·{" "}
                        {Number(trip.distanceKm ?? 0).toFixed(1)} km
                      </p>
                    </div>
                    <button
                      type="button"
                      disabled={busy || !online}
                      onClick={() => accept(trip.id)}
                      className="shrink-0 rounded-full bg-volt px-3 py-1.5 text-xs font-semibold text-ink disabled:opacity-40"
                    >
                      Accept
                    </button>
                  </div>
                ))}
              </div>
            </BookingSheet>
          </div>
        </div>
      </MobilityShell>
    </AuthGate>
  );
}
