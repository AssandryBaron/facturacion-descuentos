package com.facturacion.descuento;

import com.facturacion.modelo.Factura;

/**
 * Descuento adicional exclusivo para clientes marcados como VIP.
 * Demuestra que una estrategia puede depender de datos del cliente
 * (a través de la Factura) y no solo del subtotal.
 */
public class DescuentoClienteVip implements EstrategiaDescuento {

    private final double porcentajeVip;

    public DescuentoClienteVip(double porcentajeVip) {
        if (porcentajeVip < 0 || porcentajeVip > 1) {
            throw new IllegalArgumentException("El porcentaje VIP debe estar entre 0 y 1");
        }
        this.porcentajeVip = porcentajeVip;
    }

    @Override
    public boolean esAplicable(Factura factura, double subtotal) {
        return factura.obtenerCliente().esClienteVip();
    }

    @Override
    public double calcularDescuento(Factura factura, double subtotal) {
        return subtotal * porcentajeVip;
    }

    @Override
    public String obtenerDescripcion() {
        return String.format("Descuento cliente VIP (%.0f%%)", porcentajeVip * 100);
    }
}
