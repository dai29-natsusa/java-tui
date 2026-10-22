package jp.co.santa.soft.tui.reducer;

import jp.co.santa.soft.tui.cmd.Cmd;
import jp.co.santa.soft.tui.core.model.AppState;

public record UpdateResult(AppState state, Cmd cmd) {
  public static UpdateResult only(AppState state) {
    return new UpdateResult(state, new Cmd.None());
  }

  public static UpdateResult of(AppState state, Cmd cmd) {
    return new UpdateResult(state, cmd);
  }
}
