"use client";

import { useState } from "react";
import {
  AuthGate,
  BookingSheet,
  MobilityShell,
  RouteCanvas,
  StatusChip,
} from "@/modules/shared/ui";
import { useRentalCatalog } from "../hooks/useRentalCatalog";

export function RentScreen() {
  const {
    vehicles,
    bookings,
    selected,
    selectedId,
    setSelectedId,
    error,
    busy,
    book,
    pickup,
    returnCar,
  } = useRentalCatalog();

  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [pickupAddress, setPickupAddress] = useState("Bangalore Airport");

  return (
    <AuthGate>
      <MobilityShell active="/rent">
        <div className="relative mx-auto grid max-w-6xl lg:grid-cols-[1fr_420px]">
          <div className="relative min-h-[56vh] lg:min-h-[calc(100vh-8rem)]">
            <RouteCanvas
              className="absolute inset-0"
              title="Rental pickup"
              points={[
                {
                  lat: selected?.lat ?? 12.97,
                  lng: selected?.lng ?? 77.59,
                  label: selected?.name ?? "Fleet hub",
                },
                {
                  lat: (selected?.lat ?? 12.97) + 0.03,
                  lng: (selected?.lng ?? 77.59) + 0.02,
                  label: "Your area",
                },
              ]}
            />
            <div className="absolute inset-x-0 bottom-0 z-10 max-h-[42%] overflow-x-auto p-4">
              <div className="flex gap-3">
                {vehicles.map((v) => (
                  <button
                    key={v.id}
                    type="button"
                    onClick={() => setSelectedId(v.id)}
                    className={`min-w-[220px] rounded-2xl border p-4 text-left backdrop-blur transition ${
                      selectedId === v.id
                        ? "border-volt bg-ink-soft/95"
                        : "border-white/10 bg-ink/80 hover:border-white/25"
                    }`}
                  >
                    <p className="font-display text-lg text-white">{v.name}</p>
                    <p className="text-xs text-slate-400">
                      {v.brand} {v.model} · {v.seats ?? "—"} seats
                    </p>
                    <p className="mt-2 font-display text-volt-soft">
                      ₹{Number(v.pricePerDay ?? 0)}/day
                    </p>
                    <div className="mt-2">
                      <StatusChip status={v.status} />
                    </div>
                  </button>
                ))}
                {vehicles.length === 0 && (
                  <p className="rounded-2xl bg-ink/80 px-4 py-3 text-sm text-slate-400">
                    No rental vehicles listed yet. Owners can add fleet cars.
                  </p>
                )}
              </div>
            </div>
          </div>

          <div className="relative z-20 -mt-8 px-3 pb-8 lg:mt-0 lg:px-4 lg:py-6">
            <BookingSheet
              title="Rent a car"
              subtitle="Browse listings from vehicle-service, book via rental-service."
              className="w-full md:w-[420px]"
            >
              {error && (
                <p className="rounded-xl border border-rose-400/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-200">
                  {error}
                </p>
              )}

              <form
                className="space-y-3"
                onSubmit={async (e) => {
                  e.preventDefault();
                  await book({ startDate, endDate, pickupAddress });
                }}
              >
                <p className="text-sm text-slate-300">
                  Selected:{" "}
                  <span className="font-display text-white">
                    {selected?.name ?? "None"}
                  </span>
                </p>
                <label className="block text-xs text-slate-500">
                  Start date
                  <input
                    type="date"
                    required
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    className="mt-1 w-full rounded-xl border border-white/10 bg-ink px-3 py-2.5 text-sm"
                  />
                </label>
                <label className="block text-xs text-slate-500">
                  End date
                  <input
                    type="date"
                    required
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    className="mt-1 w-full rounded-xl border border-white/10 bg-ink px-3 py-2.5 text-sm"
                  />
                </label>
                <label className="block text-xs text-slate-500">
                  Pickup
                  <input
                    value={pickupAddress}
                    onChange={(e) => setPickupAddress(e.target.value)}
                    className="mt-1 w-full rounded-xl border border-white/10 bg-ink px-3 py-2.5 text-sm"
                  />
                </label>
                <button
                  type="submit"
                  disabled={busy || !selected}
                  className="w-full rounded-full bg-volt py-3 font-display text-sm font-semibold text-ink disabled:opacity-50"
                >
                  {busy ? "Booking…" : "Request booking"}
                </button>
              </form>

              <div className="space-y-2 border-t border-white/10 pt-4">
                <p className="text-xs uppercase tracking-wider text-slate-500">
                  My bookings
                </p>
                {bookings.map((b) => (
                  <div
                    key={b.id}
                    className="space-y-2 rounded-xl bg-white/5 px-3 py-3"
                  >
                    <div className="flex items-center justify-between">
                      <p className="text-sm">
                        #{b.id} · vehicle {b.vehicleId}
                      </p>
                      <StatusChip status={b.status} />
                    </div>
                    <p className="text-xs text-slate-400">
                      {b.startDate} → {b.endDate} · ₹{Number(b.totalAmount)}
                    </p>
                    <div className="flex gap-2">
                      {b.status === "CONFIRMED" && (
                        <button
                          type="button"
                          className="rounded-full bg-volt px-3 py-1 text-xs font-semibold text-ink"
                          onClick={() => pickup(b.id)}
                        >
                          Pickup
                        </button>
                      )}
                      {b.status === "PICKED_UP" && (
                        <button
                          type="button"
                          className="rounded-full bg-emerald-400 px-3 py-1 text-xs font-semibold text-ink"
                          onClick={() => returnCar(b.id)}
                        >
                          Return
                        </button>
                      )}
                    </div>
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
