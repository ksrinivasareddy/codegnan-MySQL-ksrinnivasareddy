package com.media.compression.tcp;

import com.media.compression.service.FFmpegExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
@Component
public class TcpCompressionServer implements Runnable {

    private final FFmpegExecutor ffmpegExecutor;
    private final int port = 9090;

    public TcpCompressionServer(FFmpegExecutor ffmpegExecutor) {
        this.ffmpegExecutor = ffmpegExecutor;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("🚀 TCP Compression Server started on port {}", port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.info("📡 New client connected: {}", clientSocket.getRemoteSocketAddress());

                // ✅ Now safe — manually create handler
                new Thread(new TcpCompressionHandler(clientSocket, ffmpegExecutor)).start();
            }

        } catch (IOException e) {
            log.error("❌ Error in TCP server: {}", e.getMessage(), e);
        }
    }
}
