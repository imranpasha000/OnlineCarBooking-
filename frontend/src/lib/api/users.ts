import { apiFetch } from "./client";

export type UserProfile = {
  id: number;
  username: string | null;
  email: string | null;
  phone: string | null;
  displayName: string | null;
  driverOnline: boolean;
  driverLat: number | null;
  driverLng: number | null;
  kycVerified: boolean;
  roles: string[] | null;
  sellerType: string | null;
};

export const usersApi = {
  me: () => apiFetch<UserProfile>("/api/users/me"),

  setDriverOnline: (online: boolean, lat?: number, lng?: number) =>
    apiFetch<UserProfile>("/api/users/me/driver/online", {
      method: "PUT",
      body: JSON.stringify({ online, lat, lng }),
    }),

  onlineDrivers: () => apiFetch<UserProfile[]>("/api/users/drivers/online"),

  listAll: () => apiFetch<UserProfile[]>("/api/users"),
};
