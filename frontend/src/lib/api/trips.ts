import { apiFetch } from "./client";

export type TripStatus =
  | "REQUESTED"
  | "OFFERED"
  | "ASSIGNED"
  | "STARTED"
  | "COMPLETED"
  | "CANCELLED";

export type Trip = {
  id: number;
  riderId: number;
  driverId: number | null;
  vehicleId: number | null;
  pickupLat: number;
  pickupLng: number;
  pickupAddress: string | null;
  dropoffLat: number;
  dropoffLng: number;
  dropoffAddress: string | null;
  status: TripStatus;
  fare: number | null;
  distanceKm: number | null;
  createdAt: string;
  updatedAt: string;
  startedAt: string | null;
  completedAt: string | null;
};

export type CreateTripPayload = {
  pickupLat: number;
  pickupLng: number;
  pickupAddress?: string;
  dropoffLat: number;
  dropoffLng: number;
  dropoffAddress?: string;
  vehicleId?: number;
};

export const tripsApi = {
  create: (data: CreateTripPayload) =>
    apiFetch<Trip>("/api/trips", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  me: () => apiFetch<Trip[]>("/api/trips/me"),

  pending: () => apiFetch<Trip[]>("/api/trips/pending"),

  accept: (id: number) =>
    apiFetch<Trip>(`/api/trips/${id}/accept`, { method: "POST" }),

  start: (id: number) =>
    apiFetch<Trip>(`/api/trips/${id}/start`, { method: "POST" }),

  complete: (id: number) =>
    apiFetch<Trip>(`/api/trips/${id}/complete`, { method: "POST" }),

  cancel: (id: number) =>
    apiFetch<Trip>(`/api/trips/${id}/cancel`, { method: "POST" }),

  active: () => apiFetch<Trip>("/api/trips/active"),
};
