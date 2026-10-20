package com.facturacion.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representa una factura: asocia un cliente con una lista de
 * detalles (líneas de producto).
 *
 * IMPORTANTE (SRP): esta clase es únicamente un modelo de datos.
 * NO calcula subtotales, descuentos ni totales por sí misma; ese
 * trabajo lo realiza la clase de servicio CalculadoraFactura.
 * Factura solo expone los campos calculados y permite registrarlos.
 */
public class Factura {

    private final String id;
    private final Cliente cliente;
    private final List<DetalleFactura> detalles = new ArrayList<>();
    private final List<String> descuentosAplicados = new ArrayList<>();

    private double subtotal;
    private double totalDescuento;
    private double total;

    public Factura(String id, Cliente cliente) {
        this.id = Objects.requireNonNull(id, "El id de la factura es obligatorio");
        this.cliente = Objects.requireNonNull(cliente, "La factura debe tener un cliente");
    }

    public void agregarDetalle(DetalleFactura detalle) {
        detalles.add(Objects.requireNonNull(detalle, "El detalle no puede ser nulo"));
    }

    public void registrarDescuentoAplicado(String descripcionDescuento) {
        descuentosAplicados.add(descripcionDescuento);
    }

    public String obtenerId() {
        return id;
    }

    public Cliente obtenerCliente() {
        return cliente;
    }

    public List<DetalleFactura> obtenerDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public List<String> obtenerDescuentosAplicados() {
        return Collections.unmodifiableList(descuentosAplicados);
    }

    public double obtenerSubtotal() {
        return subtotal;
    }

    public void establecerSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double obtenerTotalDescuento() {
        return totalDescuento;
    }

    public void establecerTotalDescuento(double totalDescuento) {
        this.totalDescuento = totalDescuento;
    }

    public double obtenerTotal() {
        return total;
    }

    public void establecerTotal(double total) {
        this.total = total;
    }
}
