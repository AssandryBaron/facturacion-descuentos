package com.facturacion.descuento;

import com.facturacion.modelo.Factura;

/**
 * Descuento calculado como un porcentaje del subtotal, aplicable
 * únicamente cuando el subtotal alcanza un monto mínimo.
 */
public class DescuentoPorcentaje implements EstrategiaDescuento {

    private final double porcentaje; // Ej: 0.10 == 10%
    private final double montoMinimoParaAplicar;

    public DescuentoPorcentaje(double porcentaje, double montoMinimoParaAplicar) {
        if (porcentaje < 0 || porcentaje > 1) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 1");
        }
        this.porcentaje = porcentaje;
        this.montoMinimoParaAplicar = montoMinimoParaAplicar;
    }

    @Override
    public boolean esAplicable(Factura factura, double subtotal) {
        return subtotal >= montoMinimoParaAplicar;
    }

    @Override
    public double calcularDescuento(Factura factura, double subtotal) {
        return subtotal * porcentaje;
    }

    @Override
    public String obtenerDescripcion() {
        return String.format("Descuento por porcentaje (%.0f%%)", porcentaje * 100);
    }
}
