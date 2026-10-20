package com.facturacion.modelo;

import java.util.Objects;

/**
 * Representa una línea de detalle dentro de una factura:
 * un producto y la cantidad comprada de dicho producto.
 * Responsabilidad única: calcular su propio subtotal.
 */
public class DetalleFactura {

    private final Producto producto;
    private final int cantidad;

    public DetalleFactura(Producto producto, int cantidad) {
        this.producto = Objects.requireNonNull(producto, "El producto es obligatorio");
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        this.cantidad = cantidad;
    }

    public Producto obtenerProducto() {
        return producto;
    }

    public int obtenerCantidad() {
        return cantidad;
    }

    /**
     * Calcula el subtotal de esta línea (precio unitario * cantidad).
     */
    public double calcularSubtotal() {
        return producto.obtenerPrecio() * cantidad;
    }

    @Override
    public String toString() {
        return String.format("%dx %s = $%.2f", cantidad, producto.obtenerNombre(), calcularSubtotal());
    }
}
