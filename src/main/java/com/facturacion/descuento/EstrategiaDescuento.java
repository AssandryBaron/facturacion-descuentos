package com.facturacion.descuento;

import com.facturacion.modelo.Factura;

/**
 * Abstracción (patrón Strategy) para cualquier tipo de descuento.
 *
 * OCP: nuevas reglas de descuento se agregan creando nuevas clases
 * que implementen esta interfaz, sin modificar el código existente
 * (CalculadoraFactura, ServicioFacturacion, etc.).
 *
 * LSP: cualquier implementación debe poder sustituir a otra sin
 * romper el cálculo. Por eso el contrato es simple y no depende de
 * detalles internos de una implementación concreta: recibe la
 * factura y el subtotal ya calculado, y siempre debe devolver un
 * valor de descuento mayor o igual a cero.
 */
public interface EstrategiaDescuento {

    /**
     * Indica si esta estrategia debe aplicarse a la factura dada.
     */
    boolean esAplicable(Factura factura, double subtotal);

    /**
     * Calcula el monto de descuento (en la misma moneda que el
     * subtotal). Debe devolver siempre un valor >= 0.
     */
    double calcularDescuento(Factura factura, double subtotal);

    /**
     * Descripción legible de la estrategia, usada para el detalle
     * impreso de la factura (ej. "Descuento por porcentaje (10%)").
     */
    String obtenerDescripcion();
}
