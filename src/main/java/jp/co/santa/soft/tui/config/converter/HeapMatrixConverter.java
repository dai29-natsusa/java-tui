package jp.co.santa.soft.tui.config.converter;

import java.io.Serial;
import java.util.stream.Stream;
import org.eclipse.microprofile.config.spi.Converter;

public class HeapMatrixConverter implements Converter<HeapConfig[]> {

  @Serial private static final long serialVersionUID = -170912801333030788L;

  public HeapMatrixConverter() {}

  @Override
  public HeapConfig[] convert(String value) throws IllegalArgumentException, NullPointerException {
    if (value == null || value.trim().isEmpty()) {
      return new HeapConfig[0];
    }

    String[] lines = value.split("\\r?\\n");

    return Stream.of(lines)
        .map(String::trim)
        .filter(l -> !l.isBlank() && !l.startsWith("#"))
        .map(HeapConfig::fromCsvLine)
        .toArray(HeapConfig[]::new);
  }
}
