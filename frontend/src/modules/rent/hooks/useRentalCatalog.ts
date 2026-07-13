"use client";

import { useCallback, useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { rentalsApi, type RentalBooking } from "@/lib/api/rentals";
import { vehiclesApi, type Vehicle } from "@/lib/api/vehicles";

export function useRentalCatalog() {
  const { user, isAuthenticated } = useAuth();
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [bookings, setBookings] = useState<RentalBooking[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const refresh = useCallback(async () => {
    if (!isAuthenticated) return;
    setError(null);
    try {
      const [list, mine] = await Promise.all([
        vehiclesApi.search({ type: "RENTAL", status: "AVAILABLE" }),
        rentalsApi.me(),
      ]);
      setVehicles(list);
      setBookings(mine.filter((b) => b.customerId === user?.userId));
      setSelectedId((prev) => prev ?? list[0]?.id ?? null);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load fleet");
    }
  }, [isAuthenticated, user?.userId]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const selected = vehicles.find((v) => v.id === selectedId) ?? null;

  const book = async (payload: {
    startDate: string;
    endDate: string;
    pickupAddress: string;
  }) => {
    if (!selected) throw new Error("Select a vehicle");
    setBusy(true);
    setError(null);
    try {
      await rentalsApi.book({
        vehicleId: selected.id,
        ownerId: selected.ownerId,
        startDate: payload.startDate,
        endDate: payload.endDate,
        pickupAddress: payload.pickupAddress,
        dailyRate: Number(selected.pricePerDay ?? 0),
      });
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Booking failed");
      throw err;
    } finally {
      setBusy(false);
    }
  };

  const pickup = async (id: number) => {
    setBusy(true);
    try {
      await rentalsApi.pickup(id);
      await refresh();
    } finally {
      setBusy(false);
    }
  };

  const returnCar = async (id: number) => {
    setBusy(true);
    try {
      await rentalsApi.return(id);
      await refresh();
    } finally {
      setBusy(false);
    }
  };

  return {
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
    refresh,
  };
}
