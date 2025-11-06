package com.media.compression.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.media.compression.model.CompressionMetadata;
import com.media.compression.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Slf4j
@Service
public class UploadService {

    @Autowired
    private FFmpegExecutor ffmpegExecutor;

    @Value("${app.storage.input-dir:uploads/input}")
    private String inputDir;

    @Value("${app.storage.temp-dir:uploads/temp}")
    private String tempDir;

    @Value("${app.storage.output-dir:uploads/output}")
    private String outputDir;

    public Map<String, Object> prepareUpload(String filename, long sizeBytes, double durationSeconds,
                                             String mimeType, Map<String, Object> deviceInfo) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("allowed", true);
        resp.put("maxAllowedSizeBytes", 100L * 1024 * 1024); // 100 MB
        resp.put("maxAllowedDurationSec", 60);
        List<String> actions = new ArrayList<>();

        if (durationSeconds > 60) actions.add("trim");
        if (sizeBytes > 100L * 1024 * 1024) actions.add("compress");
        resp.put("actions", actions);
        resp.put("uploadId", UUID.randomUUID().toString());
        return resp;
    }

    public void saveChunk(String uploadId, int index, MultipartFile chunk) throws IOException {
        Path tempFolder = Path.of(tempDir, uploadId);
        Files.createDirectories(tempFolder);
        Path chunkFile = tempFolder.resolve(String.format("%05d.chunk", index));
        try (InputStream in = chunk.getInputStream();
             OutputStream os = Files.newOutputStream(chunkFile,
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            in.transferTo(os);
        }
        log.info("📦 Saved chunk {} for uploadId {}", index, uploadId);
    }

    public Path assembleChunks(String uploadId, String originalFilename) throws IOException {
        Path tempFolder = Path.of(tempDir, uploadId);
        if (!Files.exists(tempFolder))
            throw new FileNotFoundException("Upload temp folder not found: " + tempFolder);

        Path assembled = Path.of(inputDir, uploadId + "_" + originalFilename);
        Files.createDirectories(assembled.getParent());

        try (OutputStream out = Files.newOutputStream(assembled,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            Files.list(tempFolder).sorted().forEach(chunk -> {
                try {
                    Files.copy(chunk, out);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
        log.info("✅ Assembled file: {}", assembled);
        return assembled;
    }

    public CompressionMetadata finalizeAndProcess(Path inputPath) throws IOException {
        File inputFile = inputPath.toFile();
        Files.createDirectories(Path.of(outputDir));
        File outputFile = new File(outputDir, "compressed_" + inputFile.getName());

        Map<String, Object> meta = probeMedia(inputFile.getAbsolutePath());
        double duration = ((Number) meta.getOrDefault("duration", 0.0)).doubleValue();
        int bitrate = ((Number) meta.getOrDefault("bitrate", 0)).intValue();
        int width = ((Number) meta.getOrDefault("width", 0)).intValue();
        int height = ((Number) meta.getOrDefault("height", 0)).intValue();

        Path working = inputPath;

        // Trim if >60s
        if (duration > 60) {
            File trimmed = new File(inputFile.getParentFile(), "trimmed_" + inputFile.getName());
            List<String> cmd = List.of(
                    ffmpegExecutor.getFfmpegPath(), "-y",
                    "-i", inputFile.getAbsolutePath(),
                    "-t", "60", "-c", "copy",
                    trimmed.getAbsolutePath());
            ffmpegExecutor.runAndCollect(cmd);
            working = trimmed.toPath();
        }

        // Compress if >100MB
        double inputSizeMB = FileUtil.getFileSizeMB(working.toFile());
        if (inputSizeMB > 100.0) {
            long targetBytes = 100L * 1024 * 1024;
            double targetBitrateBps = (targetBytes * 8.0) / Math.max(1.0, duration);
            int targetVideoKbps = (int) Math.max(300, (targetBitrateBps / 1000.0) - 64);
            int targetHeight = Math.max(360, Math.min(720, height));

            List<String> cmd = List.of(
                    ffmpegExecutor.getFfmpegPath(), "-y",
                    "-i", working.toAbsolutePath().toString(),
                    "-c:v", "libx264",
                    "-b:v", targetVideoKbps + "k",
                    "-vf", "scale=-2:" + targetHeight,
                    "-c:a", "aac", "-b:a", "64k",
                    outputFile.getAbsolutePath());
            ffmpegExecutor.runAndCollect(cmd);
        } else {
            Files.copy(working, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        Map<String, Object> outMeta = probeMedia(outputFile.getAbsolutePath());
        double outDuration = ((Number) outMeta.getOrDefault("duration", duration)).doubleValue();
        int outWidth = ((Number) outMeta.getOrDefault("width", width)).intValue();
        int outHeight = ((Number) outMeta.getOrDefault("height", height)).intValue();
        int outBitrate = ((Number) outMeta.getOrDefault("bitrate", bitrate)).intValue();

        CompressionMetadata cm = new CompressionMetadata();
        String mime = FileUtil.detectMimeType(outputFile.getAbsolutePath());
        cm.setMediaType(mime.startsWith("video") ? "video" :
                mime.startsWith("audio") ? "audio" : "image");
        cm.setMimeType(mime);
        cm.setInputFile(inputFile.getAbsolutePath());
        cm.setOutputFile(outputFile.getAbsolutePath());
        cm.setOriginalSizeMB(FileUtil.getFileSizeMB(inputFile));
        cm.setCompressedSizeMB(FileUtil.getFileSizeMB(outputFile));
        cm.setCompressionRatio(1 - (cm.getCompressedSizeMB() / Math.max(0.000001, cm.getOriginalSizeMB())));
        cm.setCompressionPercent(cm.getCompressionRatio() * 100);
        cm.setOriginalWidth(width);
        cm.setOriginalHeight(height);
        cm.setCompressedWidth(outWidth);
        cm.setCompressedHeight(outHeight);
        cm.setDurationSeconds(outDuration);
        cm.setBitrateKbps(outBitrate / 1000);
        cm.setMessage("✅ Compression completed successfully");

        return cm;
    }

    public Map<String, Object> probeMedia(String path) {
        Map<String, Object> m = new HashMap<>();
        try {
            List<String> cmd = List.of(
                    ffmpegExecutor.getFfprobePath(), "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=width,height,bit_rate,avg_frame_rate,duration",
                    "-of", "json", path);

            String out = ffmpegExecutor.runAndCollect(cmd);
            ObjectMapper mapper = new ObjectMapper();
            Map<?, ?> json = mapper.readValue(out, Map.class);
            List<Map<String, Object>> streams = (List<Map<String, Object>>) json.get("streams");

            if (streams != null && !streams.isEmpty()) {
                Map<String, Object> s = streams.get(0);
                m.put("width", s.getOrDefault("width", 0));
                m.put("height", s.getOrDefault("height", 0));
                m.put("bitrate", s.getOrDefault("bit_rate", 0));
                m.put("duration", s.getOrDefault("duration", 0.0));

                Object fr = s.get("avg_frame_rate");
                if (fr instanceof String str && str.contains("/")) {
                    try {
                        String[] p = str.split("/");
                        double val = Double.parseDouble(p[0]) / Double.parseDouble(p[1]);
                        m.put("framerate", val);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ ffprobe failed for {}: {}", path, e.getMessage());
        }
        return m;
    }
}
