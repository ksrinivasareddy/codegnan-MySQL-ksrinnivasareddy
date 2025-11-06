package com.media.compression.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * ⚡ SpeedTestController
 * Provides binary data to measure real download speed from the client.
 * Client measures time taken to download and calculates Mbps.
 */
@Slf4j
@RestController
@RequestMapping("/compress")
@CrossOrigin(origins = "*")
public class SpeedTestController {

    /**
     * Generates random binary data for speed testing.
     * Size in bytes (query param)
     * 
     * Example: GET /compress/speedtest?sizeKB=5000
     * Will generate ~5MB of random data
     */
    @GetMapping(value = "/speedtest", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> speedTest(
            @RequestParam(defaultValue = "1024") int sizeKB) {
        
        try {
            int sizeBytes = Math.min(sizeKB * 1024, 100 * 1024 * 1024); // Cap at 100MB
            byte[] data = new byte[sizeBytes];
            new Random().nextBytes(data);

            log.info("🚀 Speed test: Sending {} KB of data", sizeKB);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate")
                    .header(HttpHeaders.PRAGMA, "no-cache")
                    .header("X-Content-Type-Options", "nosniff")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speedtest.bin\"")
                    .header("Content-Length", String.valueOf(sizeBytes))
                    .body(data);
        } catch (OutOfMemoryError e) {
            log.error("❌ Memory limit exceeded for speed test");
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Return info about speed test parameters
     */
    @GetMapping("/speedtest/info")
    public ResponseEntity<Map<String, Object>> speedTestInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("description", "Download binary data to measure network speed");
        info.put("endpoint", "/compress/speedtest");
        info.put("method", "GET");
        info.put("parameter", "sizeKB (default: 1024)");
        info.put("example", "/compress/speedtest?sizeKB=5000");
        info.put("maxSize", "100 MB");
        info.put("instructions", new String[]{
            "1. Record time before download",
            "2. Download binary data",
            "3. Record time after download",
            "4. Calculate Mbps = (sizeKB * 8) / (timeSeconds * 1000)"
        });
        return ResponseEntity.ok(info);
    }
}
