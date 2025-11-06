package com.media.compression.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Defines the compression quality profile for each device + network type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompressionQualityProfile {

    private String device;
    private String network;
    private int targetHeight;
    private int targetBitrateKbps;
    private String preset;
    private String qualityLevel;
    private int crf;

    // ✅ Explicit getters and setters (Lombok will also generate, but this ensures no redlines)

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public void setTargetHeight(int targetHeight) {
        this.targetHeight = targetHeight;
    }

    public int getTargetBitrateKbps() {
        return targetBitrateKbps;
    }

    public void setTargetBitrateKbps(int targetBitrateKbps) {
        this.targetBitrateKbps = targetBitrateKbps;
    }

    public String getPreset() {
        return preset;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    public String getQualityLevel() {
        return qualityLevel;
    }

    public void setQualityLevel(String qualityLevel) {
        this.qualityLevel = qualityLevel;
    }

    public int getCrf() {
        return crf;
    }

    public void setCrf(int crf) {
        this.crf = crf;
    }
}
