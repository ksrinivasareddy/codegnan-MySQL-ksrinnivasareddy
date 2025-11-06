package com.media.compression.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DeviceNetworkDetector {

    /** ✅ Detect Device Type (from User-Agent and other headers) */
    public String detectDeviceType(HttpServletRequest request) {
        if (request == null) return "Unknown Device";

        String ua = request.getHeader("User-Agent");
        if (ua == null) return "Unknown Device";

        ua = ua.toLowerCase();

        // Mobile Devices
        if (ua.contains("android")) {
            if (ua.contains("tablet")) return "Android Tablet";
            return "Android Phone";
        }
        
        if (ua.contains("iphone")) return "iPhone";
        if (ua.contains("ipad")) return "iPad";
        if (ua.contains("windows phone")) return "Windows Phone";
        
        // Desktop/Laptop
        if (ua.contains("windows")) {
            if (ua.contains("mobile")) return "Windows Mobile";
            return "Windows PC";
        }
        
        if (ua.contains("macintosh") || ua.contains("mac os")) {
            if (ua.contains("iphone") || ua.contains("ipad")) return "Apple Mobile";
            return "MacBook/iMac";
        }
        
        if (ua.contains("linux")) {
            if (ua.contains("android")) return "Android Device";
            return "Linux PC";
        }
        
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "Mobile Device";
        }

        return "Desktop";
    }

    /** ✅ Detect Network Type (from client or fallback detection) */
    public String detectNetworkType(String clientNetwork) {
        if (clientNetwork != null && !clientNetwork.isBlank()) {
            String net = clientNetwork.toLowerCase().trim();
            
            // Validate known network types
            switch (net) {
                case "2g":
                case "3g":
                case "4g":
                case "5g":
                case "wifi":
                case "ethernet":
                    return net.toUpperCase();
                default:
                    return "Unknown Network";
            }
        }
        
        return "Unknown Network";
    }

    /** ✅ Detect Network Speed (Mbps) */
    public double detectNetworkSpeed(String clientSpeed) {
        if (clientSpeed != null && !clientSpeed.isBlank()) {
            try {
                double speed = Double.parseDouble(clientSpeed);
                if (speed > 0 && speed <= 10000) {
                    return Math.round(speed * 100.0) / 100.0; // Round to 2 decimals
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid speed value: {}", clientSpeed);
            }
        }
        return 0.0;
    }

    /** ✅ Classify Network Speed into category */
    public String classifyNetworkSpeed(double speedMbps) {
        if (speedMbps <= 0) return "Unknown";
        if (speedMbps < 1) return "2G";
        if (speedMbps < 3) return "3G";
        if (speedMbps < 30) return "4G";
        if (speedMbps < 100) return "5G";
        return "Fiber/Ethernet";
    }

    /** ✅ Classify Network Type from speed if type is unknown */
    public String inferNetworkType(double speedMbps) {
        if (speedMbps <= 0) return "Unknown";
        if (speedMbps < 1) return "2G";
        if (speedMbps < 3) return "3G";
        if (speedMbps < 30) return "4G";
        return "5G";
    }
}
