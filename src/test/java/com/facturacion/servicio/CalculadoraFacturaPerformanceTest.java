package com.facturacion.servicio;

import com.facturacion.descuento.DescuentoPorcentaje;
import com.facturacion.descuento.EstrategiaDescuento;
import com.facturacion.modelo.Cliente;
import com.facturacion.modelo.DetalleFactura;
import com.facturacion.modelo.Factura;
import com.facturacion.modelo.Producto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PERFORMANCE UNIT TEST (prueba de rendimiento).
 *
 * A diferencia de las pruebas anteriores, aquí NO nos interesa si el
 * resultado del cálculo es correcto (eso ya lo cubren las pruebas
 * funcionales); nos interesa que la operación se ejecute dentro de
 * un tiempo razonable, incluso con un volumen grande de datos.
 *
 * IMPORTANTE (nota realista):
 *   - Este tipo de prueba es más "frágil" que una prueba funcional:
 *     el resultado depende de la máquina donde se ejecuta (CPU,
 *     carga del sistema, JIT warm-up, etc.).
 *   - Por eso se usa un umbral generoso (ej. "menos de 1 segundo"),
 *     no un valor ajustado al límite.
 *   - En proyectos reales de mayor escala, este tipo de medición
 *     suele hacerse con herramientas especializadas como JMH (Java
 *     Microbenchmark Harness) en vez de mediciones manuales con
 *     System.nanoTime(), porque JMH controla mejor el "calentamiento"
 *     de la JVM y produce medidas más confiables.
 *   - Es buena práctica separar estas pruebas de las pruebas
 *     unitarias normales (por ejemplo, en un source set aparte o con
 *     una etiqueta @Tag("performance")) para no ralentizar la
 *     ejecución habitual de `gradle test`.
 */
class CalculadoraFacturaPerformanceTest {

    @Test
    void debeCalcularUnaFacturaConMuchosDetallesEnTiempoRazonable() {
        // Arrange: se construye una factura "grande" con 50,000 líneas
        // de detalle y una estrategia de descuento real.
        Cliente cliente = new Cliente("CLI-999", "Cliente de Carga", false);
        Factura factura = new Factura("FAC-PERF-001", cliente);

        for (int i = 0; i < 50_000; i++) {
            Producto producto = new Producto("P" + i, "Producto " + i, 10.0);
            factura.agregarDetalle(new DetalleFactura(producto, 1));
        }

        CalculadoraFactura calculadora = new CalculadoraFactura();
        List<EstrategiaDescuento> estrategias = List.of(new DescuentoPorcentaje(0.10, 100.0));

        // Act: medimos el tiempo que toma el cálculo
        long inicioNanos = System.nanoTime();
        calculadora.calcular(factura, estrategias);
        long duracionMilisegundos = (System.nanoTime() - inicioNanos) / 1_000_000;

        // Assert: el cálculo no debería tardar más de 1 segundo,
        // incluso con 50,000 líneas de detalle. El umbral es
        // deliberadamente generoso para evitar pruebas inestables
        // ("flaky tests") por variaciones normales de la máquina.
        assertTrue(duracionMilisegundos < 1000,
                "El cálculo tardó " + duracionMilisegundos + "ms, se esperaba menos de 1000ms");
    }
}
