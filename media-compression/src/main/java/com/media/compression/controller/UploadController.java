package com.media.compression.controller;

import com.media.compression.model.CompressionMetadata;
import com.media.compression.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    /** Step 1: Pre-check upload */
    @PostMapping("/prepare")
    public ResponseEntity<Map<String, Object>> prepareUpload(@RequestBody Map<String, Object> body) {
        String filename = (String) body.get("filename");
        long size = ((Number) body.get("sizeBytes")).longValue();
        double duration = ((Number) body.getOrDefault("duration", 0.0)).doubleValue();
        String mime = (String) body.get("mimeType");
        Map<String, Object> deviceInfo = (Map<String, Object>) body.getOrDefault("deviceInfo", Map.of());
        return ResponseEntity.ok(uploadService.prepareUpload(filename, size, duration, mime, deviceInfo));
    }

    /** Step 2: Upload a chunk */
    @PostMapping("/chunk")
    public ResponseEntity<String> uploadChunk(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("index") int index,
            @RequestParam("file") MultipartFile chunk) {
        try {
            uploadService.saveChunk(uploadId, index, chunk);
            return ResponseEntity.ok("Chunk " + index + " uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error saving chunk: " + e.getMessage());
        }
    }

    /** Step 3: Finalize upload and compress */
    @PostMapping("/finalize")
    public ResponseEntity<?> finalizeUpload(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("filename") String filename) {
        try {
            Path assembled = uploadService.assembleChunks(uploadId, filename);
            CompressionMetadata result = uploadService.finalizeAndProcess(assembled);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Processing failed: " + e.getMessage());
        }
    }
}
