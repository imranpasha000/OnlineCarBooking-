import { apiFetch } from "./client";

export type AuthResponse = {
  accessToken: string;
  tokenType: string;
  userId: number;
  email: string;
  roles: string[];
};

export type UserInfo = {
  id: number;
  username: string;
  email: string;
  phone: string;
  active: boolean;
  provider: string;
  roles: string[];
};

export type LoginPayload = {
  email: string;
  password: string;
};

export type RegisterPayload = {
  username: string;
  email: string;
  phone: string;
  password: string;
  roles: string[];
};

export const authApi = {
  login: (data: LoginPayload) =>
    apiFetch<AuthResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  register: (data: RegisterPayload) =>
    apiFetch<AuthResponse>("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(data),
    }),

  me: () => apiFetch<UserInfo>("/api/auth/me"),
};
