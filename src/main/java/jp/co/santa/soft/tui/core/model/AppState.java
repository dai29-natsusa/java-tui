package jp.co.santa.soft.tui.core.model;

import java.util.ArrayList;
import java.util.List;

public record AppState(
    ActivePane activePane,
    int selectedUnitIndex,
    int selectedStepIndex,
    int selectedFileIndex,
    int selectedServiceIndex,
    List<UnitTarget> units,
    List<PipelineStep> pipelineSteps,
    List<ConfigFileEntry> configFiles,
    List<DaemonEntry> daemons,
    List<String> logLines,
    boolean quitConfirmationModal,
    boolean shouldExit) {
  public static AppState initial(List<UnitTarget> units, List<DaemonEntry> daemons) {
    List<PipelineStep> initialSteps =
        List.of(
            new PipelineStep(
                1,
                "Check Deployment Packages",
                "Verify .zip or instance folder structure",
                PipelineStatus.PENDING,
                "Waiting to run..."),
            new PipelineStep(
                2,
                "SSH Keys & Remote Register",
                "Generate Ed25519 keys & inject into remote authorized_keys",
                PipelineStatus.PENDING,
                "Waiting to run..."),
            new PipelineStep(
                3,
                "Deploy Packages (SCP)",
                "Upload packages to target server via SCP",
                PipelineStatus.PENDING,
                "Waiting to run..."),
            new PipelineStep(
                4,
                "Patch Configurations",
                "Auto-edit jdbc.properties, xmcs-nodes.properties, permissions",
                PipelineStatus.PENDING,
                "Waiting to run..."),
            new PipelineStep(
                5,
                "Initial Scripts & DB Setup",
                "Execute Oracle schema init & stored procedure SP_DEL_INS_PART",
                PipelineStatus.PENDING,
                "Waiting to run..."));

    List<ConfigFileEntry> initialFiles =
        List.of(
            new ConfigFileEntry(
                "jdbc.properties",
                "conf/jdbc.properties",
                "xmcs",
                "Pending",
                "xmcs.db.hostname=\nxmcs.db.user=\nxmcs.db.passwd="),
            new ConfigFileEntry(
                "xmcs-nodes.properties",
                "conf/xmcs-nodes.properties",
                "xmcs",
                "Pending",
                "xmcs.node.ip.1=5701\nxmcs.node.ip.2=5701"),
            new ConfigFileEntry(
                "01-schema-init.sql",
                "db/01-schema-init.sql",
                "oracle",
                "Pending",
                "-- Oracle schema initialization"),
            new ConfigFileEntry(
                ".bash_profile",
                "~/.bash_profile",
                "xmcs",
                "Pending",
                "export XMCS_HOME=/home/xmcs/XMCS"));

    return new AppState(
        ActivePane.PIPELINE,
        0,
        0,
        0,
        0,
        units,
        initialSteps,
        initialFiles,
        daemons,
        List.of("System initialized. Ready for deployment pipeline."),
        false,
        false);
  }

  public UnitTarget currentUnit() {
    if (units == null || units.isEmpty()) {
      return new UnitTarget("default", "unit0", "127.0.0.1", "localhost", "/tmp", true);
    }
    int clampedIndex = Math.clamp(selectedUnitIndex, 0, units.size() - 1);
    return units.get(clampedIndex);
  }

  public AppState withActivePane(ActivePane pane) {
    return new AppState(
        pane,
        selectedUnitIndex,
        selectedStepIndex,
        selectedFileIndex,
        selectedServiceIndex,
        units,
        pipelineSteps,
        configFiles,
        daemons,
        logLines,
        quitConfirmationModal,
        shouldExit);
  }

  public AppState withSelectedStepIndex(int index) {
    int max = pipelineSteps.isEmpty() ? 0 : pipelineSteps.size() - 1;
    return new AppState(
        activePane,
        selectedUnitIndex,
        Math.clamp(index, 0, max),
        selectedFileIndex,
        selectedServiceIndex,
        units,
        pipelineSteps,
        configFiles,
        daemons,
        logLines,
        quitConfirmationModal,
        shouldExit);
  }

  public AppState withSelectedFileIndex(int index) {
    int max = configFiles.isEmpty() ? 0 : configFiles.size() - 1;
    return new AppState(
        activePane,
        selectedUnitIndex,
        selectedStepIndex,
        Math.clamp(index, 0, max),
        selectedServiceIndex,
        units,
        pipelineSteps,
        configFiles,
        daemons,
        logLines,
        quitConfirmationModal,
        shouldExit);
  }

  public AppState withSelectedServiceIndex(int index) {
    int max = daemons.isEmpty() ? 0 : daemons.size() - 1;
    return new AppState(
        activePane,
        selectedUnitIndex,
        selectedStepIndex,
        selectedFileIndex,
        Math.clamp(index, 0, max),
        units,
        pipelineSteps,
        configFiles,
        daemons,
        logLines,
        quitConfirmationModal,
        shouldExit);
  }

  public AppState withSelectedUnitIndex(int index) {
    int max = units.isEmpty() ? 0 : units.size() - 1;
    return new AppState(
        activePane,
        Math.clamp(index, 0, max),
        selectedStepIndex,
        selectedFileIndex,
        selectedServiceIndex,
        units,
        pipelineSteps,
        configFiles,
        daemons,
        logLines,
        quitConfirmationModal,
        shouldExit);
  }

  public AppState withPipelineSteps(List<PipelineStep> steps) {
    return new AppState(
        activePane,
        selectedUnitIndex,
        selectedStepIndex,
        selectedFileIndex,
        selectedServiceIndex,
        units,
        steps,
        configFiles,
        daemons,
        logLines,
        quitConfirmationModal,
        shouldExit);
  }

  public AppState withDaemons(List<DaemonEntry> updatedDaemons) {
    return new AppState(
        activePane,
        selectedUnitIndex,
        selectedStepIndex,
        selectedFileIndex,
        selectedServiceIndex,
        units,
        pipelineSteps,
        configFiles,
        updatedDaemons,
        logLines,
        quitConfirmationModal,
        shouldExit);
  }

  public AppState withAppendedLog(String line) {
    List<String> newLogs = new ArrayList<>(logLines);
    newLogs.add(line);
    if (newLogs.size() > 500) {
      newLogs.removeFirst();
    }
    return new AppState(
        activePane,
        selectedUnitIndex,
        selectedStepIndex,
        selectedFileIndex,
        selectedServiceIndex,
        units,
        pipelineSteps,
        configFiles,
        daemons,
        List.copyOf(newLogs),
        quitConfirmationModal,
        shouldExit);
  }

  public AppState withQuitModal(boolean showModal) {
    return new AppState(
        activePane,
        selectedUnitIndex,
        selectedStepIndex,
        selectedFileIndex,
        selectedServiceIndex,
        units,
        pipelineSteps,
        configFiles,
        daemons,
        logLines,
        showModal,
        shouldExit);
  }

  public AppState withShouldExit(boolean exit) {
    return new AppState(
        activePane,
        selectedUnitIndex,
        selectedStepIndex,
        selectedFileIndex,
        selectedServiceIndex,
        units,
        pipelineSteps,
        configFiles,
        daemons,
        logLines,
        quitConfirmationModal,
        exit);
  }
}
