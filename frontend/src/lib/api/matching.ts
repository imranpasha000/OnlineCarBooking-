import { apiFetch } from "./client";

export type NearbyDriver = {
  driverId: number;
  lat: number;
  lng: number;
  distanceKm: number;
};

export const matchingApi = {
  updateLocation: (lat: number, lng: number) =>
    apiFetch<{ driverId: number; status: string }>(
      "/api/matching/drivers/location",
      {
        method: "POST",
        body: JSON.stringify({ lat, lng }),
      }
    ),

  nearby: (lat: number, lng: number, radiusKm = 5) =>
    apiFetch<NearbyDriver[]>(
      `/api/matching/drivers/nearby?lat=${lat}&lng=${lng}&radiusKm=${radiusKm}`
    ),

  offline: () =>
    apiFetch<{ driverId: number; status: string }>(
      "/api/matching/drivers/offline",
      { method: "POST" }
    ),
};
