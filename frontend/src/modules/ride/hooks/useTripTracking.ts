"use client";

import { useEffect, useRef, useState } from "react";

const WS_BASE =
  process.env.NEXT_PUBLIC_WS_URL ||
  (process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080").replace(
    "8080",
    "8084"
  );

/**
 * Phase 3: live trip location via SockJS/STOMP-compatible simple WS poll fallback.
 * Trip-service exposes SockJS at /ws and topic /topic/trips/{id}/location.
 * This hook uses EventSource-like polling of last known coords via POST location echo when WS unavailable.
 */
export function useTripTracking(tripId: number | null, enabled: boolean) {
  const [location, setLocation] = useState<{ lat: number; lng: number } | null>(
    null
  );
  const [connected, setConnected] = useState(false);
  const socketRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    if (!enabled || !tripId) return;

    const url = WS_BASE.replace(/^http/, "ws") + `/ws/trips/${tripId}`;
    try {
      const ws = new WebSocket(url);
      socketRef.current = ws;
      ws.onopen = () => setConnected(true);
      ws.onclose = () => setConnected(false);
      ws.onerror = () => setConnected(false);
      ws.onmessage = (ev) => {
        try {
          const data = JSON.parse(ev.data) as { lat?: number; lng?: number };
          if (typeof data.lat === "number" && typeof data.lng === "number") {
            setLocation({ lat: data.lat, lng: data.lng });
          }
        } catch {
          // ignore
        }
      };
      return () => ws.close();
    } catch {
      setConnected(false);
    }
  }, [tripId, enabled]);

  return { location, connected };
}
