package com.shield.module.file.service;

public record FileScanResult(boolean safe, String reason) {

    public static FileScanResult clean() {
        return new FileScanResult(true, "CLEAN");
    }

    public static FileScanResult rejected(String reason) {
        return new FileScanResult(false, reason);
    }
}
