package com.media.compression.util;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

@Slf4j
public class FileUtil {

    /**
     * ✅ Compress a JPEG image with the given quality level (0.0 to 1.0)
     */
    public static void compressJpeg(BufferedImage image, File outputFile, float quality) throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null for compression");
        }

        // Create output directory if missing
        if (outputFile.getParentFile() != null && !outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        // Get a JPEG writer
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG writers available");
        }
        ImageWriter writer = writers.next();

        // Set compression parameters
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality); // e.g., 0.6f = 60% quality
        }

        // Write compressed image
        try (OutputStream os = new FileOutputStream(outputFile);
             ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }

        log.info("🖼️ JPEG compressed at quality {} → {}", quality, outputFile.getAbsolutePath());
    }

    /**
     * ✅ Detect MIME type using file signature (magic bytes)
     */
    public static String detectMimeType(String filePath) {
        try {
            Path path = Path.of(filePath);
            String mime = Files.probeContentType(path);

            if (mime == null) {
                // Fallback: use URLConnection
                mime = URLConnection.guessContentTypeFromName(filePath);
            }

            // Fallback defaults for common extensions
            if (mime == null) {
                String lower = filePath.toLowerCase();
                if (lower.endsWith(".mp4")) mime = "video/mp4";
                else if (lower.endsWith(".mkv")) mime = "video/x-matroska";
                else if (lower.endsWith(".mov")) mime = "video/quicktime";
                else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) mime = "image/jpeg";
                else if (lower.endsWith(".png")) mime = "image/png";
                else if (lower.endsWith(".gif")) mime = "image/gif";
                else if (lower.endsWith(".webp")) mime = "image/webp";
                else if (lower.endsWith(".mp3")) mime = "audio/mpeg";
                else if (lower.endsWith(".wav")) mime = "audio/wav";
                else if (lower.endsWith(".aac")) mime = "audio/aac";
                else mime = "application/octet-stream";
            }

            log.debug("📄 Detected MIME type for {} → {}", filePath, mime);
            return mime;
        } catch (IOException e) {
            log.warn("❌ Failed to detect MIME type for {}: {}", filePath, e.getMessage());
            return "application/octet-stream";
        }
    }

    /**
     * ✅ Get file size in MB (accurate to decimals)
     */
    public static double getFileSizeMB(File file) {
        if (file == null || !file.exists()) return 0.0;
        return file.length() / (1024.0 * 1024.0);
    }

    /**
     * ✅ Copy an InputStream to a target file safely
     */
    public static void saveStreamToFile(InputStream input, File target) throws IOException {
        if (target.getParentFile() != null && !target.getParentFile().exists()) {
            target.getParentFile().mkdirs();
        }
        try (OutputStream out = new FileOutputStream(target)) {
            input.transferTo(out);
        }
    }
}
