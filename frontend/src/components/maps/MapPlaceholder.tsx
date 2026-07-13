"use client";

import { RouteCanvas, type RoutePoint } from "@/modules/shared/ui/RouteCanvas";

export type MapPoint = RoutePoint;

/** Back-compat wrapper — mobility modules use RouteCanvas directly. */
export default function MapPlaceholder({
  points,
  className,
  title,
}: {
  points: MapPoint[];
  className?: string;
  title?: string;
}) {
  return <RouteCanvas points={points} className={className} title={title} />;
}
