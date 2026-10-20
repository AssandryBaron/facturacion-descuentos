package com.facturacion.repositorio;

import com.facturacion.modelo.Factura;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación simple de RepositorioFactura que guarda las
 * facturas en un mapa en memoria. Útil para pruebas y demos; podría
 * sustituirse por una implementación con base de datos sin afectar
 * a ServicioFacturacion (DIP).
 */
public class RepositorioFacturaEnMemoria implements RepositorioFactura {

    private final Map<String, Factura> facturasGuardadas = new LinkedHashMap<>();

    @Override
    public void guardar(Factura factura) {
        facturasGuardadas.put(factura.obtenerId(), factura);
    }

    @Override
    public Optional<Factura> buscarPorId(String id) {
        return Optional.ofNullable(facturasGuardadas.get(id));
    }

    @Override
    public List<Factura> listarTodas() {
        return new ArrayList<>(facturasGuardadas.values());
    }
}
