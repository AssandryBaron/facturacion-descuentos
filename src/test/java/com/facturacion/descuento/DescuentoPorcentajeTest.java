package com.facturacion.descuento;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PRUEBAS UNITARIAS AVANZADAS: pruebas parametrizadas.
 *
 * En vez de escribir un método @Test por cada combinación de datos,
 * usamos @ParameterizedTest + @CsvSource: JUnit ejecuta el MISMO
 * método una vez por cada fila de datos, sustituyendo los parámetros.
 *
 * Esto sigue siendo AAA (Arrange - Act - Assert) por cada ejecución,
 * pero evita duplicar código de prueba para cada caso.
 */
class DescuentoPorcentajeTest {

    @ParameterizedTest(name = "subtotal={0}, porcentaje={1} -> descuento esperado={2}")
    @CsvSource({
            "100.0, 0.10, 10.0",
            "200.0, 0.20, 40.0",
            "50.0,  0.05, 2.5",
            "1000.0, 0.15, 150.0"
    })
    void debeCalcularDescuentoPorcentajeParaVariosCasos(double subtotal, double porcentaje, double descuentoEsperado) {
        // Arrange: se crea la estrategia con el porcentaje del caso actual,
        // sin monto mínimo (siempre aplicable)
        DescuentoPorcentaje estrategia = new DescuentoPorcentaje(porcentaje, 0.0);

        // Act
        double descuentoCalculado = estrategia.calcularDescuento(null, subtotal);

        // Assert
        assertEquals(descuentoEsperado, descuentoCalculado, 0.001);
    }

    @ParameterizedTest(name = "subtotal={0}, mínimo={1} -> aplicable={2}")
    @CsvSource({
            "100.0, 200.0, false", // subtotal por debajo del mínimo: NO aplica
            "200.0, 200.0, true",  // subtotal igual al mínimo: SÍ aplica
            "300.0, 200.0, true"   // subtotal por encima del mínimo: SÍ aplica
    })
    void debeRespetarElMontoMinimoParaAplicarElDescuento(double subtotal, double montoMinimo, boolean esperadoAplicable) {
        // Arrange
        DescuentoPorcentaje estrategia = new DescuentoPorcentaje(0.10, montoMinimo);

        // Act
        boolean esAplicable = estrategia.esAplicable(null, subtotal);

        // Assert
        if (esperadoAplicable) {
            assertTrue(esAplicable);
        } else {
            assertFalse(esAplicable);
        }
    }
}
