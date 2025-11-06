package com.media.compression.controller;

import com.media.compression.entity.CompressionHistory;
import com.media.compression.model.CompressionMetadata;
import com.media.compression.repository.CompressionHistoryRepository;
import com.media.compression.service.CompressionService;
import com.media.compression.util.DeviceNetworkDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CompressionController {

    private final CompressionService compressionService;
    private final DeviceNetworkDetector deviceNetworkDetector;
    private final CompressionHistoryRepository historyRepo;

    private final String BASE_DIR = System.getProperty("user.dir") + File.separator + "uploads";
    private final String INPUT_DIR = BASE_DIR + File.separator + "input";
    private final String OUTPUT_DIR = BASE_DIR + File.separator + "output";

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(INPUT_DIR));
            Files.createDirectories(Paths.get(OUTPUT_DIR));
            log.info("📁 Upload directories verified successfully.");
        } catch (IOException e) {
            log.error("❌ Failed to create upload directories: {}", e.getMessage());
        }
    }

    // =================== Compression ===================

    @PostMapping("/compress")
    public ResponseEntity<?> compressMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "deviceType", required = false) String deviceType,
            @RequestParam(value = "networkType", required = false) String networkType,
            @RequestParam(value = "speedMbps", defaultValue = "0") double speedMbps,
            HttpServletRequest request) {

        try {
            // Detect from request if not passed
            String detectedDevice = deviceNetworkDetector.detectDeviceType(request);
            String detectedNetwork = deviceNetworkDetector.detectNetworkType(networkType);
            double detectedSpeed = deviceNetworkDetector.detectNetworkSpeed(String.valueOf(speedMbps));

            // Use provided values if available
            if (deviceType != null && !deviceType.isBlank()) {
                detectedDevice = deviceType;
            }
            if (networkType != null && !networkType.isBlank()) {
                detectedNetwork = networkType;
            }

            Path filePath = Paths.get(INPUT_DIR, file.getOriginalFilename());
            file.transferTo(filePath);

            log.info("📩 Uploaded: {} | Device={} | Network={} | Speed={} Mbps",
                    file.getOriginalFilename(), detectedDevice, detectedNetwork, detectedSpeed);

            // Call service with all parameters
            CompressionMetadata meta = compressionService.compressAdaptive(
                    filePath.toAbsolutePath().toString(),
                    detectedDevice,
                    detectedNetwork,
                    detectedSpeed
            );

            return ResponseEntity.ok(meta);

        } catch (Exception e) {
            log.error("❌ Compression error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Compression failed: " + e.getMessage());
        }
    }


    @GetMapping("/compress/download")
    public ResponseEntity<?> downloadFile(@RequestParam("file") String filename) throws IOException {
        Path filePath = Paths.get(OUTPUT_DIR, filename);
        log.info("📥 Download requested for file: {}", filePath.toAbsolutePath());

        if (!Files.exists(filePath)) {
            log.error("❌ File not found: {}", filePath.toAbsolutePath());
            return ResponseEntity.status(404).body("File not found: " + filename);
        }

        byte[] data = Files.readAllBytes(filePath);
        String mimeType = Files.probeContentType(filePath);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .header("Content-Type", mimeType != null ? mimeType : "application/octet-stream")
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Expose-Headers", "Content-Disposition")
                .body(data);
    }

    // =================== History ===================

    @GetMapping("/history")
    public ResponseEntity<List<CompressionHistory>> allHistory() {
        return ResponseEntity.ok(historyRepo.findAll());
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<?> deleteHistory(@PathVariable Long id) {
        if (!historyRepo.existsById(id)) return ResponseEntity.notFound().build();
        historyRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
