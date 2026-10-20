Feature: Facturación con descuentos
  Como encargado de ventas
  quiero que el sistema calcule el total de una factura aplicando los descuentos que correspondan
  para cobrarle al cliente el valor correcto.

  Background:
    * def Cliente = Java.type('com.facturacion.modelo.Cliente')
    * def Producto = Java.type('com.facturacion.modelo.Producto')
    * def DetalleFactura = Java.type('com.facturacion.modelo.DetalleFactura')
    * def Factura = Java.type('com.facturacion.modelo.Factura')
    * def DescuentoPorcentaje = Java.type('com.facturacion.descuento.DescuentoPorcentaje')
    * def DescuentoMontoFijo = Java.type('com.facturacion.descuento.DescuentoMontoFijo')
    * def DescuentoClienteVip = Java.type('com.facturacion.descuento.DescuentoClienteVip')
    * def CalculadoraFactura = Java.type('com.facturacion.servicio.CalculadoraFactura')
    * def cerca = function(a, b) { return Math.abs(a - b) < 0.001 }

  Scenario: Un cliente VIP recibe el descuento por porcentaje y el descuento VIP acumulados
    Given def cliente = new Cliente('CLI-1', 'Ana Gomez', true)
    And def factura = new Factura('FAC-1', cliente)
    And eval factura.agregarDetalle(new DetalleFactura(new Producto('P1', 'Monitor 27 pulgadas', 800.0), 1))
    And def estrategias = []
    And karate.appendTo(estrategias, new DescuentoPorcentaje(0.10, 100.0))
    And karate.appendTo(estrategias, new DescuentoClienteVip(0.05))
    When eval new CalculadoraFactura().calcular(factura, estrategias)
    Then def subtotal = factura.obtenerSubtotal()
    And def totalDescuento = factura.obtenerTotalDescuento()
    And def total = factura.obtenerTotal()
    And def cantidadDescuentos = factura.obtenerDescuentosAplicados().size()
    And assert cerca(subtotal, 800.0)
    And assert cerca(totalDescuento, 120.0)
    And assert cerca(total, 680.0)
    And match cantidadDescuentos == 2

  Scenario: Un cliente regular no recibe el descuento VIP
    Given def cliente = new Cliente('CLI-2', 'Maria Lopez', false)
    And def factura = new Factura('FAC-2', cliente)
    And eval factura.agregarDetalle(new DetalleFactura(new Producto('P2', 'Silla ergonomica', 300.0), 1))
    And def estrategias = []
    And karate.appendTo(estrategias, new DescuentoMontoFijo(50.0, 200.0))
    And karate.appendTo(estrategias, new DescuentoClienteVip(0.05))
    When eval new CalculadoraFactura().calcular(factura, estrategias)
    Then def total = factura.obtenerTotal()
    And def cantidadDescuentos = factura.obtenerDescuentosAplicados().size()
    And assert cerca(total, 250.0)
    And match cantidadDescuentos == 1

  Scenario: Una factura sin descuentos se cobra por el valor del subtotal
    Given def cliente = new Cliente('CLI-3', 'Pedro Sanchez', false)
    And def factura = new Factura('FAC-3', cliente)
    And eval factura.agregarDetalle(new DetalleFactura(new Producto('P3', 'Libro', 25.0), 3))
    And def estrategias = []
    When eval new CalculadoraFactura().calcular(factura, estrategias)
    Then def total = factura.obtenerTotal()
    And assert cerca(total, 75.0)

  Scenario: Un descuento de monto fijo nunca deja el total en negativo
    Given def cliente = new Cliente('CLI-4', 'Laura Torres', false)
    And def factura = new Factura('FAC-4', cliente)
    And eval factura.agregarDetalle(new DetalleFactura(new Producto('P4', 'Cuaderno', 30.0), 1))
    And def estrategias = []
    And karate.appendTo(estrategias, new DescuentoMontoFijo(50.0, 0.0))
    When eval new CalculadoraFactura().calcular(factura, estrategias)
    Then def total = factura.obtenerTotal()
    And assert cerca(total, 0.0)

  Scenario Outline: El descuento por porcentaje solo aplica si se alcanza el monto mínimo - <caso>
    Given def cliente = new Cliente('CLI-5', 'Diego Vega', false)
    And def factura = new Factura('FAC-5', cliente)
    And eval factura.agregarDetalle(new DetalleFactura(new Producto('P5', 'Articulo', <precio>), <cantidad>))
    And def estrategias = []
    And karate.appendTo(estrategias, new DescuentoPorcentaje(0.10, 200.0))
    When eval new CalculadoraFactura().calcular(factura, estrategias)
    Then def total = factura.obtenerTotal()
    And assert cerca(total, <totalEsperado>)

    Examples:
      | caso                  | precio | cantidad | totalEsperado |
      | por debajo del minimo | 100.0  | 1        | 100.0         |
      | exactamente el minimo | 200.0  | 1        | 180.0         |
      | por encima del minimo | 300.0  | 2        | 540.0         |