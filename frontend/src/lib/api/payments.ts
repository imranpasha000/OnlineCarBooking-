import { apiFetch } from "./client";

export type Payment = {
  id: number;
  userId: number;
  amount: number;
  currency: string;
  status: string;
  provider: string;
  referenceType: string | null;
  referenceId: number | null;
  createdAt: string;
};

export const paymentsApi = {
  me: () => apiFetch<Payment[]>("/api/payments/me"),
};
