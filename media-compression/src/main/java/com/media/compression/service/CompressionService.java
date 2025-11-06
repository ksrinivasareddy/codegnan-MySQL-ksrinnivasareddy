package com.media.compression.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.media.compression.entity.CompressionHistory;
import com.media.compression.repository.CompressionHistoryRepository;
import com.media.compression.util.DeviceNetworkDetector;
import com.media.compression.util.FFmpegExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CompressionService {

    @Autowired
    private FFmpegExecutor ffmpegExecutor;

    @Autowired
    private CompressionHistoryRepository compressionHistoryRepository;

    @Autowired
    private DeviceNetworkDetector deviceNetworkDetector;

    private static final String UPLOAD_DIR = "uploads/";
    private static final String INPUT_DIR = UPLOAD_DIR + "input/";
    private static final String OUTPUT_DIR = UPLOAD_DIR + "output/";
    private static final String BASE_URL = "http://localhost:8080/";

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(INPUT_DIR));
            Files.createDirectories(Paths.get(OUTPUT_DIR));
            log.info("Upload directories created successfully");
        } catch (IOException e) {
            log.error("Failed to create upload directories", e);
        }
    }

    public Map<String, Object> compressAdaptive(MultipartFile file, String userAgent, String networkType, String compressionSpeed) {
        Map<String, Object> result = new HashMap<>();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
        
        File inputFile = new File(INPUT_DIR + uniqueFilename);
        File outputFile = null;

        try {
            // Save uploaded file
            file.transferTo(inputFile);
            long originalSize = inputFile.length();

            // Detect device and network
            String deviceCategory = deviceNetworkDetector.detectDevice(userAgent);
            String detectedNetwork = deviceNetworkDetector.detectNetwork(networkType);

            // Choose compression profile
            Map<String, Object> profile = chooseProfile(deviceCategory, detectedNetwork, compressionSpeed);

            // Determine media type and compress
            String mediaType;
            if (fileExtension.matches("(?i)(jpg|jpeg|png|gif|bmp|webp)")) {
                mediaType = "image";
                outputFile = new File(OUTPUT_DIR + UUID.randomUUID().toString() + ".jpg");
                compressImage(inputFile, outputFile, (Integer) profile.get("quality"));
            } else if (fileExtension.matches("(?i)(mp4|avi|mov|mkv|flv|wmv)")) {
                mediaType = "video";
                outputFile = new File(OUTPUT_DIR + UUID.randomUUID().toString() + ".mp4");
                compressVideo(inputFile, outputFile, profile);
            } else if (fileExtension.matches("(?i)(mp3|wav|aac|flac|ogg)")) {
                mediaType = "audio";
                outputFile = new File(OUTPUT_DIR + UUID.randomUUID().toString() + ".mp3");
                compressAudio(inputFile, outputFile, (Integer) profile.get("audioBitrate"));
            } else {
                result.put("status", "error");
                result.put("message", "Unsupported file type: " + fileExtension);
                return result;
            }

            long compressedSize = outputFile.length();
            double compressionRatio = (double) originalSize / compressedSize;
            double compressionPercentage = ((double) (originalSize - compressedSize) / originalSize) * 100;

            // Get metadata
            Map<String, Object> metadata = getMediaMetadata(outputFile, mediaType);

            // Build result
            result.put("status", "success");
            result.put("originalFilename", originalFilename);
            result.put("compressedFilename", outputFile.getName());
            result.put("originalSize", originalSize);
            result.put("compressedSize", compressedSize);
            result.put("compressionRatio", String.format("%.2f", compressionRatio));
            result.put("compressionPercentage", String.format("%.2f%%", compressionPercentage));
            result.put("mediaType", mediaType);
            result.put("deviceCategory", deviceCategory);
            result.put("networkType", detectedNetwork);
            result.put("compressionSpeed", compressionSpeed);
            result.put("profile", profile);
            result.put("metadata", metadata);
            result.put("inputUrl", BASE_URL + INPUT_DIR + uniqueFilename);
            result.put("outputUrl", BASE_URL + OUTPUT_DIR + outputFile.getName());

            // Save to database
            saveHistory(result);

        } catch (Exception e) {
            log.error("Compression failed", e);
            result.put("status", "error");
            result.put("message", "Compression failed: " + e.getMessage());
        }

        return result;
    }

    private void compressImage(File inputFile, File outputFile, int quality) throws IOException {
        BufferedImage originalImage = ImageIO.read(inputFile);
        
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Calculate new dimensions based on quality
        double scaleFactor = quality / 100.0;
        int newWidth = (int) (originalWidth * scaleFactor);
        int newHeight = (int) (originalHeight * scaleFactor);
        
        // Scale image
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = scaledImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        graphics.dispose();
        
        // Write as JPEG with compression
        ImageIO.write(scaledImage, "jpg", outputFile);
        
        log.info("Image compressed: {}x{} -> {}x{}", originalWidth, originalHeight, newWidth, newHeight);
    }

    private void compressVideo(File inputFile, File outputFile, Map<String, Object> profile) throws IOException, InterruptedException {
        int bitrate = (Integer) profile.get("bitrate");
        int height = (Integer) profile.get("height");
        String preset = (String) profile.get("preset");
        int crf = (Integer) profile.get("crf");
        
        String command = String.format(
            "-i %s -c:v libx264 -b:v %dk -vf scale=-2:%d -preset %s -crf %d -c:a aac -b:a 128k %s",
            inputFile.getAbsolutePath(),
            bitrate,
            height,
            preset,
            crf,
            outputFile.getAbsolutePath()
        );
        
        ffmpegExecutor.execute(command);
        log.info("Video compressed with profile: bitrate={}k, height={}, preset={}, crf={}", bitrate, height, preset, crf);
    }

    private void compressAudio(File inputFile, File outputFile, int audioBitrate) throws IOException, InterruptedException {
        String command = String.format(
            "-i %s -c:a libmp3lame -b:a %dk %s",
            inputFile.getAbsolutePath(),
            audioBitrate,
            outputFile.getAbsolutePath()
        );
        
        ffmpegExecutor.execute(command);
        log.info("Audio compressed with bitrate: {}k", audioBitrate);
    }

    private Map<String, Object> chooseProfile(String deviceCategory, String networkType, String compressionSpeed) {
        Map<String, Object> profile = new HashMap<>();
        
        // Default values
        int quality = 80;
        int bitrate = 1000;
        int height = 720;
        String preset = "medium";
        int crf = 23;
        int audioBitrate = 128;
        
        // Adjust based on device
        switch (deviceCategory.toLowerCase()) {
            case "mobile":
                quality = 60;
                bitrate = 500;
                height = 480;
                audioBitrate = 96;
                break;
            case "tablet":
                quality = 70;
                bitrate = 800;
                height = 720;
                audioBitrate = 112;
                break;
            case "desktop":
                quality = 85;
                bitrate = 1500;
                height = 1080;
                audioBitrate = 128;
                break;
        }
        
        // Adjust based on network
        switch (networkType.toLowerCase()) {
            case "2g":
            case "slow-2g":
                quality = Math.min(quality, 50);
                bitrate = Math.min(bitrate, 300);
                height = Math.min(height, 360);
                audioBitrate = Math.min(audioBitrate, 64);
                break;
            case "3g":
                quality = Math.min(quality, 65);
                bitrate = Math.min(bitrate, 600);
                height = Math.min(height, 480);
                audioBitrate = Math.min(audioBitrate, 96);
                break;
            case "4g":
                // Keep current values
                break;
            case "5g":
            case "wifi":
                quality = Math.min(quality + 10, 95);
                bitrate = Math.min(bitrate + 500, 2500);
                audioBitrate = Math.min(audioBitrate + 32, 192);
                break;
        }
        
        // Adjust based on compression speed
        switch (compressionSpeed.toLowerCase()) {
            case "fast":
                preset = "ultrafast";
                crf = 28;
                break;
            case "medium":
                preset = "medium";
                crf = 23;
                break;
            case "slow":
                preset = "slow";
                crf = 20;
                break;
        }
        
        profile.put("quality", quality);
        profile.put("bitrate", bitrate);
        profile.put("height", height);
        profile.put("preset", preset);
        profile.put("crf", crf);
        profile.put("audioBitrate", audioBitrate);
        
        return profile;
    }

    private Map<String, Object> getMediaMetadata(File file, String mediaType) {
        Map<String, Object> metadata = new HashMap<>();
        
        try {
            if (mediaType.equals("image")) {
                BufferedImage image = ImageIO.read(file);
                metadata.put("width", image.getWidth());
                metadata.put("height", image.getHeight());
                metadata.put("aspectRatio", String.format("%.2f", (double) image.getWidth() / image.getHeight()));
            } else if (mediaType.equals("video") || mediaType.equals("audio")) {
                // Use FFprobe to get metadata
                String command = String.format("-v quiet -print_format json -show_format -show_streams %s", file.getAbsolutePath());
                String output = ffmpegExecutor.executeProbe(command);
                
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(output);
                
                if (mediaType.equals("video")) {
                    JsonNode videoStream = root.path("streams").get(0);
                    metadata.put("width", videoStream.path("width").asInt());
                    metadata.put("height", videoStream.path("height").asInt());
                    metadata.put("duration", root.path("format").path("duration").asDouble());
                    metadata.put("bitrate", root.path("format").path("bit_rate").asInt() / 1000);
                    metadata.put("aspectRatio", videoStream.path("display_aspect_ratio").asText());
                } else {
                    metadata.put("duration", root.path("format").path("duration").asDouble());
                    metadata.put("bitrate", root.path("format").path("bit_rate").asInt() / 1000);
                    JsonNode audioStream = root.path("streams").get(0);
                    metadata.put("sampleRate", audioStream.path("sample_rate").asInt());
                    metadata.put("channels", audioStream.path("channels").asInt());
                }
            }
        } catch (Exception e) {
            log.error("Failed to extract metadata", e);
            metadata.put("error", "Failed to extract metadata: " + e.getMessage());
        }
        
        return metadata;
    }

    private void saveHistory(Map<String, Object> result) {
        try {
            CompressionHistory history = new CompressionHistory();
            history.setOriginalFilename((String) result.get("originalFilename"));
            history.setCompressedFilename((String) result.get("compressedFilename"));
            history.setOriginalSize((Long) result.get("originalSize"));
            history.setCompressedSize((Long) result.get("compressedSize"));
            history.setCompressionRatio((String) result.get("compressionRatio"));
            history.setCompressionPercentage((String) result.get("compressionPercentage"));
            history.setMediaType((String) result.get("mediaType"));
            history.setDeviceCategory((String) result.get("deviceCategory"));
            history.setNetworkType((String) result.get("networkType"));
            history.setCompressionSpeed((String) result.get("compressionSpeed"));
            history.setInputUrl((String) result.get("inputUrl"));
            history.setOutputUrl((String) result.get("outputUrl"));
            history.setStatus((String) result.get("status"));
            history.setCreatedAt(LocalDateTime.now());
            
            compressionHistoryRepository.save(history);
            log.info("Compression history saved for file: {}", result.get("originalFilename"));
        } catch (Exception e) {
            log.error("Failed to save compression history", e);
        }
    }

    private String classifyDeviceCategory(String userAgent) {
        if (userAgent == null) {
            return "desktop";
        }
        
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "mobile";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "tablet";
        } else {
            return "desktop";
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
