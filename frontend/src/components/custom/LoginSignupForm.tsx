"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Button } from "../ui/button";
import { Label } from "../ui/label";
import { Input } from "../ui/input";
import { Checkbox } from "../ui/checkbox";
import { useAuth } from "@/context/AuthContext";

export default function LoginSignupForm() {
  const router = useRouter();
  const { login, register } = useAuth();

  const [activeTab, setActiveTab] = useState<"login" | "signup">("login");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [roleRider, setRoleRider] = useState(true);
  const [roleDriver, setRoleDriver] = useState(false);
  const [roleOwner, setRoleOwner] = useState(false);
  const [acceptPolicy, setAcceptPolicy] = useState(false);

  async function handleLogin(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await login(loginEmail.trim(), loginPassword);
      router.push("/ride");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    setError(null);

    if (!acceptPolicy) {
      setError("Please accept the privacy policy");
      return;
    }

    const roles: string[] = [];
    if (roleRider) roles.push("ROLE_RIDER");
    if (roleDriver) roles.push("ROLE_DRIVER");
    if (roleOwner) roles.push("ROLE_OWNER");
    if (roles.length === 0) roles.push("ROLE_RIDER");

    setSubmitting(true);
    try {
      await register({
        username: username.trim(),
        email: email.trim(),
        phone: phone.trim(),
        password,
        roles,
      });
      router.push("/ride");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Registration failed");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
      <div className="mb-6 flex gap-1">
        <Button
          type="button"
          className={`flex-1 rounded-t-md py-3 text-center font-medium transition-colors ${
            activeTab === "login"
              ? "bg-[#05a8f3] text-white hover:bg-[#0490d1]"
              : "border-b-2 border-[#05a8f3] bg-white text-[#05a8f3] hover:bg-sky-50"
          }`}
          onClick={() => {
            setActiveTab("login");
            setError(null);
          }}
        >
          Sign in
        </Button>
        <Button
          type="button"
          className={`flex-1 rounded-t-md py-3 text-center font-medium transition-colors ${
            activeTab === "signup"
              ? "bg-[#05a8f3] text-white hover:bg-[#0490d1]"
              : "border-b-2 border-[#05a8f3] bg-white text-[#05a8f3] hover:bg-sky-50"
          }`}
          onClick={() => {
            setActiveTab("signup");
            setError(null);
          }}
        >
          Register
        </Button>
      </div>

      {error && (
        <div className="mb-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {error}
        </div>
      )}

      {activeTab === "login" ? (
        <form className="space-y-4" onSubmit={handleLogin}>
          <div>
            <Label className="mb-1 block text-sm font-medium text-gray-700">
              Email
            </Label>
            <Input
              type="email"
              required
              value={loginEmail}
              onChange={(e) => setLoginEmail(e.target.value)}
              placeholder="you@example.com"
              className="w-full"
            />
          </div>

          <div>
            <Label className="mb-1 block text-sm font-medium text-gray-700">
              Password
            </Label>
            <Input
              type="password"
              required
              value={loginPassword}
              onChange={(e) => setLoginPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full"
            />
          </div>

          <Button
            type="submit"
            disabled={submitting}
            className="w-full bg-[#05a8f3] text-white hover:bg-[#0490d1]"
          >
            {submitting ? "Signing in…" : "Login"}
          </Button>
        </form>
      ) : (
        <form className="space-y-4" onSubmit={handleRegister}>
          <div>
            <Label className="mb-1 block text-sm font-medium text-gray-700">
              Username
            </Label>
            <Input
              type="text"
              required
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="User@123"
              className="w-full"
            />
          </div>

          <div>
            <Label className="mb-1 block text-sm font-medium text-gray-700">
              Email
            </Label>
            <Input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@example.com"
              className="w-full"
            />
          </div>

          <div>
            <Label className="mb-1 block text-sm font-medium text-gray-700">
              Phone
            </Label>
            <Input
              type="tel"
              required
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="+91-9999999999"
              className="w-full"
            />
          </div>

          <div>
            <Label className="mb-1 block text-sm font-medium text-gray-700">
              Password
            </Label>
            <Input
              type="password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              className="w-full"
            />
          </div>

          <div>
            <p className="mb-2 text-sm font-medium text-gray-700">Roles</p>
            <div className="flex flex-wrap gap-4">
              <div className="flex items-center gap-2">
                <Checkbox
                  id="roleRider"
                  checked={roleRider}
                  onCheckedChange={(c) => setRoleRider(c === true)}
                />
                <Label htmlFor="roleRider" className="text-sm text-gray-700">
                  Rider
                </Label>
              </div>
              <div className="flex items-center gap-2">
                <Checkbox
                  id="roleDriver"
                  checked={roleDriver}
                  onCheckedChange={(c) => setRoleDriver(c === true)}
                />
                <Label htmlFor="roleDriver" className="text-sm text-gray-700">
                  Driver
                </Label>
              </div>
              <div className="flex items-center gap-2">
                <Checkbox
                  id="roleOwner"
                  checked={roleOwner}
                  onCheckedChange={(c) => setRoleOwner(c === true)}
                />
                <Label htmlFor="roleOwner" className="text-sm text-gray-700">
                  Owner
                </Label>
              </div>
            </div>
          </div>

          <div className="flex items-center">
            <Checkbox
              checked={acceptPolicy}
              onCheckedChange={(checked) => setAcceptPolicy(checked === true)}
              id="acceptPolicy"
            />
            <Label htmlFor="acceptPolicy" className="ml-2 text-sm text-gray-700">
              I accept the privacy policy
            </Label>
          </div>

          <Button
            type="submit"
            disabled={submitting}
            className="w-full bg-[#05a8f3] text-white hover:bg-[#0490d1]"
          >
            {submitting ? "Creating account…" : "Register"}
          </Button>
        </form>
      )}
    </div>
  );
}
