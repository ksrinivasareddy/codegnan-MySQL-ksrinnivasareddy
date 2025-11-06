package com.media.compression.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "compression_history")
public class CompressionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // File Information
    private String fileName;
    private String fileExtension;
    private String mimeType;
    private String mediaType;

    // Size Information (in MB)
    private Double originalSize;
    private Double compressedSize;
    private Double compressionRatio;
    private Double compressionPercent;

    // File Paths
    private String inputPath;
    private String outputPath;

    // Device Information
    private String deviceType;
    private String deviceCategory; // Mobile, Desktop, Tablet
    
    // Network Information
    private String networkType; // 2G, 3G, 4G, 5G, WiFi, Ethernet
    private Double networkSpeedMbps;
    private String networkCategory; // Based on speed
    
    // Media Specific
    private Integer originalWidth;
    private Integer originalHeight;
    private Integer compressedWidth;
    private Integer compressedHeight;
    private String originalAspectRatio;
    private String compressedAspectRatio;
    
    private Double originalDuration; // For video in seconds
    private Double originalBitrate; // For video/audio
    private Double compressedBitrate;
    private Double originalFrameRate; // For video

    // Metadata JSON
    @Column(columnDefinition = "LONGTEXT")
    private String jsonData;

    // Status & Timestamps
    @Enumerated(EnumType.STRING)
    private CompressionStatus status; // SUCCESS, FAILED, PARTIAL
    private String errorMessage;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long processingTimeMs; // Time taken to compress in milliseconds

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = CompressionStatus.SUCCESS;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enum for status
    public enum CompressionStatus {
        SUCCESS, FAILED, PARTIAL, PENDING
    }
}
