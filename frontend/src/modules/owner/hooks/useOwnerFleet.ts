"use client";

import { useCallback, useEffect, useState } from "react";
import { useAuth } from "@/context/AuthContext";
import { rentalsApi, type RentalBooking } from "@/lib/api/rentals";
import {
  vehiclesApi,
  type CreateVehiclePayload,
  type Vehicle,
} from "@/lib/api/vehicles";

const emptyForm: CreateVehiclePayload = {
  name: "",
  brand: "",
  model: "",
  plateNumber: "",
  type: "RENTAL",
  pricePerDay: 1500,
  seats: 5,
  fuelType: "Petrol",
  transmission: "Automatic",
  lat: 12.97,
  lng: 77.59,
};

export function useOwnerFleet() {
  const { user, isAuthenticated } = useAuth();
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [pending, setPending] = useState<RentalBooking[]>([]);
  const [bookings, setBookings] = useState<RentalBooking[]>([]);
  const [form, setForm] = useState<CreateVehiclePayload>(emptyForm);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const refresh = useCallback(async () => {
    if (!isAuthenticated) return;
    setError(null);
    try {
      const [mine, pendingList, all] = await Promise.all([
        vehiclesApi.mine(),
        rentalsApi.ownerPending(),
        rentalsApi.me(),
      ]);
      setVehicles(mine);
      setPending(pendingList);
      setBookings(all.filter((b) => b.ownerId === user?.userId));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load fleet");
    }
  }, [isAuthenticated, user?.userId]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const addVehicle = async () => {
    setBusy(true);
    setError(null);
    try {
      await vehiclesApi.create(form);
      setForm(emptyForm);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Create failed");
    } finally {
      setBusy(false);
    }
  };

  const confirm = async (id: number) => {
    setBusy(true);
    try {
      await rentalsApi.confirm(id);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Confirm failed");
    } finally {
      setBusy(false);
    }
  };

  return {
    vehicles,
    pending,
    bookings,
    form,
    setForm,
    error,
    busy,
    addVehicle,
    confirm,
  };
}
