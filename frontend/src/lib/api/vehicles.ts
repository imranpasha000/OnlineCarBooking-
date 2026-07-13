import { apiFetch } from "./client";

export type VehicleType = "RIDE" | "RENTAL" | "BOTH";
export type VehicleStatus = "AVAILABLE" | "UNAVAILABLE" | "BOOKED";

export type Vehicle = {
  id: number;
  ownerId: number;
  name: string;
  brand: string | null;
  model: string | null;
  year: number | null;
  plateNumber: string | null;
  type: VehicleType;
  pricePerDay: number | null;
  pricePerKm: number | null;
  status: VehicleStatus;
  imageUrl: string | null;
  fuelType: string | null;
  transmission: string | null;
  seats: number | null;
  description: string | null;
  lat: number | null;
  lng: number | null;
  createdAt: string;
  updatedAt: string;
};

export type CreateVehiclePayload = {
  name: string;
  brand?: string;
  model?: string;
  year?: number;
  plateNumber?: string;
  type: VehicleType;
  pricePerDay?: number;
  pricePerKm?: number;
  imageUrl?: string;
  fuelType?: string;
  transmission?: string;
  seats?: number;
  description?: string;
  lat?: number;
  lng?: number;
};

export const vehiclesApi = {
  search: (params?: { type?: VehicleType; status?: VehicleStatus }) => {
    const qs = new URLSearchParams();
    if (params?.type) qs.set("type", params.type);
    if (params?.status) qs.set("status", params.status);
    const query = qs.toString();
    return apiFetch<Vehicle[]>(
      `/api/vehicles/search${query ? `?${query}` : ""}`
    );
  },

  available: (type?: VehicleType) => {
    const qs = type ? `?type=${type}` : "";
    return apiFetch<Vehicle[]>(`/api/vehicles/available${qs}`);
  },

  create: (data: CreateVehiclePayload) =>
    apiFetch<Vehicle>("/api/vehicles", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  mine: () => apiFetch<Vehicle[]>("/api/vehicles"),

  updateStatus: (id: number, status: VehicleStatus) =>
    apiFetch<Vehicle>(`/api/vehicles/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status }),
    }),
};
