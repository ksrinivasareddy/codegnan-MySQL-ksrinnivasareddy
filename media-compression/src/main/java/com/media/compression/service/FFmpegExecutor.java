package com.media.compression.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ✅ FFmpeg Command Executor Service
 * Handles all FFmpeg and FFprobe command executions
 */
@Slf4j
@Service
public class FFmpegExecutor {

    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    @Value("${app.ffprobe.path:ffprobe}")
    private String ffprobePath;

    /**
     * Get FFmpeg executable path
     */
    public String getFfmpegPath() {
        return ffmpegPath;
    }

    /**
     * Get FFprobe executable path
     */
    public String getFfprobePath() {
        return ffprobePath;
    }

    /**
     * Execute FFmpeg command and collect output
     */
    public String runAndCollect(String... command) throws IOException, InterruptedException {
        return runAndCollect(List.of(command));
    }

    /**
     * Execute FFmpeg command with list of arguments
     */
    public String runAndCollect(List<String> command) throws IOException, InterruptedException {
        log.info("🎬 Executing FFmpeg command: {}", String.join(" ", command));
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
                log.debug("FFmpeg: {}", line);
            }
        }
        
        boolean finished = process.waitFor(10, TimeUnit.MINUTES);
        
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("FFmpeg process timeout after 10 minutes");
        }
        
        int exitCode = process.exitValue();
        
        if (exitCode != 0) {
            log.error("❌ FFmpeg failed with exit code: {}", exitCode);
            log.error("Output: {}", output);
            throw new IOException("FFmpeg process failed with exit code: " + exitCode);
        }
        
        log.info("✅ FFmpeg command completed successfully");
        return output.toString();
    }

    /**
     * Execute FFmpeg command without collecting output (for large files)
     */
    public void runWithoutCollect(List<String> command) throws IOException, InterruptedException {
        log.info("🎬 Executing FFmpeg command (no output collection): {}", 
                String.join(" ", command));
        
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.inheritIO(); // Stream output directly to console
        
        Process process = pb.start();
        
        boolean finished = process.waitFor(30, TimeUnit.MINUTES);
        
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("FFmpeg process timeout after 30 minutes");
        }
        
        int exitCode = process.exitValue();
        
        if (exitCode != 0) {
            throw new IOException("FFmpeg process failed with exit code: " + exitCode);
        }
        
        log.info("✅ FFmpeg command completed successfully");
    }

    /**
     * Check if FFmpeg is available
     */
    public boolean isFFmpegAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(ffmpegPath, "-version");
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            log.warn("⚠️ FFmpeg not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if FFprobe is available
     */
    public boolean isFFprobeAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder(ffprobePath, "-version");
            Process process = pb.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            log.warn("⚠️ FFprobe not available: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validate FFmpeg installation on startup
     */
    public void validateInstallation() {
        log.info("🔍 Validating FFmpeg installation...");
        
        if (!isFFmpegAvailable()) {
            log.error("❌ FFmpeg is not available at: {}", ffmpegPath);
            log.error("Please install FFmpeg: https://ffmpeg.org/download.html");
        } else {
            log.info("✅ FFmpeg is available at: {}", ffmpegPath);
        }
        
        if (!isFFprobeAvailable()) {
            log.error("❌ FFprobe is not available at: {}", ffprobePath);
        } else {
            log.info("✅ FFprobe is available at: {}", ffprobePath);
        }
    }
}
