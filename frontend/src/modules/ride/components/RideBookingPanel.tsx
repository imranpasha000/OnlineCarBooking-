"use client";

import { useState } from "react";
import { BookingSheet, StatusChip } from "@/modules/shared/ui";
import { useRideTrips } from "../hooks/useRideTrips";

const defaults = {
  pickupAddress: "MG Road, Bangalore",
  pickupLat: "12.9716",
  pickupLng: "77.5946",
  dropoffAddress: "Koramangala 5th Block",
  dropoffLat: "12.9352",
  dropoffLng: "77.6245",
};

export function RideBookingPanel({
  onRouteChange,
}: {
  onRouteChange: (route: {
    pickupLat: number;
    pickupLng: number;
    dropoffLat: number;
    dropoffLng: number;
  }) => void;
}) {
  const { activeTrip, trips, error, busy, requestRide, cancelTrip } =
    useRideTrips();
  const [form, setForm] = useState(defaults);

  function update<K extends keyof typeof defaults>(key: K, value: string) {
    const next = { ...form, [key]: value };
    setForm(next);
    onRouteChange({
      pickupLat: Number(next.pickupLat) || 12.97,
      pickupLng: Number(next.pickupLng) || 77.59,
      dropoffLat: Number(next.dropoffLat) || 12.93,
      dropoffLng: Number(next.dropoffLng) || 77.62,
    });
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    await requestRide({
      pickupAddress: form.pickupAddress,
      pickupLat: Number(form.pickupLat),
      pickupLng: Number(form.pickupLng),
      dropoffAddress: form.dropoffAddress,
      dropoffLat: Number(form.dropoffLat),
      dropoffLng: Number(form.dropoffLng),
    });
  }

  return (
    <BookingSheet
      title="Where to?"
      subtitle="Request a ride — same flow as the driver side, mirrored."
      className="w-full md:w-[400px]"
    >
      {error && (
        <p className="rounded-xl border border-rose-400/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-200">
          {error}
        </p>
      )}

      {activeTrip ? (
        <div className="space-y-3 rounded-2xl border border-volt/20 bg-ink-mist/60 p-4">
          <div className="flex items-center justify-between gap-2">
            <p className="font-display text-lg">Trip #{activeTrip.id}</p>
            <StatusChip status={activeTrip.status} />
          </div>
          <p className="text-sm text-slate-300">
            {activeTrip.pickupAddress} → {activeTrip.dropoffAddress}
          </p>
          {activeTrip.fare != null && (
            <p className="font-display text-volt-soft">
              Est. ₹{Number(activeTrip.fare).toFixed(0)}
            </p>
          )}
          {activeTrip.status === "REQUESTED" && (
            <button
              type="button"
              disabled={busy}
              onClick={() => cancelTrip(activeTrip.id)}
              className="w-full rounded-full border border-rose-400/40 py-2.5 text-sm text-rose-200 hover:bg-rose-500/10"
            >
              Cancel request
            </button>
          )}
        </div>
      ) : (
        <form className="space-y-3" onSubmit={onSubmit}>
          <Field
            label="Pickup"
            value={form.pickupAddress}
            onChange={(v) => update("pickupAddress", v)}
          />
          <div className="grid grid-cols-2 gap-2">
            <Field
              label="Lat"
              value={form.pickupLat}
              onChange={(v) => update("pickupLat", v)}
            />
            <Field
              label="Lng"
              value={form.pickupLng}
              onChange={(v) => update("pickupLng", v)}
            />
          </div>
          <Field
            label="Dropoff"
            value={form.dropoffAddress}
            onChange={(v) => update("dropoffAddress", v)}
          />
          <div className="grid grid-cols-2 gap-2">
            <Field
              label="Lat"
              value={form.dropoffLat}
              onChange={(v) => update("dropoffLat", v)}
            />
            <Field
              label="Lng"
              value={form.dropoffLng}
              onChange={(v) => update("dropoffLng", v)}
            />
          </div>
          <button
            type="submit"
            disabled={busy}
            className="w-full rounded-full bg-volt py-3 font-display text-sm font-semibold text-ink transition hover:bg-volt-soft disabled:opacity-60"
          >
            {busy ? "Finding a driver…" : "Confirm ride"}
          </button>
        </form>
      )}

      <div className="space-y-2 border-t border-white/10 pt-4">
        <p className="text-xs uppercase tracking-wider text-slate-500">
          Recent
        </p>
        {trips.length === 0 && (
          <p className="text-sm text-slate-500">No trips yet.</p>
        )}
        {trips.slice(0, 5).map((t) => (
          <div
            key={t.id}
            className="flex items-center justify-between gap-2 rounded-xl bg-white/5 px-3 py-2"
          >
            <div className="min-w-0">
              <p className="truncate text-sm">
                {t.pickupAddress} → {t.dropoffAddress}
              </p>
              <p className="text-xs text-slate-500">#{t.id}</p>
            </div>
            <StatusChip status={t.status} />
          </div>
        ))}
      </div>
    </BookingSheet>
  );
}

function Field({
  label,
  value,
  onChange,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <label className="block">
      <span className="mb-1 block text-[11px] uppercase tracking-wider text-slate-500">
        {label}
      </span>
      <input
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-full rounded-xl border border-white/10 bg-ink px-3 py-2.5 text-sm text-white outline-none ring-volt focus:ring-1"
      />
    </label>
  );
}
