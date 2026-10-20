package com.facturacion.modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * PRUEBAS UNITARIAS SIMPLES sobre DetalleFactura.
 *
 * Son "simples" porque cada prueba:
 *   - Prueba una sola unidad de código (un método de una sola clase).
 *   - No depende de otras clases del sistema (no hay colaboradores).
 *   - Tiene una entrada clara y una salida clara, con un solo assert
 *     principal.
 *
 * Todas siguen el patrón AAA (Arrange - Act - Assert), marcado
 * explícitamente en los comentarios de cada método.
 */
class DetalleFacturaTest {

    // ---------------------------------------------------------------
    // HAPPY PATH: escenario normal, con datos válidos.
    // ---------------------------------------------------------------
    @Test
    void debeCalcularSubtotalCorrectamenteConDatosValidos() {
        // Arrange: preparamos un producto y una cantidad válidos
        Producto producto = new Producto("P1", "Teclado mecánico", 50.0);
        DetalleFactura detalle = new DetalleFactura(producto, 3);

        // Act: ejecutamos la operación que queremos probar
        double subtotal = detalle.calcularSubtotal();

        // Assert: verificamos el resultado esperado
        assertEquals(150.0, subtotal, 0.001);
    }

    @Test
    void debeCalcularSubtotalConCantidadUno() {
        // Arrange
        Producto producto = new Producto("P2", "Mouse", 25.0);
        DetalleFactura detalle = new DetalleFactura(producto, 1);

        // Act
        double subtotal = detalle.calcularSubtotal();

        // Assert
        assertEquals(25.0, subtotal, 0.001);
    }

    // ---------------------------------------------------------------
    // EXCEPTIONAL PATH (camino excepcional / negativo): datos
    // inválidos que deben ser rechazados por el sistema.
    // ---------------------------------------------------------------
    @Test
    void debeLanzarExcepcionSiLaCantidadEsCero() {
        // Arrange
        Producto producto = new Producto("P3", "Monitor", 300.0);

        // Act + Assert: en pruebas de excepción, la acción ocurre
        // dentro de la aserción, porque lo que probamos es que
        // "actuar" lance el error esperado.
        assertThrows(IllegalArgumentException.class, () -> new DetalleFactura(producto, 0));
    }

    @Test
    void debeLanzarExcepcionSiLaCantidadEsNegativa() {
        // Arrange
        Producto producto = new Producto("P4", "Silla", 120.0);

        // Act + Assert
        assertThrows(IllegalArgumentException.class, () -> new DetalleFactura(producto, -5));
    }

    @Test
    void debeLanzarExcepcionSiElPrecioDelProductoEsNegativo() {
        // Arrange + Act + Assert: aquí el error ocurre al construir
        // el propio Producto, antes de llegar a DetalleFactura.
        assertThrows(IllegalArgumentException.class, () -> new Producto("P5", "Producto inválido", -10.0));
    }
}
