package jp.co.santa.soft.tui.config;

import java.util.Map;
import jp.co.santa.soft.tui.config.state.UnitRecord;

public record UnitConfigState(Map<String, UnitRecord> units) {}
