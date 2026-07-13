"use client";

import React, { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { Button } from "@/components/ui/button";
import {
  NavigationMenu,
  NavigationMenuItem,
  NavigationMenuLink,
  NavigationMenuList,
} from "../ui/navigation-menu";
import { useAuth } from "@/context/AuthContext";
import { cn } from "@/lib/utils";

const productLinks = [
  { href: "/ride", label: "Ride" },
  { href: "/drive", label: "Drive" },
  { href: "/rent", label: "Rent" },
  { href: "/owner", label: "Fleet" },
  { href: "/admin", label: "Admin" },
];

const Header = () => {
  const [isOpen, setIsOpen] = useState(false);
  const { user, isAuthenticated, logout, loading } = useAuth();
  const pathname = usePathname();
  const dark = ["/ride", "/drive", "/rent", "/owner", "/admin"].some(
    (p) => pathname === p || pathname.startsWith(`${p}/`)
  );

  return (
    <header
      className={cn(
        "sticky top-0 z-50 border-b backdrop-blur",
        dark
          ? "border-white/10 bg-ink/90 text-white"
          : "border-slate-200 bg-white/95 text-slate-900 shadow-sm"
      )}
    >
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between py-3">
          <Link href="/" className="font-display text-2xl font-bold tracking-tight">
            <span className={dark ? "text-volt" : "text-[#05a8f3]"}>BOX</span>
            <span className={dark ? "text-white" : "text-slate-900"}>CARS</span>
          </Link>

          <div className="hidden items-center md:flex">
            <NavigationMenu>
              <NavigationMenuList className="gap-1">
                <NavigationMenuItem>
                  <NavigationMenuLink asChild>
                    <Link
                      href="/"
                      className={cn(
                        "rounded-full px-3 py-2 text-sm font-medium",
                        dark ? "hover:bg-white/10" : "hover:bg-gray-100"
                      )}
                    >
                      Home
                    </Link>
                  </NavigationMenuLink>
                </NavigationMenuItem>
                {productLinks.map((link) => (
                  <NavigationMenuItem key={link.href}>
                    <NavigationMenuLink asChild>
                      <Link
                        href={link.href}
                        className={cn(
                          "rounded-full px-3 py-2 text-sm font-medium",
                          pathname.startsWith(link.href)
                            ? dark
                              ? "bg-volt text-ink"
                              : "bg-[#05a8f3] text-white"
                            : dark
                              ? "hover:bg-white/10"
                              : "hover:bg-gray-100"
                        )}
                      >
                        {link.label}
                      </Link>
                    </NavigationMenuLink>
                  </NavigationMenuItem>
                ))}
              </NavigationMenuList>
            </NavigationMenu>
          </div>

          <div className="flex items-center gap-3">
            {!loading &&
              (isAuthenticated ? (
                <div className="hidden items-center gap-3 sm:flex">
                  <span
                    className={cn(
                      "max-w-[160px] truncate text-sm",
                      dark ? "text-slate-300" : "text-slate-600"
                    )}
                  >
                    {user?.email}
                  </span>
                  <Button
                    variant="outline"
                    size="sm"
                    className={cn(
                      "rounded-full",
                      dark
                        ? "border-volt/40 bg-transparent text-volt hover:bg-volt hover:text-ink"
                        : "border-[#05a8f3] text-[#05a8f3]"
                    )}
                    onClick={logout}
                  >
                    Logout
                  </Button>
                </div>
              ) : (
                <Button
                  asChild
                  size="sm"
                  className={cn(
                    "hidden rounded-full sm:inline-flex",
                    dark
                      ? "bg-volt text-ink hover:bg-volt-soft"
                      : "bg-[#05a8f3] text-white hover:bg-[#0490d1]"
                  )}
                >
                  <Link href="/sign-in">Sign In</Link>
                </Button>
              ))}

            <Button
              variant="ghost"
              size="icon"
              className={cn("md:hidden", dark && "text-white hover:bg-white/10")}
              onClick={() => setIsOpen(!isOpen)}
              aria-label="Toggle menu"
            >
              <svg className="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            </Button>
          </div>
        </div>
      </div>

      {isOpen && (
        <div
          className={cn(
            "absolute left-0 right-0 top-full z-50 border-t md:hidden",
            dark
              ? "border-white/10 bg-ink text-white"
              : "border-gray-200 bg-white text-slate-800"
          )}
        >
          <nav className="container mx-auto flex flex-col gap-1 px-4 py-3">
            <Link href="/" onClick={() => setIsOpen(false)} className="py-2">
              Home
            </Link>
            {productLinks.map((link) => (
              <Link
                key={link.href}
                href={link.href}
                className="py-2"
                onClick={() => setIsOpen(false)}
              >
                {link.label}
              </Link>
            ))}
            {isAuthenticated ? (
              <Button
                variant="outline"
                className="mt-2"
                onClick={() => {
                  logout();
                  setIsOpen(false);
                }}
              >
                Logout
              </Button>
            ) : (
              <Link href="/sign-in" onClick={() => setIsOpen(false)}>
                <Button className="mt-2 w-full">Sign In</Button>
              </Link>
            )}
          </nav>
        </div>
      )}
    </header>
  );
};

export default Header;
