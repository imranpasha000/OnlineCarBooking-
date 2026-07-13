package com.boxcars.matching.service;

import com.boxcars.common.event.DomainEvent;
import com.boxcars.matching.config.RabbitConfig;
import com.boxcars.matching.dto.NearbyDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    public static final String DRIVERS_GEO_KEY = "drivers:geo";
    public static final String DRIVERS_ONLINE_KEY = "drivers:online";

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    public void updateDriverLocation(Long driverId, double lat, double lng) {
        try {
            redisTemplate.opsForGeo().add(DRIVERS_GEO_KEY, new Point(lng, lat), String.valueOf(driverId));
            redisTemplate.opsForSet().add(DRIVERS_ONLINE_KEY, String.valueOf(driverId));
            log.info("Driver {} location updated lat={} lng={}", driverId, lat, lng);
        } catch (Exception ex) {
            log.warn("Failed to update driver location in Redis: {}", ex.getMessage());
            throw ex;
        }
    }

    public void setDriverOffline(Long driverId) {
        try {
            redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY, String.valueOf(driverId));
            redisTemplate.opsForSet().remove(DRIVERS_ONLINE_KEY, String.valueOf(driverId));
            log.info("Driver {} set offline", driverId);
        } catch (Exception ex) {
            log.warn("Failed to set driver offline in Redis: {}", ex.getMessage());
            throw ex;
        }
    }

    public List<NearbyDriverResponse> findNearby(double lat, double lng, double radiusKm) {
        try {
            Circle circle = new Circle(new Point(lng, lat), new Distance(radiusKm, Metrics.KILOMETERS));
            RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                    .newGeoRadiusArgs()
                    .includeDistance()
                    .includeCoordinates()
                    .sortAscending();
            GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo()
                    .radius(DRIVERS_GEO_KEY, circle, args);

            List<NearbyDriverResponse> nearby = new ArrayList<>();
            if (results == null) {
                return nearby;
            }
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
                RedisGeoCommands.GeoLocation<String> location = result.getContent();
                nearby.add(NearbyDriverResponse.builder()
                        .driverId(Long.parseLong(location.getName()))
                        .distanceKm(result.getDistance() != null ? result.getDistance().getValue() : null)
                        .lat(location.getPoint() != null ? location.getPoint().getY() : null)
                        .lng(location.getPoint() != null ? location.getPoint().getX() : null)
                        .build());
            }
            return nearby;
        } catch (Exception ex) {
            log.warn("Redis GEO nearby search failed: {}", ex.getMessage());
            return List.of();
        }
    }

    public void handleTripRequested(DomainEvent event) {
        Map<String, Object> payload = event.payload() != null ? event.payload() : Map.of();
        Long tripId = toLong(payload.get("tripId"));
        if (tripId == null && event.aggregateId() != null) {
            tripId = toLong(event.aggregateId());
        }

        Double lat = toDouble(payload.get("pickupLat"));
        if (lat == null) {
            lat = toDouble(payload.get("lat"));
        }
        Double lng = toDouble(payload.get("pickupLng"));
        if (lng == null) {
            lng = toDouble(payload.get("lng"));
        }

        List<Long> driverIds = new ArrayList<>();
        try {
            if (lat != null && lng != null) {
                List<NearbyDriverResponse> nearby = findNearby(lat, lng, 5.0);
                for (NearbyDriverResponse driver : nearby) {
                    driverIds.add(driver.getDriverId());
                }
            } else {
                log.info("TRIP_REQUESTED missing pickup coords — publishing broadcast DRIVER_OFFER");
            }
        } catch (Exception ex) {
            log.warn("Nearby driver lookup failed, continuing with empty driverIds: {}", ex.getMessage());
            driverIds = new ArrayList<>();
        }

        publishDriverOffer(tripId, driverIds, payload);
    }

    public void publishDriverOffer(Long tripId, List<Long> driverIds, Map<String, Object> sourcePayload) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tripId", tripId);
        payload.put("driverIds", driverIds != null ? driverIds : List.of());
        if (sourcePayload != null) {
            Object riderId = sourcePayload.get("riderId");
            if (riderId == null) {
                riderId = sourcePayload.get("customerId");
            }
            if (riderId != null) {
                payload.put("riderId", riderId);
            }
            if (sourcePayload.get("pickupLat") != null) {
                payload.put("pickupLat", sourcePayload.get("pickupLat"));
            }
            if (sourcePayload.get("pickupLng") != null) {
                payload.put("pickupLng", sourcePayload.get("pickupLng"));
            }
        }

        String aggregateId = tripId != null ? String.valueOf(tripId) : "unknown";
        DomainEvent offer = DomainEvent.of(DomainEvent.DRIVER_OFFER, aggregateId, payload);
        try {
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, DomainEvent.DRIVER_OFFER, offer);
            log.info("Published DRIVER_OFFER tripId={} driverIds={}", tripId, driverIds);
        } catch (Exception ex) {
            log.error("Failed to publish DRIVER_OFFER: {}", ex.getMessage(), ex);
        }
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Double toDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
