package com.media.compression.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompressionMetadata {
    
    // Media Type Info
    private String mediaType; // video, image, audio
    private String mimeType;
    
    // File Paths
    private String inputFile;
    private String outputFile;
    private String outputFileUrl; // Web accessible URL
    private String outputFilePath; // Download URL
    
    // Size Information (MB)
    private double originalSizeMB;
    private double compressedSizeMB;
    private double compressionRatio; // 0.0 to 1.0
    private double compressionPercent; // 0 to 100
    
    // Image/Video Dimensions
    private int originalWidth;
    private int originalHeight;
    private int compressedWidth;
    private int compressedHeight;
    private String originalAspect;
    private String compressedAspect;
    
    // Video/Audio Properties
    private double durationSeconds;
    private double frameRate;
    private double bitrateKbps;
    private double compressedBitrateKbps;
    
    // Device & Network Info
    private String device;
    private String deviceCategory;
    private String network;
    private double speedMbps;
    private String networkCategory;
    
    // Status Info
    private String message;
    private String status; // SUCCESS, FAILED, PARTIAL
    private String errorMessage;
    private long processingTimeMs;
    
    // Thumbnail
    private String thumbnailPath;
    
    // For JSON storage
    private String metadata;
}
