package com.media.compression;

import com.media.compression.service.FFmpegExecutor;
import com.media.compression.tcp.TcpCompressionServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class MediaCompressionApplication {

    @Value("${app.tcp.enabled:true}")
    private boolean tcpEnabled;

    public static void main(String[] args) {
        SpringApplication.run(MediaCompressionApplication.class, args);
    }

    /**
     * ✅ Validate FFmpeg installation on startup
     */
    @Bean
    public CommandLineRunner validateFFmpeg(FFmpegExecutor ffmpegExecutor) {
        return args -> {
            log.info("=".repeat(60));
            log.info("🚀 Media Compression Service Starting...");
            log.info("=".repeat(60));
            ffmpegExecutor.validateInstallation();
        };
    }

    /**
     * ✅ Automatically starts TCP server on app startup
     */
    @Bean
    public CommandLineRunner startTcpServer(TcpCompressionServer tcpServer) {
        return args -> {
            if (tcpEnabled) {
                Thread tcpThread = new Thread(tcpServer);
                tcpThread.setDaemon(true); // won't block app shutdown
                tcpThread.start();
                log.info("🚀 TCP Compression Server started automatically on port 9090!");
            } else {
                log.info("⚠️ TCP Server is disabled in configuration");
            }
        };
    }
}
