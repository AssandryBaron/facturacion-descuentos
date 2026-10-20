package com.facturacion.servicio;

import com.facturacion.descuento.DescuentoClienteVip;
import com.facturacion.descuento.DescuentoMontoFijo;
import com.facturacion.descuento.DescuentoPorcentaje;
import com.facturacion.descuento.EstrategiaDescuento;
import com.facturacion.impresion.ImpresoraFacturaConsola;
import com.facturacion.modelo.Cliente;
import com.facturacion.modelo.DetalleFactura;
import com.facturacion.modelo.Factura;
import com.facturacion.modelo.Producto;
import com.facturacion.repositorio.RepositorioFacturaEnMemoria;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioFacturacionTest {

    @Test
    void debeCalcularTotalAplicandoDosEstrategiasDeDescuento() {
        // Arrange
        ServicioFacturacion servicioFacturacion = new ServicioFacturacion(
                new CalculadoraFactura(),
                new ImpresoraFacturaConsola(),
                new RepositorioFacturaEnMemoria()
        );

        Cliente clienteVip = new Cliente("CLI-002", "Carlos Ruiz", true);
        Producto monitor = new Producto("PROD-010", "Monitor 27 pulgadas", 800.0);

        Factura factura = new Factura("FAC-TEST-001", clienteVip);
        factura.agregarDetalle(new DetalleFactura(monitor, 1)); // subtotal = 800.0

        List<EstrategiaDescuento> estrategias = List.of(
                new DescuentoPorcentaje(0.10, 100.0), // 10% -> 80.0
                new DescuentoClienteVip(0.05)         // 5%  -> 40.0
        );

        // Act
        servicioFacturacion.procesarFactura(factura, estrategias);

        // Assert
        assertEquals(800.0, factura.obtenerSubtotal(), 0.001);
        assertEquals(120.0, factura.obtenerTotalDescuento(), 0.001); // 80 + 40
        assertEquals(680.0, factura.obtenerTotal(), 0.001);
        assertEquals(2, factura.obtenerDescuentosAplicados().size());
    }

    @Test
    void noDebeAplicarDescuentoVipACLienteRegular() {
        Cliente clienteRegular = new Cliente("CLI-003", "María López", false);
        Producto silla = new Producto("PROD-020", "Silla ergonómica", 300.0);

        Factura factura = new Factura("FAC-TEST-002", clienteRegular);
        factura.agregarDetalle(new DetalleFactura(silla, 1));

        CalculadoraFactura calculadora = new CalculadoraFactura();
        List<EstrategiaDescuento> estrategias = List.of(
                new DescuentoMontoFijo(50.0, 200.0), // aplica: subtotal 300 >= 200
                new DescuentoClienteVip(0.05)        // no aplica: cliente no es VIP
        );

        calculadora.calcular(factura, estrategias);

        assertEquals(300.0, factura.obtenerSubtotal(), 0.001);
        assertEquals(50.0, factura.obtenerTotalDescuento(), 0.001);
        assertEquals(250.0, factura.obtenerTotal(), 0.001);
        assertEquals(1, factura.obtenerDescuentosAplicados().size());
    }

    @Test
    void debePersistirLaFacturaEnElRepositorio() {
        RepositorioFacturaEnMemoria repositorio = new RepositorioFacturaEnMemoria();
        ServicioFacturacion servicioFacturacion = new ServicioFacturacion(
                new CalculadoraFactura(),
                new ImpresoraFacturaConsola(),
                repositorio
        );

        Cliente cliente = new Cliente("CLI-004", "Pedro Sánchez", false);
        Factura factura = new Factura("FAC-TEST-003", cliente);
        factura.agregarDetalle(new DetalleFactura(new Producto("PROD-030", "Libro", 25.0), 3));

        servicioFacturacion.procesarFactura(factura, List.of());

        Optional<Factura> facturaGuardada = repositorio.buscarPorId("FAC-TEST-003");
        assertTrue(facturaGuardada.isPresent());
        assertEquals(75.0, facturaGuardada.get().obtenerTotal(), 0.001);
    }
}
