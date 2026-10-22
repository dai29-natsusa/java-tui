package jp.co.santa.soft.tui.reducer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import jp.co.santa.soft.tui.cmd.Cmd;
import jp.co.santa.soft.tui.core.model.ActivePane;
import jp.co.santa.soft.tui.core.model.AppState;
import jp.co.santa.soft.tui.core.model.DaemonEntry;
import jp.co.santa.soft.tui.core.model.PipelineStatus;
import jp.co.santa.soft.tui.core.model.UnitTarget;
import jp.co.santa.soft.tui.msg.AppMsg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppReducerTest {

  private AppState initialState;

  @BeforeEach
  void setUp() {
    List<UnitTarget> units =
        List.of(
            new UnitTarget("unit01", "xmcs1", "192.168.40.1", "ol8xmcs1", "/home/xmcs/XMCS", true),
            new UnitTarget(
                "unit02", "xmcs2", "192.168.40.2", "ol8xmcs2", "/home/xmcs/XMCS", false));

    List<DaemonEntry> daemons =
        List.of(
            new DaemonEntry("Core", "scripts/core.sh", 1001, true),
            new DaemonEntry("Client", "scripts/client.sh", 1002, false));

    initialState = AppState.initial(units, daemons);
  }

  @Test
  void shouldCycleActivePanesForwardAndBackward() {
    assertEquals(ActivePane.PIPELINE, initialState.activePane());

    UpdateResult r1 = AppReducer.update(initialState, new AppMsg.NextPane());
    assertEquals(ActivePane.FILES, r1.state().activePane());

    UpdateResult r2 = AppReducer.update(r1.state(), new AppMsg.NextPane());
    assertEquals(ActivePane.SERVICES, r2.state().activePane());

    UpdateResult r3 = AppReducer.update(r2.state(), new AppMsg.PrevPane());
    assertEquals(ActivePane.FILES, r3.state().activePane());
  }

  @Test
  void shouldNavigateItemsWithinActivePane() {
    assertEquals(0, initialState.selectedStepIndex());

    UpdateResult r1 = AppReducer.update(initialState, new AppMsg.NextItem());
    assertEquals(1, r1.state().selectedStepIndex());

    UpdateResult r2 = AppReducer.update(r1.state(), new AppMsg.PrevItem());
    assertEquals(0, r2.state().selectedStepIndex());
  }

  @Test
  void shouldTriggerPipelineStepAndReturnCommand() {
    UpdateResult result = AppReducer.update(initialState, new AppMsg.TriggerCurrentAction());

    assertEquals(PipelineStatus.RUNNING, result.state().pipelineSteps().getFirst().status());
    assertInstanceOf(Cmd.RunPipelineStep.class, result.cmd());

    Cmd.RunPipelineStep stepCmd = (Cmd.RunPipelineStep) result.cmd();
    assertEquals(1, stepCmd.stepId());
    assertEquals("unit01", stepCmd.target().id());
  }

  @Test
  void shouldHandleSystemEventsAndProducePureState() {
    UpdateResult r1 = AppReducer.update(initialState, new AppMsg.StepProgress(1, "75% complete"));
    assertTrue(r1.state().logLines().stream().anyMatch(l -> l.contains("75% complete")));

    UpdateResult r2 =
        AppReducer.update(
            r1.state(), new AppMsg.StepCompleted(1, PipelineStatus.SUCCESS, "Validated 3 jars"));
    assertEquals(PipelineStatus.SUCCESS, r2.state().pipelineSteps().getFirst().status());
    assertTrue(r2.state().logLines().stream().anyMatch(l -> l.contains("Validated 3 jars")));
  }

  @Test
  void shouldOpenQuitModalAndConfirmExitWithKeyUnregistration() {
    assertFalse(initialState.quitConfirmationModal());

    UpdateResult r1 = AppReducer.update(initialState, new AppMsg.RequestQuit());
    assertTrue(r1.state().quitConfirmationModal());
    assertFalse(r1.state().shouldExit());

    UpdateResult r2 = AppReducer.update(r1.state(), new AppMsg.ConfirmQuit(true));
    assertFalse(r2.state().quitConfirmationModal());
    assertTrue(r2.state().shouldExit());
    assertInstanceOf(Cmd.UnregisterSshKey.class, r2.cmd());
  }
}
