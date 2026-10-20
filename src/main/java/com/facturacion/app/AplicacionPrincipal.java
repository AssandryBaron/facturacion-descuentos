package com.facturacion.app;

import com.facturacion.descuento.DescuentoClienteVip;
import com.facturacion.descuento.DescuentoPorcentaje;
import com.facturacion.descuento.EstrategiaDescuento;
import com.facturacion.impresion.ImpresoraFacturaConsola;
import com.facturacion.modelo.Cliente;
import com.facturacion.modelo.DetalleFactura;
import com.facturacion.modelo.Factura;
import com.facturacion.modelo.Producto;
import com.facturacion.repositorio.RepositorioFacturaEnMemoria;
import com.facturacion.servicio.CalculadoraFactura;
import com.facturacion.servicio.ServicioFacturacion;

import java.util.List;

/**
 * Punto de entrada de la aplicación. Arma manualmente las
 * dependencias (composition root) e inyecta las abstracciones en
 * ServicioFacturacion, demostrando el principio de Inversión de
 * Dependencias (DIP) en acción.
 */
public class AplicacionPrincipal {

    public static void main(String[] args) {

        // 1. Se construyen las dependencias concretas (composition root)
        ServicioFacturacion servicioFacturacion = new ServicioFacturacion(
                new CalculadoraFactura(),
                new ImpresoraFacturaConsola(),
                new RepositorioFacturaEnMemoria()
        );

        // 2. Se crea el cliente y los productos
        Cliente cliente = new Cliente("CLI-001", "Ana Gómez", true); // Cliente VIP

        Producto laptop = new Producto("PROD-001", "Laptop 15 pulgadas", 2500.00);
        Producto mouse = new Producto("PROD-002", "Mouse inalámbrico", 45.00);
        Producto teclado = new Producto("PROD-003", "Teclado mecánico", 120.00);

        // 3. Se arma la factura con sus detalles
        Factura factura = new Factura("FAC-0001", cliente);
        factura.agregarDetalle(new DetalleFactura(laptop, 1));
        factura.agregarDetalle(new DetalleFactura(mouse, 2));
        factura.agregarDetalle(new DetalleFactura(teclado, 1));

        // 4. Se seleccionan al menos dos estrategias de descuento distintas.
        //    Gracias al patrón Strategy (OCP), agregar una tercera o cuarta
        //    estrategia no requeriría modificar ninguna clase existente.
        List<EstrategiaDescuento> estrategiasDescuento = List.of(
                new DescuentoPorcentaje(0.10, 500.0),   // 10% si el subtotal >= 500
                new DescuentoClienteVip(0.05)           // 5% adicional para clientes VIP
        );

        // 5. Se procesa la factura: calcula, guarda e imprime
        servicioFacturacion.procesarFactura(factura, estrategiasDescuento);
    }
}
