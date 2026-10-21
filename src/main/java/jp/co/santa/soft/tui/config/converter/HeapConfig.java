package jp.co.santa.soft.tui.config.converter;

public record HeapConfig(String module, Integer xms, Integer xmx) {
  public static HeapConfig fromCsvLine(String line) {
    String[] fields = line.split(",");
    if (fields.length != 3) {
      throw new IllegalArgumentException("Invalid row format");
    }

    try {
      return new HeapConfig(
          fields[0].trim(), Integer.parseInt(fields[1].trim()), Integer.parseInt(fields[2].trim()));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid row format");
    }
  }
}
