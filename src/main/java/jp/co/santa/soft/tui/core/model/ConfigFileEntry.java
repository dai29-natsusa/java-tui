package jp.co.santa.soft.tui.core.model;

public record ConfigFileEntry(
    String name, String path, String userRole, String status, String contentPreview) {}
