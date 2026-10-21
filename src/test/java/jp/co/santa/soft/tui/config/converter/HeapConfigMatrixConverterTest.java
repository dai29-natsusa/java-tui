package jp.co.santa.soft.tui.config.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class HeapConfigMatrixConverterTest {
  private final HeapMatrixConverter converter = new HeapMatrixConverter();

  @Test
  void givenRegularMatrix_whenConvert_thenReturnMatrix() {
    String csv =
        """
      # module,  xms,  xms
         CACHE,  128, 2048
          CORE,  512, 1024
        CLIENT, 1024, 2048
      """;

    var convertedMatrix = converter.convert(csv);
    assertEquals(3, convertedMatrix.length);
    assertEquals("CACHE", convertedMatrix[0].module());
    assertEquals(128, convertedMatrix[0].xms());
    assertEquals(2048, convertedMatrix[0].xmx());

    assertEquals("CORE", convertedMatrix[1].module());
    assertEquals(512, convertedMatrix[1].xms());
    assertEquals(1024, convertedMatrix[1].xmx());

    assertEquals("CLIENT", convertedMatrix[2].module());
    assertEquals(1024, convertedMatrix[2].xms());
    assertEquals(2048, convertedMatrix[2].xmx());
  }

  @Test
  void givenTwoColumnsRow_whenConvert_thenThrowIllegalArgumentException() {
    String csv =
        """
            # module,  xms,  xms
               CACHE,  128, 2048
                CORE,  512
            """;

    assertThrows(IllegalArgumentException.class, () -> converter.convert(csv));
  }

  @Test
  void givenNonNumeric_whenConvert_thenThrowIllegalArgumentException() {
    String csv =
        """
            # module,  xms,  xms
               CACHE,  128, 2048
                CORE,  512, ONE_Yen
            """;

    assertThrows(IllegalArgumentException.class, () -> converter.convert(csv));
  }
}
