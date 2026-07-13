import { apiFetch } from "./client";

export type RentalStatus =
  | "REQUESTED"
  | "CONFIRMED"
  | "PICKED_UP"
  | "RETURNED"
  | "CANCELLED";

export type RentalBooking = {
  id: number;
  customerId: number;
  ownerId: number;
  vehicleId: number;
  startDate: string;
  endDate: string;
  pickupAddress: string | null;
  returnAddress: string | null;
  status: RentalStatus;
  dailyRate: number;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
};

export type BookRentalPayload = {
  vehicleId: number;
  ownerId: number;
  startDate: string;
  endDate: string;
  pickupAddress?: string;
  returnAddress?: string;
  dailyRate: number;
};

export const rentalsApi = {
  book: (data: BookRentalPayload) =>
    apiFetch<RentalBooking>("/api/rentals/book", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  me: () => apiFetch<RentalBooking[]>("/api/rentals/me"),

  ownerPending: () =>
    apiFetch<RentalBooking[]>("/api/rentals/owner/pending"),

  confirm: (id: number) =>
    apiFetch<RentalBooking>(`/api/rentals/${id}/confirm`, {
      method: "POST",
    }),

  pickup: (id: number) =>
    apiFetch<RentalBooking>(`/api/rentals/${id}/pickup`, {
      method: "POST",
    }),

  return: (id: number) =>
    apiFetch<RentalBooking>(`/api/rentals/${id}/return`, {
      method: "POST",
    }),
};
