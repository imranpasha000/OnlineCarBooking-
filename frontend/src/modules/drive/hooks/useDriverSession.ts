"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { ApiError } from "@/lib/api/client";
import { matchingApi } from "@/lib/api/matching";
import { tripsApi, type Trip } from "@/lib/api/trips";
import { usersApi } from "@/lib/api/users";
import { useAuth } from "@/context/AuthContext";

export function useDriverSession() {
  const { isAuthenticated } = useAuth();
  const [online, setOnline] = useState(false);
  const [pending, setPending] = useState<Trip[]>([]);
  const [active, setActive] = useState<Trip | null>(null);
  const [lat, setLat] = useState(12.9716);
  const [lng, setLng] = useState(77.5946);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const timer = useRef<ReturnType<typeof setInterval> | null>(null);

  const refresh = useCallback(async () => {
    if (!isAuthenticated) return;
    try {
      setPending(await tripsApi.pending());
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load pending");
    }
    try {
      setActive(await tripsApi.active());
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) setActive(null);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  useEffect(() => {
    if (!online) {
      if (timer.current) clearInterval(timer.current);
      return;
    }
    const ping = async () => {
      try {
        await matchingApi.updateLocation(lat, lng);
        await usersApi.setDriverOnline(true, lat, lng);
      } catch {
        // keep UI online; infra may be down locally
      }
      void refresh();
    };
    void ping();
    timer.current = setInterval(ping, 8000);
    return () => {
      if (timer.current) clearInterval(timer.current);
    };
  }, [online, lat, lng, refresh]);

  const goOnline = async (next: boolean) => {
    setBusy(true);
    setError(null);
    try {
      if (next) {
        await usersApi.setDriverOnline(true, lat, lng);
        await matchingApi.updateLocation(lat, lng);
      } else {
        await usersApi.setDriverOnline(false, lat, lng);
        await matchingApi.offline();
      }
      setOnline(next);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to update status");
    } finally {
      setBusy(false);
    }
  };

  const accept = async (id: number) => {
    setBusy(true);
    try {
      await tripsApi.accept(id);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Accept failed");
    } finally {
      setBusy(false);
    }
  };

  const start = async (id: number) => {
    setBusy(true);
    try {
      await tripsApi.start(id);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Start failed");
    } finally {
      setBusy(false);
    }
  };

  const complete = async (id: number) => {
    setBusy(true);
    try {
      await tripsApi.complete(id);
      await refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Complete failed");
    } finally {
      setBusy(false);
    }
  };

  return {
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
  };
}
