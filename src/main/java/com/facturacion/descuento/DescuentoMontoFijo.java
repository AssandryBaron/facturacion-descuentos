package com.facturacion.descuento;

import com.facturacion.modelo.Factura;

/**
 * Descuento de un monto fijo en dinero, aplicable cuando el
 * subtotal supera un mínimo establecido. Nunca descuenta más de lo
 * que vale el subtotal (evita totales negativos).
 */
public class DescuentoMontoFijo implements EstrategiaDescuento {

    private final double montoFijo;
    private final double montoMinimoParaAplicar;

    public DescuentoMontoFijo(double montoFijo, double montoMinimoParaAplicar) {
        if (montoFijo < 0) {
            throw new IllegalArgumentException("El monto fijo de descuento no puede ser negativo");
        }
        this.montoFijo = montoFijo;
        this.montoMinimoParaAplicar = montoMinimoParaAplicar;
    }

    @Override
    public boolean esAplicable(Factura factura, double subtotal) {
        return subtotal >= montoMinimoParaAplicar;
    }

    @Override
    public double calcularDescuento(Factura factura, double subtotal) {
        return Math.min(montoFijo, subtotal);
    }

    @Override
    public String obtenerDescripcion() {
        return String.format("Descuento por monto fijo ($%.2f)", montoFijo);
    }
}
