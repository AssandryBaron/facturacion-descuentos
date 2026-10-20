# Sistema de Facturación con Descuentos

Sistema de facturación en Java que calcula el total de una factura aplicando distintas estrategias de descuento de forma extensible, siguiendo los principios SOLID. Todo el código (clases, métodos, variables y comentarios) está escrito en español.

## Tabla de contenido

- [Requisitos](#requisitos)
- [Cómo ejecutar el programa](#cómo-ejecutar-el-programa)
- [Estructura del proyecto](#estructura-del-proyecto)
- [Arquitectura general](#arquitectura-general)
- [Flujo de ejecución](#flujo-de-ejecución)
- [Descripción de cada clase](#descripción-de-cada-clase)
- [Principios SOLID aplicados](#principios-solid-aplicados)
- [Cómo agregar un nuevo tipo de descuento](#cómo-agregar-un-nuevo-tipo-de-descuento)

## Requisitos

- JDK 17 o superior
- Gradle (o usar el wrapper `./gradlew` incluido en el proyecto)

## Cómo ejecutar el programa

```bash
./gradlew run
```

Esto compila el proyecto y ejecuta `AplicacionPrincipal.main()`, que crea una factura de ejemplo, le aplica descuentos y muestra el resultado por consola.

## Estructura del proyecto

```
facturacion-descuentos/
├── build.gradle
├── settings.gradle
└── src/main/java/com/facturacion/
    ├── modelo/
    │   ├── Producto.java
    │   ├── Cliente.java
    │   ├── DetalleFactura.java
    │   └── Factura.java
    ├── descuento/
    │   ├── EstrategiaDescuento.java
    │   ├── DescuentoPorcentaje.java
    │   ├── DescuentoMontoFijo.java
    │   └── DescuentoClienteVip.java
    ├── servicio/
    │   ├── CalculadoraFactura.java
    │   └── ServicioFacturacion.java
    ├── impresion/
    │   ├── ImpresoraFactura.java
    │   └── ImpresoraFacturaConsola.java
    ├── repositorio/
    │   ├── RepositorioFactura.java
    │   └── RepositorioFacturaEnMemoria.java
    └── app/
        └── AplicacionPrincipal.java
```

Cada paquete agrupa clases con una responsabilidad concreta:

| Paquete | Responsabilidad |
|---|---|
| `modelo` | Representar los datos del dominio (producto, cliente, detalle, factura). No calcula nada. |
| `descuento` | Definir y calcular las distintas reglas de descuento (patrón Strategy). |
| `servicio` | Calcular totales y orquestar el proceso completo de facturación. |
| `impresion` | Mostrar el resultado de una factura ya calculada. |
| `repositorio` | Guardar y recuperar facturas. |
| `app` | Punto de entrada del programa (`main`). |

## Arquitectura general

El sistema aplica el patrón de diseño **Strategy** para los descuentos: en vez de tener un método gigante con `if/else` para cada tipo de descuento, existe una interfaz `EstrategiaDescuento` que cualquier regla de descuento debe implementar. `CalculadoraFactura` solo conoce esa interfaz, nunca las clases concretas — por eso se pueden agregar nuevos descuentos sin modificar el código existente.

Además, las dependencias de infraestructura (cómo se imprime y cómo se persiste una factura) también están detrás de interfaces (`ImpresoraFactura`, `RepositorioFactura`), inyectadas por constructor en `ServicioFacturacion`. Esto permite reemplazar, por ejemplo, la impresión por consola por una impresión en PDF sin tocar el resto del sistema.

## Flujo de ejecución

Así es como fluye una ejecución típica, desde que arranca el programa hasta que se ve el resultado:

1. **`AplicacionPrincipal.main()`** crea las implementaciones concretas (`CalculadoraFactura`, `ImpresoraFacturaConsola`, `RepositorioFacturaEnMemoria`) y las inyecta en un `ServicioFacturacion`.
2. Crea un `Cliente`, varios `Producto` y arma una `Factura` con sus `DetalleFactura`.
3. Define la lista de `EstrategiaDescuento` a aplicar (por ejemplo, un 10% de descuento general y un 5% adicional para clientes VIP).
4. Llama a `servicioFacturacion.procesarFactura(factura, estrategias)`, que dispara internamente:
   - **`CalculadoraFactura.calcular(...)`**: suma los subtotales de cada detalle, recorre las estrategias de descuento (verificando `esAplicable` y sumando `calcularDescuento` de las que apliquen) y calcula el total final.
   - **`RepositorioFactura.guardar(...)`**: guarda la factura ya calculada.
   - **`ImpresoraFactura.imprimir(...)`**: muestra el detalle completo por consola (cliente, líneas, subtotal, descuentos aplicados y total).

## Descripción de cada clase

### Paquete `modelo`

**`Producto`** — Representa un producto o servicio facturable: `id`, `nombre`, `precio`. Es inmutable y valida en su constructor que el precio no sea negativo.

**`Cliente`** — Representa al cliente de la factura: `id`, `nombre` y si es `clienteVip`. El método `esClienteVip()` es usado por la estrategia `DescuentoClienteVip` para decidir si aplica un descuento adicional.

**`DetalleFactura`** — Una línea de la factura: un `Producto` más una `cantidad`. Valida que la cantidad sea mayor a cero. Su único cálculo es `calcularSubtotal()` (precio × cantidad).

**`Factura`** — Asocia un `Cliente` con una lista de `DetalleFactura`. No calcula nada por sí misma: solo permite agregar detalles, registrar los descuentos aplicados, y guardar/leer los resultados (subtotal, total de descuento y total) que calcula `CalculadoraFactura`.

### Paquete `descuento`

**`EstrategiaDescuento`** (interfaz) — El contrato que debe cumplir cualquier tipo de descuento: `esAplicable(factura, subtotal)` decide si se activa, `calcularDescuento(factura, subtotal)` calcula el monto, `obtenerDescripcion()` da un texto legible.

**`DescuentoPorcentaje`** — Descuento de un porcentaje del subtotal, aplicable si el subtotal alcanza un monto mínimo configurado.

**`DescuentoMontoFijo`** — Descuento de un monto fijo en dinero, aplicable bajo las mismas condiciones de monto mínimo. Nunca descuenta más de lo que vale el subtotal.

**`DescuentoClienteVip`** — Descuento porcentual exclusivo para clientes marcados como VIP; su condición de aplicabilidad depende del cliente, no del subtotal.

### Paquete `servicio`

**`CalculadoraFactura`** — Responsable único del cálculo: suma los subtotales de los detalles, aplica las estrategias de descuento que correspondan y determina el total final (nunca menor a cero).

**`ServicioFacturacion`** — Orquesta el proceso completo de facturación llamando, en orden, a `CalculadoraFactura`, `RepositorioFactura` e `ImpresoraFactura`. Recibe estas tres dependencias por constructor, sin instanciarlas él mismo.

### Paquete `impresion`

**`ImpresoraFactura`** (interfaz) — Define el método `imprimir(factura)`.

**`ImpresoraFacturaConsola`** — Implementación que muestra el detalle completo de la factura (cliente, líneas, subtotal, descuentos aplicados y total) por consola.

### Paquete `repositorio`

**`RepositorioFactura`** (interfaz) — Define `guardar`, `buscarPorId` y `listarTodas`.

**`RepositorioFacturaEnMemoria`** — Implementación que guarda las facturas en un `Map` en memoria (sin base de datos real); útil para pruebas y demostraciones.

### Paquete `app`

**`AplicacionPrincipal`** — Contiene el método `main`. Es el único lugar del programa donde se instancian clases concretas directamente (composition root): arma las dependencias, crea los datos de ejemplo y dispara el procesamiento de la factura.

## Principios SOLID aplicados

- **Responsabilidad única (SRP):** cada clase tiene un solo motivo para cambiar — el modelo solo representa datos, `CalculadoraFactura` solo calcula, `ImpresoraFacturaConsola` solo imprime, `RepositorioFacturaEnMemoria` solo persiste.
- **Abierto/cerrado (OCP):** se pueden agregar nuevos tipos de descuento creando una nueva clase que implemente `EstrategiaDescuento`, sin modificar `CalculadoraFactura` ni `ServicioFacturacion`.
- **Sustitución de Liskov (LSP):** cualquier implementación de `EstrategiaDescuento` puede sustituir a otra en la lista de estrategias sin romper el cálculo.
- **Segregación de interfaces (ISP):** `EstrategiaDescuento`, `ImpresoraFactura` y `RepositorioFactura` son interfaces pequeñas y específicas, sin métodos que las implementaciones no necesiten.
- **Inversión de dependencias (DIP):** `ServicioFacturacion` depende únicamente de abstracciones, recibidas por constructor, nunca de clases concretas.

## Cómo agregar un nuevo tipo de descuento

1. Crear una nueva clase en el paquete `descuento` que implemente `EstrategiaDescuento`.
2. Implementar `esAplicable`, `calcularDescuento` y `obtenerDescripcion` según la regla deseada.
3. Agregar una instancia de esa clase a la lista de estrategias que se le pasa a `ServicioFacturacion.procesarFactura(...)`.

No es necesario modificar ninguna clase existente para que el nuevo descuento funcione.
