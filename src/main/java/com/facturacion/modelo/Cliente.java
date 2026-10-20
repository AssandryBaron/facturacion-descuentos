package com.facturacion.modelo;

import java.util.Objects;

/**
 * Representa al cliente asociado a una factura.
 * Guarda el estado "clienteVip" que puede ser usado por estrategias
 * de descuento específicas (ver DescuentoClienteVip), sin que el
 * propio Cliente conozca nada sobre cómo se calculan los descuentos.
 */
public class Cliente {

    private final String id;
    private final String nombre;
    private final boolean clienteVip;

    public Cliente(String id, String nombre, boolean clienteVip) {
        this.id = Objects.requireNonNull(id, "El id del cliente es obligatorio");
        this.nombre = Objects.requireNonNull(nombre, "El nombre del cliente es obligatorio");
        this.clienteVip = clienteVip;
    }

    public String obtenerId() {
        return id;
    }

    public String obtenerNombre() {
        return nombre;
    }

    public boolean esClienteVip() {
        return clienteVip;
    }

    @Override
    public String toString() {
        return nombre + (clienteVip ? " (Cliente VIP)" : "");
    }
}
