package com.media.compression.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.*;

@Slf4j
@RestController
@RequestMapping("/uploads/output")
@CrossOrigin(origins = "*")
public class FileServeController {

    private final Path OUTPUT_DIR = Paths.get(System.getProperty("user.dir"), "uploads", "output");

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = OUTPUT_DIR.resolve(filename).normalize();

            if (!Files.exists(filePath)) {
                log.warn("❌ Requested file not found: {}", filePath.toAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) contentType = "application/octet-stream";

            log.info("✅ Serving file: {}", filePath.toAbsolutePath());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("❌ Malformed file URL: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Error serving file: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
