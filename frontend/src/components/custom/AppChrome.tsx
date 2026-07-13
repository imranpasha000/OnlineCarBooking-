"use client";

import { usePathname } from "next/navigation";
import Header from "@/components/custom/header";
import Footer from "@/components/custom/footer";

const MOBILITY = ["/ride", "/drive", "/rent", "/owner", "/admin"];

export function AppChrome({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const isMobility = MOBILITY.some(
    (p) => pathname === p || pathname.startsWith(`${p}/`)
  );

  return (
    <>
      <Header />
      {children}
      {!isMobility && <Footer />}
    </>
  );
}
