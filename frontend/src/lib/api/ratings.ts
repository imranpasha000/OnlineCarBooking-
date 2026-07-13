import { apiFetch } from "./client";

export type CreateRatingPayload = {
  referenceType: "TRIP" | "RENTAL";
  referenceId: number;
  toUserId: number;
  score: number;
  comment?: string;
};

export type Rating = {
  id: number;
  fromUserId: number;
  toUserId: number;
  referenceType: string;
  referenceId: number;
  score: number;
  comment: string | null;
  createdAt: string;
};

export const ratingsApi = {
  create: (data: CreateRatingPayload) =>
    apiFetch<Rating>("/api/ratings", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  average: (userId: number) =>
    apiFetch<{ userId: number; average: number; count: number }>(
      `/api/ratings/user/${userId}/average`
    ),
};
