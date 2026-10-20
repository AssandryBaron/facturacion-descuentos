package com.facturacion.servicio;

import com.facturacion.descuento.EstrategiaDescuento;
import com.facturacion.modelo.DetalleFactura;
import com.facturacion.modelo.Factura;

import java.util.List;

/**
 * Responsable único (SRP): calcular subtotal, descuentos y total de
 * una factura. No imprime ni persiste nada.
 *
 * DIP: recibe la lista de estrategias de descuento como abstracción
 * (EstrategiaDescuento), sin conocer ninguna implementación
 * concreta. Esto permite agregar nuevos tipos de descuento (OCP)
 * sin modificar esta clase.
 */
public class CalculadoraFactura {

    /**
     * Calcula subtotal, descuentos aplicables y total de la
     * factura, dejando el resultado registrado en la propia
     * factura (subtotal, descuentos aplicados y total).
     */
    public void calcular(Factura factura, List<EstrategiaDescuento> estrategiasDescuento) {
        double subtotal = calcularSubtotal(factura);
        double totalDescuento = 0.0;

        for (EstrategiaDescuento estrategia : estrategiasDescuento) {
            if (estrategia.esAplicable(factura, subtotal)) {
                double descuento = estrategia.calcularDescuento(factura, subtotal);
                totalDescuento += descuento;
                factura.registrarDescuentoAplicado(
                        estrategia.obtenerDescripcion() + ": -$" + String.format("%.2f", descuento));
            }
        }

        double total = Math.max(0.0, subtotal - totalDescuento);

        factura.establecerSubtotal(subtotal);
        factura.establecerTotalDescuento(totalDescuento);
        factura.establecerTotal(total);
    }

    private double calcularSubtotal(Factura factura) {
        double subtotal = 0.0;
        for (DetalleFactura detalle : factura.obtenerDetalles()) {
            subtotal += detalle.calcularSubtotal();
        }
        return subtotal;
    }
}
