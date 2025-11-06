package com.media.compression.model;

public class CompressionResult {

    private String mediaType;
    private String inputFile;
    private String outputFile;

    private double originalSizeMB;
    private double compressedSizeMB;
    private double compressionRatio;
    private String message;

    private int originalWidth;
    private int originalHeight;
    private int compressedWidth;
    private int compressedHeight;
    private String originalAspect;
    private String compressedAspect;
    private String location;

    public CompressionResult() {}

    public CompressionResult(String mediaType, String inputFile, String outputFile,
                             double originalSizeMB, double compressedSizeMB,
                             double compressionRatio, String message,
                             int originalWidth, int originalHeight,
                             int compressedWidth, int compressedHeight,
                             String originalAspect, String compressedAspect,
                             String location) {
        this.mediaType = mediaType;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.originalSizeMB = originalSizeMB;
        this.compressedSizeMB = compressedSizeMB;
        this.compressionRatio = compressionRatio;
        this.message = message;
        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
        this.compressedWidth = compressedWidth;
        this.compressedHeight = compressedHeight;
        this.originalAspect = originalAspect;
        this.compressedAspect = compressedAspect;
        this.location = location;
    }

    // Getters and Setters
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getInputFile() { return inputFile; }
    public void setInputFile(String inputFile) { this.inputFile = inputFile; }

    public String getOutputFile() { return outputFile; }
    public void setOutputFile(String outputFile) { this.outputFile = outputFile; }

    public double getOriginalSizeMB() { return originalSizeMB; }
    public void setOriginalSizeMB(double originalSizeMB) { this.originalSizeMB = originalSizeMB; }

    public double getCompressedSizeMB() { return compressedSizeMB; }
    public void setCompressedSizeMB(double compressedSizeMB) { this.compressedSizeMB = compressedSizeMB; }

    public double getCompressionRatio() { return compressionRatio; }
    public void setCompressionRatio(double compressionRatio) { this.compressionRatio = compressionRatio; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public int getOriginalWidth() { return originalWidth; }
    public void setOriginalWidth(int originalWidth) { this.originalWidth = originalWidth; }

    public int getOriginalHeight() { return originalHeight; }
    public void setOriginalHeight(int originalHeight) { this.originalHeight = originalHeight; }

    public int getCompressedWidth() { return compressedWidth; }
    public void setCompressedWidth(int compressedWidth) { this.compressedWidth = compressedWidth; }

    public int getCompressedHeight() { return compressedHeight; }
    public void setCompressedHeight(int compressedHeight) { this.compressedHeight = compressedHeight; }

    public String getOriginalAspect() { return originalAspect; }
    public void setOriginalAspect(String originalAspect) { this.originalAspect = originalAspect; }

    public String getCompressedAspect() { return compressedAspect; }
    public void setCompressedAspect(String compressedAspect) { this.compressedAspect = compressedAspect; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
