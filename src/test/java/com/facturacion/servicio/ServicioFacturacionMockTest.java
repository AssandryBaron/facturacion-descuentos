package com.facturacion.servicio;

import com.facturacion.descuento.EstrategiaDescuento;
import com.facturacion.impresion.ImpresoraFactura;
import com.facturacion.modelo.Cliente;
import com.facturacion.modelo.DetalleFactura;
import com.facturacion.modelo.Factura;
import com.facturacion.modelo.Producto;
import com.facturacion.repositorio.RepositorioFactura;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * PRUEBAS UNITARIAS AVANZADAS: uso de Mockito (dobles de prueba / mocks).
 *
 * ServicioFacturacion depende de tres interfaces (CalculadoraFactura,
 * ImpresoraFactura, RepositorioFactura). Gracias al principio DIP,
 * podemos "simular" ImpresoraFactura y RepositorioFactura con mocks:
 * objetos falsos que no ejecutan lógica real, pero que registran
 * qué métodos se llamaron y con qué argumentos.
 *
 * Esto nos permite probar el COMPORTAMIENTO de ServicioFacturacion
 * (¿llamó a guardar()? ¿llamó a imprimir()? ¿cuántas veces?) de
 * forma aislada, sin depender de una implementación real de
 * impresión o persistencia.
 *
 * @ExtendWith(MockitoExtension.class) le dice a JUnit que inicialice
 * automáticamente los campos anotados con @Mock antes de cada prueba.
 */
@ExtendWith(MockitoExtension.class)
class ServicioFacturacionMockTest {

    @Mock
    private ImpresoraFactura impresoraMock;

    @Mock
    private RepositorioFactura repositorioMock;

    @Test
    void debeGuardarEImprimirLaFacturaAlProcesarla() {
        // Arrange: usamos la CalculadoraFactura real (no es necesario
        // simularla, es lógica simple y determinista) pero mockeamos
        // las dependencias de efectos secundarios (guardar, imprimir).
        ServicioFacturacion servicio = new ServicioFacturacion(
                new CalculadoraFactura(), impresoraMock, repositorioMock);

        Cliente cliente = new Cliente("CLI-100", "Laura Torres", false);
        Factura factura = new Factura("FAC-100", cliente);
        factura.agregarDetalle(new DetalleFactura(new Producto("P1", "Cuaderno", 10.0), 2));

        // Act
        servicio.procesarFactura(factura, List.of());

        // Assert: verificamos que el servicio SÍ haya llamado a cada
        // colaborador exactamente una vez, con esa misma factura.
        verify(repositorioMock, times(1)).guardar(factura);
        verify(impresoraMock, times(1)).imprimir(factura);
    }

    @Test
    void noDebeImprimirNiGuardarSiLaFacturaNoSeProcesa() {
        // Arrange: se crea el servicio pero NUNCA se llama a
        // procesarFactura(); demuestra el caso "sin interacción".
        ServicioFacturacion servicio = new ServicioFacturacion(
                new CalculadoraFactura(), impresoraMock, repositorioMock);

        // Act: (a propósito, no se ejecuta ninguna acción sobre el servicio)

        // Assert: los mocks no debieron recibir ninguna llamada
        verifyNoInteractions(impresoraMock, repositorioMock);
    }

    @Test
    void debeUsarEstrategiaDescuentoSimuladaParaControlarElResultado() {
        // Arrange: además de mockear infraestructura, podemos mockear
        // una EstrategiaDescuento para forzar un descuento fijo sin
        // depender de ninguna implementación real.
        EstrategiaDescuento estrategiaFalsa = org.mockito.Mockito.mock(EstrategiaDescuento.class);
        org.mockito.Mockito.when(estrategiaFalsa.esAplicable(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyDouble()))
                .thenReturn(true);
        org.mockito.Mockito.when(estrategiaFalsa.calcularDescuento(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyDouble()))
                .thenReturn(15.0);
        org.mockito.Mockito.when(estrategiaFalsa.obtenerDescripcion()).thenReturn("Descuento simulado");

        ServicioFacturacion servicio = new ServicioFacturacion(
                new CalculadoraFactura(), impresoraMock, repositorioMock);

        Cliente cliente = new Cliente("CLI-101", "Diego Vega", false);
        Factura factura = new Factura("FAC-101", cliente);
        factura.agregarDetalle(new DetalleFactura(new Producto("P2", "Mochila", 100.0), 1));

        // Act
        servicio.procesarFactura(factura, List.of(estrategiaFalsa));

        // Assert: el total debe reflejar el descuento fijo que forzamos
        // en el mock (100.0 - 15.0 = 85.0), sin importar la lógica
        // interna real de ninguna estrategia concreta.
        org.junit.jupiter.api.Assertions.assertEquals(85.0, factura.obtenerTotal(), 0.001);
    }
}
