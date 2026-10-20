package com.facturacion.servicio;

import com.facturacion.descuento.EstrategiaDescuento;
import com.facturacion.impresion.ImpresoraFactura;
import com.facturacion.modelo.Factura;
import com.facturacion.repositorio.RepositorioFactura;

import java.util.List;
import java.util.Objects;

/**
 * Servicio de más alto nivel que orquesta el proceso de
 * facturación: calcular, guardar e imprimir.
 *
 * DIP: depende únicamente de abstracciones (CalculadoraFactura ya
 * depende de EstrategiaDescuento; aquí además inyectamos
 * ImpresoraFactura y RepositorioFactura como interfaces). Ninguna
 * dependencia concreta se instancia dentro de esta clase: todas se
 * reciben por el constructor (inyección de dependencias).
 */
public class ServicioFacturacion {

    private final CalculadoraFactura calculadoraFactura;
    private final ImpresoraFactura impresoraFactura;
    private final RepositorioFactura repositorioFactura;

    public ServicioFacturacion(CalculadoraFactura calculadoraFactura,
                                ImpresoraFactura impresoraFactura,
                                RepositorioFactura repositorioFactura) {
        this.calculadoraFactura = Objects.requireNonNull(calculadoraFactura);
        this.impresoraFactura = Objects.requireNonNull(impresoraFactura);
        this.repositorioFactura = Objects.requireNonNull(repositorioFactura);
    }

    /**
     * Procesa una factura: calcula totales aplicando las estrategias
     * de descuento indicadas, la persiste y la imprime.
     */
    public void procesarFactura(Factura factura, List<EstrategiaDescuento> estrategiasDescuento) {
        calculadoraFactura.calcular(factura, estrategiasDescuento);
        repositorioFactura.guardar(factura);
        impresoraFactura.imprimir(factura);
    }
}
