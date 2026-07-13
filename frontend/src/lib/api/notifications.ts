import { apiFetch } from "./client";

export type NotificationRecord = {
  id: string;
  userId: number;
  type: string;
  title: string;
  message: string;
  read: boolean;
  createdAt: string;
};

export const notificationsApi = {
  me: () => apiFetch<NotificationRecord[]>("/api/notifications/me"),
};
