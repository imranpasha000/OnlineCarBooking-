"use client";

import {
  AuthGate,
  BookingSheet,
  MobilityShell,
  RouteCanvas,
  StatusChip,
} from "@/modules/shared/ui";
import { useOwnerFleet } from "../hooks/useOwnerFleet";
import type { VehicleType } from "@/lib/api/vehicles";

export function OwnerScreen() {
  const {
    vehicles,
    pending,
    bookings,
    form,
    setForm,
    error,
    busy,
    addVehicle,
    confirm,
  } = useOwnerFleet();

  return (
    <AuthGate>
      <MobilityShell active="/owner">
        <div className="relative mx-auto grid max-w-6xl lg:grid-cols-[1fr_420px]">
          <RouteCanvas
            className="min-h-[40vh] lg:min-h-[calc(100vh-8rem)]"
            title="Owner fleet"
            points={
              vehicles[0]
                ? [
                    {
                      lat: vehicles[0].lat ?? 12.97,
                      lng: vehicles[0].lng ?? 77.59,
                      label: vehicles[0].name,
                    },
                    {
                      lat: (vehicles[0].lat ?? 12.97) + 0.04,
                      lng: (vehicles[0].lng ?? 77.59) - 0.02,
                      label: "Service area",
                    },
                  ]
                : [
                    { lat: 12.97, lng: 77.59, label: "Hub" },
                    { lat: 13.0, lng: 77.62, label: "Zone" },
                  ]
            }
          />

          <div className="relative z-20 -mt-8 space-y-4 px-3 pb-8 lg:mt-0 lg:px-4 lg:py-6">
            <BookingSheet
              title="Your fleet"
              subtitle="List cars on vehicle-service · confirm rentals"
              className="w-full md:w-[420px]"
            >
              {error && (
                <p className="rounded-xl border border-rose-400/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-200">
                  {error}
                </p>
              )}

              <div className="space-y-2">
                {vehicles.map((v) => (
                  <div
                    key={v.id}
                    className="flex items-center justify-between rounded-xl bg-white/5 px-3 py-3"
                  >
                    <div>
                      <p className="font-display text-sm">{v.name}</p>
                      <p className="text-xs text-slate-400">
                        {v.type} · ₹{Number(v.pricePerDay ?? 0)}/day
                      </p>
                    </div>
                    <StatusChip status={v.status} />
                  </div>
                ))}
                {vehicles.length === 0 && (
                  <p className="text-sm text-slate-500">No cars listed yet.</p>
                )}
              </div>

              <form
                className="space-y-2 border-t border-white/10 pt-4"
                onSubmit={(e) => {
                  e.preventDefault();
                  void addVehicle();
                }}
              >
                <p className="text-xs uppercase tracking-wider text-slate-500">
                  Add vehicle
                </p>
                <input
                  required
                  placeholder="Name"
                  value={form.name}
                  onChange={(e) => setForm({ ...form, name: e.target.value })}
                  className="w-full rounded-xl border border-white/10 bg-ink px-3 py-2 text-sm"
                />
                <div className="grid grid-cols-2 gap-2">
                  <input
                    placeholder="Brand"
                    value={form.brand ?? ""}
                    onChange={(e) =>
                      setForm({ ...form, brand: e.target.value })
                    }
                    className="rounded-xl border border-white/10 bg-ink px-3 py-2 text-sm"
                  />
                  <input
                    placeholder="Model"
                    value={form.model ?? ""}
                    onChange={(e) =>
                      setForm({ ...form, model: e.target.value })
                    }
                    className="rounded-xl border border-white/10 bg-ink px-3 py-2 text-sm"
                  />
                </div>
                <div className="grid grid-cols-2 gap-2">
                  <select
                    value={form.type}
                    onChange={(e) =>
                      setForm({
                        ...form,
                        type: e.target.value as VehicleType,
                      })
                    }
                    className="rounded-xl border border-white/10 bg-ink px-3 py-2 text-sm"
                  >
                    <option value="RENTAL">RENTAL</option>
                    <option value="RIDE">RIDE</option>
                    <option value="BOTH">BOTH</option>
                  </select>
                  <input
                    type="number"
                    placeholder="₹ / day"
                    value={form.pricePerDay ?? ""}
                    onChange={(e) =>
                      setForm({
                        ...form,
                        pricePerDay: Number(e.target.value),
                      })
                    }
                    className="rounded-xl border border-white/10 bg-ink px-3 py-2 text-sm"
                  />
                </div>
                <button
                  type="submit"
                  disabled={busy}
                  className="w-full rounded-full bg-volt py-2.5 font-display text-sm font-semibold text-ink"
                >
                  {busy ? "Saving…" : "List vehicle"}
                </button>
              </form>

              <div className="space-y-2 border-t border-white/10 pt-4">
                <p className="text-xs uppercase tracking-wider text-slate-500">
                  Pending confirmations
                </p>
                {pending.map((b) => (
                  <div
                    key={b.id}
                    className="flex items-center justify-between gap-2 rounded-xl bg-white/5 px-3 py-3"
                  >
                    <div>
                      <p className="text-sm">Booking #{b.id}</p>
                      <p className="text-xs text-slate-400">
                        {b.startDate} → {b.endDate}
                      </p>
                    </div>
                    <button
                      type="button"
                      disabled={busy}
                      onClick={() => confirm(b.id)}
                      className="rounded-full bg-volt px-3 py-1.5 text-xs font-semibold text-ink"
                    >
                      Confirm
                    </button>
                  </div>
                ))}
                {pending.length === 0 && (
                  <p className="text-sm text-slate-500">No pending requests.</p>
                )}
              </div>

              <div className="space-y-2 border-t border-white/10 pt-4">
                <p className="text-xs uppercase tracking-wider text-slate-500">
                  Owner bookings
                </p>
                {bookings.slice(0, 6).map((b) => (
                  <div
                    key={b.id}
                    className="flex items-center justify-between rounded-xl bg-white/5 px-3 py-2"
                  >
                    <p className="text-sm">#{b.id}</p>
                    <StatusChip status={b.status} />
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
