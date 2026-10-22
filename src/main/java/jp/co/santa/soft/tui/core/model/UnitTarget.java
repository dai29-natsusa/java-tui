package jp.co.santa.soft.tui.core.model;

public record UnitTarget(
    String id, String name, String ip, String hostname, String workdir, boolean active) {}
