package com.facturacion.repositorio;

import com.facturacion.modelo.Factura;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz pequeña y específica (ISP) para la persistencia de
 * facturas. No expone operaciones de cálculo ni de impresión.
 */
public interface RepositorioFactura {

    void guardar(Factura factura);

    Optional<Factura> buscarPorId(String id);

    List<Factura> listarTodas();
}
