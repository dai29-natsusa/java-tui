package jp.co.santa.soft.tui.config.state;

import java.util.List;
import jp.co.santa.soft.tui.config.converter.HeapConfig;

public record UnitRecord(
    String project,
    int id,
    String name,
    String ip,
    String hostname,
    String workdir,
    List<UserRecord> users,
    List<ServiceRecord> services,
    List<InstanceRecord> instance,
    HeapConfig[] heap) {}
