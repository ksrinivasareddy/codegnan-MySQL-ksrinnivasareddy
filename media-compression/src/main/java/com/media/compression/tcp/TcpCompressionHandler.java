package com.media.compression.tcp;

import com.media.compression.service.FFmpegExecutor;
import lombok.extern.slf4j.Slf4j;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
public class TcpCompressionHandler implements Runnable {

    private final Socket clientSocket;
    private final FFmpegExecutor ffmpegExecutor;

    public TcpCompressionHandler(Socket clientSocket, FFmpegExecutor ffmpegExecutor) {
        this.clientSocket = clientSocket;
        this.ffmpegExecutor = ffmpegExecutor;
    }

    @Override
    public void run() {
        log.info("🎬 Receiving data stream from client...");

        try {
            // Create necessary directories
            Files.createDirectories(Path.of("uploads/input"));
            Files.createDirectories(Path.of("uploads/output"));

            String sessionId = UUID.randomUUID().toString();
            Path inputFile = Path.of("uploads/input", "tcp_input_" + sessionId + ".mp4");
            Path outputFile = Path.of("uploads/output", "compressed_tcp_" + sessionId + ".mp4");

            // ✅ Receive the video data from client
            try (InputStream in = clientSocket.getInputStream();
                 OutputStream out = Files.newOutputStream(inputFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            log.info("✅ File received successfully: {}", inputFile);

            // ✅ Compress video with FFmpeg
            String[] cmd = {
                    ffmpegExecutor.getFfmpegPath(), "-y",
                    "-i", inputFile.toString(),
                    "-c:v", "libx264",
                    "-preset", "medium",
                    "-b:v", "1200k",
                    "-vf", "scale=-2:720",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    outputFile.toString()
            };

            log.info("✅ All chunks sent to FFmpeg. Waiting for compression to complete...");
            ffmpegExecutor.runAndCollect(cmd);

            log.info("🎉 TCP Compression completed: {}", outputFile);

            // ✅ Send acknowledgment to client
            try (OutputStream socketOut = clientSocket.getOutputStream()) {
                socketOut.write(("Compression successful: " + outputFile).getBytes());
            }

        } catch (Exception e) {
            log.error("❌ Error in TCP compression: {}", e.getMessage(), e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                log.warn("⚠️ Failed to close client socket: {}", e.getMessage());
            }
        }
    }
}
