package com.media.compression.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Determines dynamic compression quality profiles based on device and network type.
 * Used for both images and videos.
 */
public class DeviceQualityUtil {

    public static Map<String, Object> getQualityProfile(String deviceType, String networkType, String mediaType) {
        Map<String, Object> profile = new HashMap<>();

        // Normalize
        deviceType = (deviceType == null) ? "unknown" : deviceType.toLowerCase();
        networkType = (networkType == null) ? "wifi" : networkType.toLowerCase();
        mediaType = (mediaType == null) ? "generic" : mediaType.toLowerCase();

        int targetDpi;
        int targetHeight;
        int targetBitrateKbps;
        double qualityFactor;

        // Device-specific DPI (used for scaling)
        switch (deviceType) {
            case "android" -> targetDpi = 240;
            case "iphone" -> targetDpi = 320;
            case "tablet", "ipad" -> targetDpi = 480;
            case "desktop", "laptop" -> targetDpi = 96;
            default -> targetDpi = 160;
        }

        // Network-specific compression factor
        switch (networkType) {
            case "2g", "3g" -> qualityFactor = 0.5;
            case "4g" -> qualityFactor = 0.7;
            case "5g", "wifi" -> qualityFactor = 0.9;
            default -> qualityFactor = 0.6;
        }

        // Media-type optimization
        if ("video".equals(mediaType)) {
            if (targetDpi <= 160) {
                targetHeight = 360;
                targetBitrateKbps = 800;
            } else if (targetDpi <= 240) {
                targetHeight = 480;
                targetBitrateKbps = 1200;
            } else if (targetDpi <= 320) {
                targetHeight = 720;
                targetBitrateKbps = 2000;
            } else {
                targetHeight = 1080;
                targetBitrateKbps = 3000;
            }
        } else {
            targetHeight = 1080;
            targetBitrateKbps = 0;
        }

        profile.put("targetDpi", targetDpi);
        profile.put("targetHeight", targetHeight);
        profile.put("targetBitrateKbps", targetBitrateKbps);
        profile.put("compressionQuality", qualityFactor);
        profile.put("description", String.format("Optimized for %s on %s (%ddpi, %.0f%% quality)",
                deviceType, networkType, targetDpi, qualityFactor * 100));
        return profile;
    }
}
