package com.facturacion.impresion;

import com.facturacion.modelo.DetalleFactura;
import com.facturacion.modelo.Factura;

/**
 * Implementación concreta de ImpresoraFactura que muestra el
 * detalle completo de la factura por consola (System.out).
 *
 * Al depender ServicioFacturacion de la interfaz ImpresoraFactura
 * (DIP) y no de esta clase concreta, se podría reemplazar por una
 * ImpresoraFacturaPdf, ImpresoraFacturaEmail, etc. sin tocar el
 * resto del sistema.
 */
public class ImpresoraFacturaConsola implements ImpresoraFactura {

    @Override
    public void imprimir(Factura factura) {
        System.out.println("========================================");
        System.out.println("FACTURA N°: " + factura.obtenerId());
        System.out.println("Cliente: " + factura.obtenerCliente());
        System.out.println("----------------------------------------");
        System.out.println("Detalle:");
        for (DetalleFactura detalle : factura.obtenerDetalles()) {
            System.out.println("  - " + detalle);
        }
        System.out.println("----------------------------------------");
        System.out.printf("Subtotal:        $%.2f%n", factura.obtenerSubtotal());

        if (factura.obtenerDescuentosAplicados().isEmpty()) {
            System.out.println("Descuentos:      Ninguno");
        } else {
            System.out.println("Descuentos aplicados:");
            for (String descuento : factura.obtenerDescuentosAplicados()) {
                System.out.println("  - " + descuento);
            }
        }

        System.out.printf("Total descuento: -$%.2f%n", factura.obtenerTotalDescuento());
        System.out.printf("TOTAL A PAGAR:   $%.2f%n", factura.obtenerTotal());
        System.out.println("========================================");
    }
}
