# Frontend modular structure

```
src/
  lib/api/          # Gateway API clients (auth, trips, rentals, vehicles, …)
  context/          # AuthProvider
  modules/
    shared/ui/      # MobilityShell, RouteCanvas, BookingSheet, StatusChip, AuthGate
    ride/           # Rider Uber-like flow
    drive/          # Driver mirror flow
    rent/           # Rental catalog + booking (uses vehicle listings API)
    owner/          # Fleet listing + confirm bookings
  app/
    ride|drive|rent|owner|admin/page.tsx  # thin route shells
```

Design: **Night Route** — ink canvas (`#0B1220`) + volt cyan (`#00C2FF`), Outfit + DM Sans. Full-bleed route plane + floating booking sheet (Uber interaction pattern, unique BoxCars look).

Env: `NEXT_PUBLIC_API_URL=http://localhost:8080`
