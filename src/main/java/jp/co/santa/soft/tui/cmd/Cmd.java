package jp.co.santa.soft.tui.cmd;

import java.util.List;
import jp.co.santa.soft.tui.core.model.UnitTarget;

public sealed interface Cmd {
  record None() implements Cmd {}

  record Batch(List<Cmd> cmdList) implements Cmd {}

  record RunPipelineStep(int stepId, UnitTarget target) implements Cmd {}

  record ControlDaemon(String daemonName, String action, UnitTarget target) implements Cmd {}

  record UnregisterSshKey(UnitTarget target) implements Cmd {}
}
