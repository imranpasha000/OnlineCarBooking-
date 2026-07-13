"use client";

import { useState } from "react";
import {
  AuthGate,
  MobilityShell,
  RouteCanvas,
} from "@/modules/shared/ui";
import { RideBookingPanel } from "./RideBookingPanel";
import { useRideTrips } from "../hooks/useRideTrips";
import { useTripTracking } from "../hooks/useTripTracking";

function RideScreenInner() {
  const { activeTrip } = useRideTrips();
  const tracking = useTripTracking(
    activeTrip?.id ?? null,
    !!activeTrip &&
      (activeTrip.status === "ASSIGNED" || activeTrip.status === "STARTED")
  );

  const [route, setRoute] = useState({
    pickupLat: 12.9716,
    pickupLng: 77.5946,
    dropoffLat: 12.9352,
    dropoffLng: 77.6245,
  });

  const driverPoint = tracking.location
    ? {
        lat: tracking.location.lat,
        lng: tracking.location.lng,
        label: "Driver",
      }
    : {
        lat: route.pickupLat,
        lng: route.pickupLng,
        label: "Pickup",
      };

  return (
    <MobilityShell active="/ride">
      <div className="relative mx-auto grid max-w-6xl lg:grid-cols-[1fr_400px]">
        <RouteCanvas
          className="min-h-[56vh] lg:min-h-[calc(100vh-8rem)]"
          title={tracking.connected ? "Live tracking" : "Rider view"}
          live={!!activeTrip}
          points={[
            driverPoint,
            {
              lat: route.dropoffLat,
              lng: route.dropoffLng,
              label: "Dropoff",
            },
          ]}
        />
        <div className="relative z-20 -mt-8 px-3 pb-8 lg:mt-0 lg:px-4 lg:py-6">
          <RideBookingPanel onRouteChange={setRoute} />
        </div>
      </div>
    </MobilityShell>
  );
}

export function RideScreen() {
  return (
    <AuthGate>
      <RideScreenInner />
    </AuthGate>
  );
}
