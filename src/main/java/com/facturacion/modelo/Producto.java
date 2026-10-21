package com.facturacion.modelo;

import java.util.Objects;

/**
 * Representa un producto o servicio que puede ser facturado.
 * Es un objeto de dominio inmutable (Single Responsibility: solo
 * conoce sus propios datos, no calcula ni aplica descuentos).
 */
public class Producto {

    private final String id;
    private final String nombre;
    private final double precio;

    public Producto(String id, String nombre, double precio) {
        if (precio < 0) {
            throw new IllegalArgumentException("El precio del producto no puede ser negativo");
        }
        this.id = Objects.requireNonNull(id, "El id del producto es obligatorio");
        this.nombre = Objects.requireNonNull(nombre, "El nombre del producto es obligatorio");
        this.precio = precio;
    }

    //este es el comentario

    public String obtenerId() {
        return id;
    }

    public String obtenerNombre() {
        return nombre;
    }

    public double obtenerPrecio() {
        return precio;
    }

    @Override
    public String toString() {
        return nombre + " ($" + String.format("%.2f", precio) + ")";
    }
}
