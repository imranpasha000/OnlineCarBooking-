"use client";

import { useCallback, useEffect, useState } from "react";
import { ApiError } from "@/lib/api/client";
import { tripsApi, type CreateTripPayload, type Trip } from "@/lib/api/trips";
import { useAuth } from "@/context/AuthContext";

export function useRideTrips() {
  const { isAuthenticated } = useAuth();
  const [activeTrip, setActiveTrip] = useState<Trip | null>(null);
  const [trips, setTrips] = useState<Trip[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const refresh = useCallback(async () => {
    if (!isAuthenticated) return;
    setError(null);
    try {
      setTrips(await tripsApi.me());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load trips");
    }
    try {
      setActiveTrip(await tripsApi.active());
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) setActiveTrip(null);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const requestRide = async (payload: CreateTripPayload) => {
    setBusy(true);
    setError(null);
    try {
      await tripsApi.create(payload);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Request failed");
      throw err;
    } finally {
      setBusy(false);
    }
  };

  const cancelTrip = async (id: number) => {
    setBusy(true);
    setError(null);
    try {
      await tripsApi.cancel(id);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Cancel failed");
    } finally {
      setBusy(false);
    }
  };

  return { activeTrip, trips, error, busy, refresh, requestRide, cancelTrip, setError };
}
