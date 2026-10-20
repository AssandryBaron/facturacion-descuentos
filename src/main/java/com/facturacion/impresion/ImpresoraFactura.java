package com.facturacion.impresion;

import com.facturacion.modelo.Factura;

/**
 * Interfaz pequeña y específica (ISP): solo se encarga de "mostrar"
 * una factura ya calculada. No sabe nada de cómo se calculan
 * subtotales ni descuentos, ni de cómo se persiste la factura.
 */
public interface ImpresoraFactura {

    void imprimir(Factura factura);
}
