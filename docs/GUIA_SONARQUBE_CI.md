# Guía de calidad continua: SonarQube, cobertura, Karate, trunk-based e integración continua

Proyecto: **Sistema de Facturación con Descuentos** (Java 17 + Gradle + JUnit 5 + Mockito)
Asignatura: Calidad de Software — Universidad de Antioquia

Esta guía explica **qué se agregó al proyecto, para qué sirve cada pieza, cómo ejecutarla paso a paso y cómo interpretar los resultados**. Está pensada para que puedas explicarla en la sustentación.

---

## Índice

1. [Qué se agregó al proyecto](#1-qué-se-agregó-al-proyecto)
2. [Visión general: el flujo completo](#2-visión-general-el-flujo-completo)
3. [Preparación inicial (una sola vez)](#3-preparación-inicial-una-sola-vez)
4. [SonarQube: levantarlo y ejecutar el primer análisis](#4-sonarqube-levantarlo-y-ejecutar-el-primer-análisis)
5. [Análisis estático de código](#5-análisis-estático-de-código)
6. [Code smells](#6-code-smells)
7. [Deuda técnica](#7-deuda-técnica)
8. [Complejidad ciclomática y cognitiva](#8-complejidad-ciclomática-y-cognitiva)
9. [Cobertura (coverage) con JaCoCo](#9-cobertura-coverage-con-jacoco)
10. [Quality Gate (puerta de calidad)](#10-quality-gate-puerta-de-calidad)
11. [Pruebas de aceptación con Karate](#11-pruebas-de-aceptación-con-karate)
12. [Trunk-based development: incrementos sobre el trunk](#12-trunk-based-development-incrementos-sobre-el-trunk)
13. [Integración continua (pipeline)](#13-integración-continua-pipeline)
14. [Generar el artefacto](#14-generar-el-artefacto)
15. [Checklist: orden recomendado de ejecución](#15-checklist-orden-recomendado-de-ejecución)
16. [Problemas frecuentes](#16-problemas-frecuentes)
17. [Qué se verificó y qué no](#17-qué-se-verificó-y-qué-no)
18. [Preguntas probables del profesor](#18-preguntas-probables-del-profesor)

---

## 1. Qué se agregó al proyecto

| Archivo | Para qué sirve |
|---|---|
| `build.gradle` (modificado) | Agrega los plugins `jacoco` (cobertura) y `org.sonarqube` (envío del análisis), la dependencia de Karate, la tarea `pruebasAceptacion`, la configuración de Sonar y el manifiesto del jar ejecutable. |
| `src/test/resources/karate/facturacion.feature` | Escenarios de aceptación en lenguaje Gherkin (Given / When / Then). |
| `src/test/java/com/facturacion/aceptacion/AceptacionKarateTest.java` | "Ejecutor" (runner) que lanza los `.feature` desde JUnit 5. |
| `docker-compose.yml` | Levanta un SonarQube local con un solo comando. |
| `.github/workflows/ci.yml` | Pipeline de integración continua (construir, probar, analizar, guardar artefacto). |
| `.github/workflows/release.yml` | Genera una versión publicada (release) con el jar cuando se crea una etiqueta `vX.Y.Z`. |
| `.gitignore` | Evita subir al repositorio la carpeta `build/`, configuraciones de IDE y secretos. |
| `docs/GUIA_SONARQUBE_CI.md` | Este documento. |

---

## 2. Visión general: el flujo completo

```
 Desarrollador                          GitHub                               SonarQube
 ─────────────                          ──────                               ─────────
 rama corta  ──push──►  Pull Request ──► CI (ci.yml)
                                          1. Compilar
                                          2. Pruebas unitarias (JUnit/Mockito)
                                          3. Pruebas de aceptación (Karate)
                                          4. Cobertura (JaCoCo → XML)
                                          5. Análisis estático  ───────────────►  reglas, code smells,
                                                                                   deuda, complejidad,
                                                                                   cobertura
                                          6. Quality Gate  ◄──────────────────── ¿pasa o falla?
                                          7. Guardar artefacto (jar) + reportes
              ◄── ✅ / ❌ ──── merge a main (trunk) solo si todo está en verde
                                          │
                       etiqueta v1.1.0 ──►  release.yml → jar publicado en GitHub Releases
```

Idea central: **ningún cambio entra al trunk (`main`) sin pasar por pruebas, análisis estático y Quality Gate**, y de cada versión buena sale un artefacto (jar) reproducible.

---

## 3. Preparación inicial (una sola vez)

### 3.1 Requisitos

- JDK 17 (el `build.gradle` lo exige mediante *toolchain*).
- Git y una cuenta de GitHub.
- Docker Desktop (para SonarQube local).
- Gradle instalado **solo para generar el wrapper**.

### 3.2 Generar el Gradle Wrapper

El proyecto todavía **no incluye** `gradlew` / `gradlew.bat`. Los necesitas porque el pipeline usa `./gradlew`. En la carpeta del proyecto:

```bash
gradle wrapper --gradle-version 8.14.3
```

Esto crea `gradlew`, `gradlew.bat` y la carpeta `gradle/wrapper/`. **Hay que subirlos al repositorio** (por eso `.gitignore` no los excluye). Con el wrapper, cualquier persona o servidor ejecuta el proyecto con la misma versión de Gradle sin instalarla.

Comprueba que funciona:

```bash
./gradlew --version        # Windows: .\gradlew.bat --version
./gradlew build            # compila y ejecuta pruebas unitarias + aceptación + cobertura
```

### 3.3 Crear el repositorio

```bash
git init -b main
git add .
git commit -m "chore: proyecto inicial con pruebas, Sonar, Karate y CI"
git remote add origin https://github.com/TU_USUARIO/facturacion-descuentos.git
git push -u origin main
```

---

## 4. SonarQube: levantarlo y ejecutar el primer análisis

### 4.1 Levantar SonarQube local

```bash
docker compose up -d
```

- En **Linux** ejecuta antes: `sudo sysctl -w vm.max_map_count=524288` (SonarQube lo exige).
- Espera 1–2 minutos y abre <http://localhost:9000>. Credenciales iniciales: `admin` / `admin` (te pedirá cambiar la clave).
- Si quieres ver el arranque: `docker compose logs -f`.

> El `docker-compose.yml` usa la imagen `sonarqube:community` (SonarQube Community Build, gratuita) con base de datos embebida. Es suficiente para el laboratorio; en producción se usa PostgreSQL.

### 4.2 Crear el proyecto y el token

1. En la interfaz web: **Create project → Manually**.
2. *Project display name:* `Sistema de Facturacion con Descuentos`. *Project key:* `facturacion-descuentos` (debe coincidir con `sonar.projectKey` en `build.gradle`).
3. Elige analizar **Locally** y **genera un token** (también en *My Account → Security → Generate Tokens*). Cópialo: solo se muestra una vez.

> Los nombres exactos de los menús pueden variar un poco según la versión de SonarQube.

### 4.3 Ejecutar el análisis

**Linux / macOS:**

```bash
export SONAR_TOKEN=pega_aqui_tu_token
export SONAR_HOST_URL=http://localhost:9000
./gradlew sonar
```

**Windows (PowerShell):**

```powershell
$env:SONAR_TOKEN = "pega_aqui_tu_token"
$env:SONAR_HOST_URL = "http://localhost:9000"
.\gradlew.bat sonar
```

La tarea `sonar` depende de `check`, así que **primero** compila, ejecuta las pruebas unitarias y de aceptación y genera el reporte de cobertura, y **después** envía todo a SonarQube. Al terminar, abre <http://localhost:9000/dashboard?id=facturacion-descuentos>.

> ⚠️ **Nunca escribas el token dentro de `build.gradle` ni lo subas a Git.** Se pasa por variable de entorno (local) o por *secret* (GitHub).

### 4.4 Cómo leer el panel

| Pestaña | Qué muestra |
|---|---|
| **Overview** | Estado del Quality Gate, resumen de bugs, vulnerabilidades, code smells, cobertura y duplicación. |
| **Issues** | Lista de hallazgos con la regla, la severidad y el esfuerzo estimado para corregirlos. |
| **Measures** | Todas las métricas: complejidad, deuda técnica, líneas de código, cobertura, etc. |
| **Code** | Navegación archivo por archivo, con cobertura y complejidad por clase. |
| **Activity** | Evolución en el tiempo (útil para mostrar la mejora entre análisis). |

---

## 5. Análisis estático de código

**Qué es:** analizar el código fuente **sin ejecutarlo**, aplicando reglas automáticas que detectan problemas. Complementa a las pruebas: las pruebas verifican *comportamiento en ejecución*; el análisis estático revisa *cómo está escrito el código*.

SonarQube clasifica los hallazgos en cuatro grandes tipos:

| Tipo | Significado | Ejemplo |
|---|---|---|
| **Bug** | Código que probablemente falle o se comporte mal. | Posible `NullPointerException`. |
| **Vulnerabilidad** | Fallo explotable por un atacante. | Contraseña escrita en el código. |
| **Security Hotspot** | Código sensible que requiere **revisión humana**. | Uso de números aleatorios en seguridad. |
| **Code smell** | "Mal olor": el código funciona pero es difícil de mantener (ver sección 6). | Variable declarada y nunca usada. |

El conjunto de reglas activas se llama **Quality Profile** (por defecto, *Sonar way* para Java). Cada regla tiene un identificador, por ejemplo `java:S106`.

> En versiones recientes de SonarQube la interfaz agrupa los hallazgos por *cualidad de software* (mantenibilidad, fiabilidad, seguridad) con niveles de impacto, en lugar de los tres tipos clásicos. El concepto es el mismo.

---

## 6. Code smells

Un **code smell** no es un error: el programa funciona, pero el código es más difícil de leer, cambiar o probar, y eso genera costo futuro (deuda técnica).

Ejemplos típicos que detecta Sonar: código duplicado, métodos demasiado largos o complejos, variables o importaciones sin usar, números "mágicos", clases con demasiadas responsabilidades, uso de `System.out` en lugar de un logger, código comentado.

### Hallazgos probables en este proyecto

> Estos dos puntos los identifiqué **revisando el código a mano**; no pude ejecutar SonarQube en mi entorno. Verifica en tu panel si aparecen y con qué severidad.

| Dónde | Regla probable | Por qué es un smell | Cómo corregirlo |
|---|---|---|---|
| `ImpresoraFacturaConsola` (usa `System.out.println` / `printf`) | `java:S106` | Las salidas estándar no se pueden configurar ni filtrar como un logger. | Es un caso discutible: la clase **existe justamente para imprimir por consola**. Puedes marcarlo como *Won't fix / Accepted* con justificación, o pasar a un `Logger`. |
| `ServicioFacturacionMockTest.noDebeImprimirNiGuardarSiLaFacturaNoSeProcesa` | `java:S1481` (variable local sin usar) | La variable `servicio` se crea pero nunca se usa. | Eliminar la variable (basta con `verifyNoInteractions(...)`) o usarla en la prueba. |

### Mejora de diseño relacionada (no es una regla de Sonar)

El sistema representa dinero con `double`. En sistemas reales se usa `BigDecimal` para evitar errores de redondeo. Puedes mencionarlo como **deuda técnica identificada manualmente** y como mejora futura.

### Ciclo de mejora (lo ideal para mostrar al profesor)

1. Ejecutar `./gradlew sonar` → anotar cuántos smells y cuánta deuda hay.
2. Corregir uno o dos hallazgos en una rama corta.
3. Volver a ejecutar el análisis → mostrar que la métrica **bajó** (pestaña *Activity*).

---

## 7. Deuda técnica

**Qué es:** metáfora financiera. Cada atajo o mala práctica es una "deuda" que luego se "paga con intereses": más tiempo para entender, modificar y corregir el código.

SonarQube la **cuantifica en tiempo**: cada issue de mantenibilidad lleva un **esfuerzo de remediación estimado** (minutos/horas), y la deuda técnica total es la suma de esos esfuerzos.

### Métricas relacionadas

| Métrica | Cómo se calcula | Cómo leerla |
|---|---|---|
| **Deuda técnica** (*Technical Debt*) | Suma del esfuerzo estimado de todos los issues de mantenibilidad. | "Cuánto tiempo costaría dejar el código limpio". |
| **Ratio de deuda** (*Debt Ratio*) | Deuda ÷ costo estimado de desarrollar el código (por defecto, 30 min por línea de código). | Porcentaje: qué parte del esfuerzo de construcción hay que "repagar". |
| **Maintainability Rating** | Letra derivada del ratio de deuda. | Resumen rápido para el Quality Gate. |

### Calificación (rating) de mantenibilidad

| Ratio de deuda | Rating |
|---|---|
| ≤ 5 % | **A** |
| 6 % – 10 % | **B** |
| 11 % – 20 % | **C** |
| 21 % – 50 % | **D** |
| > 50 % | **E** |

Un proyecto pequeño y limpio como este debería moverse entre **A** y **B**.

### Cómo reducirla

- Corregir primero los issues de mayor severidad y menor esfuerzo.
- Evitar introducir deuda **nueva**: el Quality Gate por defecto se enfoca en el **código nuevo** (*new code*), no en la deuda histórica.

---

## 8. Complejidad ciclomática y cognitiva

Ambas miden qué tan complicado es un método, pero responden preguntas distintas.

| | **Ciclomática** (McCabe) | **Cognitiva** (SonarSource) |
|---|---|---|
| Pregunta que responde | ¿Cuántos caminos de ejecución independientes hay? | ¿Qué tan difícil es **entender** este código al leerlo? |
| Orientada a | Pruebas (mínimo de casos para cubrir todos los caminos). | Lectura humana. |
| Cómo cuenta | +1 por cada `if`, `for`, `while`, `case`, `catch`, `&&`, `||`, `?:`. Base = 1 por método. | +1 por cada estructura de control, **más un extra por cada nivel de anidamiento**; +1 por cada secuencia de operadores lógicos; ignora estructuras que no aportan dificultad (p. ej. un `switch` cuenta una vez, no por cada `case`). |
| Umbral típico de Sonar | Regla `java:S1541` (por defecto 10 por método; no siempre activa en *Sonar way*). | Regla `java:S3776` (por defecto 15 por método). |

### Ejemplo 1 — `CalculadoraFactura.calcular(...)`

```java
for (EstrategiaDescuento estrategia : estrategiasDescuento) {      // +1
    if (estrategia.esAplicable(factura, subtotal)) {               // +1 (+1 por estar anidado)
        ...
    }
}
```

- **Ciclomática = 3** → 1 (base) + `for` + `if`.
- **Cognitiva = 3** → `for` (+1) + `if` (+1) + penalización por anidamiento dentro del `for` (+1).

Por qué difieren: el `if` dentro del `for` es **más difícil de leer** que un `if` suelto, y la cognitiva lo refleja; la ciclomática solo cuenta caminos.

### Ejemplo 2 — constructor de `DescuentoPorcentaje`

```java
if (porcentaje < 0 || porcentaje > 1) {   // if (+1) y secuencia de || (+1)
    throw new IllegalArgumentException(...);
}
```

- **Ciclomática = 3** → 1 (base) + `if` + `||`.
- **Cognitiva = 2** → `if` (+1) + la secuencia de `||` (+1).

### Dónde verlo en SonarQube

*Measures → Complexity*: complejidad ciclomática y cognitiva por proyecto, carpeta y archivo. Los métodos de este proyecto son pequeños, así que los valores deberían ser bajos (esa es justamente una ventaja de aplicar SRP y Strategy: en lugar de un método gigante con muchos `if`, cada estrategia es una clase simple).

### Cómo reducir complejidad

Extraer métodos, usar *guard clauses* (retornos tempranos), reemplazar cadenas de `if/else` por polimorfismo (justo lo que hace el patrón Strategy de este proyecto).

---

## 9. Cobertura (coverage) con JaCoCo

**Qué mide:** qué porcentaje del código **se ejecutó** mientras corrían las pruebas.

| Tipo | Significado |
|---|---|
| **Cobertura de líneas** | % de líneas ejecutadas por alguna prueba. |
| **Cobertura de ramas** (*branch*) | % de caminos de cada decisión (`if`, `&&`, ...) recorridos. Un `if` con solo el caso verdadero probado da 50 % de ramas. |

> Cobertura alta **no garantiza** pruebas buenas (se puede ejecutar código sin verificar nada), pero cobertura baja **sí indica** zonas sin probar.

### Cómo funciona en este proyecto

1. El plugin `jacoco` adjunta un **agente** a la JVM de las pruebas y registra qué instrucciones se ejecutan (archivos `build/jacoco/*.exec`).
2. La tarea `jacocoTestReport` **une** la cobertura de las pruebas unitarias (`test`) y las de aceptación (`pruebasAceptacion`, porque Karate ejecuta las clases del sistema dentro de la misma JVM) y genera:
   - `build/reports/jacoco/test/jacocoTestReport.xml` → lo lee SonarQube.
   - `build/reports/jacoco/test/html/index.html` → para revisarlo tú en el navegador (verde = cubierto, rojo = no cubierto, amarillo = ramas parciales).
3. `build.gradle` le indica a Sonar la ruta del XML con `sonar.coverage.jacoco.xmlReportPaths`.

Comando para generar solo la cobertura:

```bash
./gradlew check jacocoTestReport
```

### Zonas que probablemente queden sin cubrir

- `AplicacionPrincipal.main(...)`: ningún test lo invoca.
- `ImpresoraFacturaConsola`: se usa solo dentro de pruebas que la reemplazan por *mocks* o que no inspeccionan la salida.

Opciones: (a) escribir una prueba que capture `System.out` para `ImpresoraFacturaConsola`; (b) excluir `AplicacionPrincipal` de la cobertura con `sonar.coverage.exclusions` (hazlo solo con justificación; excluir para "maquillar" el porcentaje no es buena práctica).

---

## 10. Quality Gate (puerta de calidad)

Es un **conjunto de condiciones que el código debe cumplir** para considerarse aceptable. El resultado es binario: **Passed** o **Failed**.

El Quality Gate por defecto (*Sonar way*) evalúa el **código nuevo**, con condiciones del estilo (pueden variar por versión):

- Cobertura del código nuevo ≥ 80 %.
- Líneas duplicadas del código nuevo ≤ 3 %.
- Ratings de fiabilidad, seguridad y mantenibilidad del código nuevo en **A**.
- Todos los *Security Hotspots* nuevos revisados.

`build.gradle` define `sonar.qualitygate.wait = true`, así que `./gradlew sonar` **espera** el veredicto y **falla (exit ≠ 0)** si el gate no pasa. Eso es lo que pone el pipeline en rojo y bloquea el merge.

> ⚠️ **Primer análisis:** SonarQube considera *todo* el código como "nuevo", así que el primer análisis puede fallar por cobertura < 80 %. No es un error de configuración: es el momento perfecto para mostrar el ciclo "medir → mejorar → volver a medir".

---

## 11. Pruebas de aceptación con Karate

### 11.1 Qué son y en qué se diferencian

| | Pruebas unitarias | Pruebas de aceptación |
|---|---|---|
| Pregunta | ¿Esta clase/método funciona bien? | ¿El sistema hace lo que el **negocio** espera? |
| Lenguaje | Código Java (JUnit). | Lenguaje de negocio (Gherkin), legible por no programadores. |
| Alcance | Una unidad, aislada (a veces con mocks). | Varias clases reales juntas, un caso de uso completo. |
| Quién las entiende | Desarrolladores. | Desarrolladores, analistas, cliente, profesor. |

### 11.2 Qué es Karate

Un framework para pruebas de aceptación escritas en archivos `.feature` (sintaxis Gherkin). Es muy conocido para probar **APIs REST**, pero también puede **llamar clases Java directamente** (*Java interop*). Como este proyecto es una aplicación de consola/biblioteca y no expone una API HTTP, usamos esa segunda opción.

### 11.3 Cómo está armado

**`facturacion.feature`** (en `src/test/resources/karate/`):

```gherkin
Feature: Facturación con descuentos        # nombre de la funcionalidad

  Background:                               # se ejecuta ANTES de cada escenario
    * def Cliente = Java.type('com.facturacion.modelo.Cliente')   # importa la clase Java
    * def cerca = function(a, b) { return Math.abs(a - b) < 0.001 }   # comparar decimales

  Scenario: Un cliente VIP recibe el descuento por porcentaje y el descuento VIP acumulados
    Given def cliente = new Cliente('CLI-1', 'Ana Gomez', true)     # Arrange: preparar datos
    ...
    When eval new CalculadoraFactura().calcular(factura, estrategias)   # Act: ejecutar la acción
    Then def total = factura.obtenerTotal()                         # Assert: verificar
    And assert cerca(total, 680.0)
```

- `Given / When / Then / And` equivalen a **Arrange / Act / Assert**. En Karate son solo etiquetas de lectura: las palabras clave van en inglés, el texto del escenario puede ir en español.
- `* def x = ...` define una variable; `eval` ejecuta una expresión sin guardar su resultado; `assert` verifica una condición; `match` compara valores.
- `Scenario Outline` + `Examples` es el equivalente de una prueba **parametrizada**: el mismo escenario se ejecuta una vez por cada fila de la tabla, reemplazando `<precio>`, `<cantidad>`, `<totalEsperado>`.

**Escenarios incluidos:**

| # | Escenario | Qué valida |
|---|---|---|
| 1 | Cliente VIP: porcentaje + VIP | Los descuentos se acumulan: 800 − 80 − 40 = **680**, con 2 descuentos aplicados. |
| 2 | Cliente regular | El descuento VIP **no** se aplica: total **250**, 1 descuento. |
| 3 | Sin descuentos | Total = subtotal (**75**). |
| 4 | Monto fijo mayor al subtotal | El total nunca es negativo (**0**). |
| 5 (Outline) | Monto mínimo (3 filas) | Debajo del mínimo → sin descuento (100); exactamente el mínimo → con descuento (180); por encima (540). |

**`AceptacionKarateTest.java`** es el *runner*: usa `Runner.path("classpath:karate")` para ejecutar todos los `.feature`, genera el reporte HTML en `build/karate-reports` y hace fallar la prueba JUnit si algún escenario falla. Lleva `@Tag("aceptacion")` para que Gradle lo separe de las unitarias.

### 11.4 Ejecutarlas

```bash
./gradlew pruebasAceptacion
```

Resultado visual: abre `build/karate-reports/karate-summary.html` en el navegador (resumen por feature y detalle paso a paso).

### 11.5 Cómo evolucionaría con una API REST

Si más adelante el sistema se expone con Spring Boot, el mismo estilo sirve para probar HTTP:

```gherkin
Scenario: Crear una factura con descuento VIP
  Given url 'http://localhost:8080/api/facturas'
  And request { cliente: 'CLI-1', items: [ { producto: 'P1', cantidad: 1 } ] }
  When method post
  Then status 201
  And match response.total == 680
```

> Versión usada: `com.intuit.karate:karate-junit5:1.4.1`, la última estable publicada bajo ese *groupId*. Las versiones 1.5.x migraron al groupId `io.karatelabs` y, al momento de escribir esto, aparecen como *release candidates*.

---

## 12. Trunk-based development: incrementos sobre el trunk

### 12.1 Qué es

Estrategia de ramas donde **todos integran sus cambios en una única rama principal (el *trunk*, aquí `main`) con mucha frecuencia**, en incrementos pequeños, y el trunk **siempre está listo para liberar**.

| | **Trunk-based** | **Gitflow** (alternativa clásica) |
|---|---|---|
| Ramas principales | Una: `main`. | `main` + `develop` + `release/*` + `hotfix/*`. |
| Vida de las ramas de trabajo | Horas o 1–2 días. | Días o semanas. |
| Integración | Varias veces al día. | Al terminar la funcionalidad. |
| Riesgo | Conflictos pequeños y detectados temprano. | "Integración infernal" al final. |
| Requisito | CI rápida y buenas pruebas automáticas. | Menor, pero libera más lento. |

### 12.2 Reglas prácticas

1. `main` siempre compila y pasa todas las pruebas.
2. Las ramas de trabajo son **cortas** (idealmente < 2 días) y de **un solo propósito**.
3. Cambios **pequeños**: un incremento = una idea (una prueba, una corrección, una funcionalidad pequeña).
4. Todo entra por **pull request**, con la CI en verde.
5. Si una funcionalidad no está terminada pero hay que integrar, se esconde detrás de una **bandera de funcionalidad** (*feature flag*) en vez de mantener una rama larga.
6. Se borra la rama después del merge.

### 12.3 Ciclo de un incremento

```bash
git switch main && git pull                      # 1. partir del trunk actualizado
git switch -c mejora/prueba-impresora            # 2. rama corta y descriptiva
# ... hacer UN cambio pequeño ...
./gradlew build                                  # 3. verificar localmente
git add -A
git commit -m "test: agrega prueba para ImpresoraFacturaConsola"
git push -u origin mejora/prueba-impresora       # 4. subir la rama
# 5. abrir Pull Request → la CI corre automáticamente
# 6. con CI en verde: merge (squash) a main y borrar la rama
git switch main && git pull                      # 7. sincronizar y repetir
```

### 12.4 Ejemplo de tres incrementos consecutivos

| Incremento | Contenido | Resultado esperado en Sonar |
|---|---|---|
| 1 | Subir el proyecto con pipeline y análisis. | Línea base de métricas. |
| 2 | Corregir el code smell de la variable sin usar en el test. | Baja el número de code smells y la deuda. |
| 3 | Agregar prueba de `ImpresoraFacturaConsola`. | Sube la cobertura. |

Cada incremento es un PR pequeño que pasa por la CI completa; así se ve la evolución de calidad en la pestaña *Activity* de Sonar.

### 12.5 Proteger el trunk en GitHub

*Settings → Branches → Add branch protection rule* para `main`:

- ✅ Require a pull request before merging.
- ✅ Require status checks to pass before merging → selecciona **"Construir, probar y analizar"**.
- ✅ Require branches to be up to date before merging.
- ✅ Require linear history (opcional, combina bien con *squash merge*).

---

## 13. Integración continua (pipeline)

**Integración continua (CI):** cada vez que alguien integra código, un servidor **construye y verifica automáticamente** el proyecto, para detectar problemas en minutos y no al final.

### 13.1 Explicación de `.github/workflows/ci.yml`

| Bloque | Qué hace |
|---|---|
| `on: push / pull_request` a `main` | Dispara el pipeline en cada push al trunk y en cada PR. |
| `concurrency` | Si llega otro commit a la misma rama, cancela la ejecución anterior (ahorra tiempo). |
| `permissions: contents: read` | Principio de mínimo privilegio para el token del pipeline. |
| `actions/checkout` con `fetch-depth: 0` | Descarga el repo **con historial completo** (Sonar lo necesita para el "código nuevo" y la autoría). |
| `actions/setup-java` (Temurin 17) | Instala el JDK. |
| `gradle/actions/setup-gradle` | Prepara Gradle y **cachea** dependencias entre ejecuciones. |
| `./gradlew build` | Compila, corre pruebas unitarias, pruebas de aceptación y genera cobertura. |
| `./gradlew sonar` | Envía el análisis y **espera el Quality Gate**. Solo corre si existe el secreto `SONAR_TOKEN`. |
| `upload-artifact` (jar) | Guarda el **artefacto** ejecutable. |
| `upload-artifact` (reportes) | Guarda reportes de pruebas, JaCoCo y Karate; con `if: always()` se suben incluso si algo falló. |

### 13.2 Configurar los secretos

*GitHub → Settings → Secrets and variables → Actions → New repository secret*:

| Secreto | Valor |
|---|---|
| `SONAR_TOKEN` | El token generado en SonarQube (o SonarCloud). |
| `SONAR_HOST_URL` | La URL del servidor de Sonar. |

### 13.3 Importante: ¿dónde corre SonarQube?

Los servidores de GitHub Actions **no pueden alcanzar `http://localhost:9000`** de tu computador. Opciones:

| Opción | Cuándo usarla |
|---|---|
| **SonarCloud** (el servicio en la nube de Sonar, actualmente también llamado *SonarQube Cloud*) | La más sencilla para CI. `SONAR_HOST_URL = https://sonarcloud.io`. Requiere además `sonar.organization`: en el paso de Sonar usa `./gradlew sonar -Dsonar.organization=TU_ORGANIZACION`. Ofrece análisis de ramas y pull requests. |
| **SonarQube en un servidor accesible** | Si tu universidad o equipo tiene uno. |
| **Runner autoalojado** (*self-hosted*) en tu equipo | Para que el pipeline vea tu `localhost`. |
| **Solo local + CI sin Sonar** | Si no quieres configurar nada en la nube: el paso de Sonar se omite solo (por la condición del secreto) y corres `./gradlew sonar` en tu máquina. |

> SonarQube **Community Build** analiza una sola rama; el análisis de ramas y de *pull requests* requiere ediciones de pago o SonarCloud.

---

## 14. Generar el artefacto

Un **artefacto** es el resultado empaquetado y versionado de una construcción, listo para distribuirse o desplegarse. Aquí es un **jar ejecutable**.

### 14.1 Generarlo local

```bash
./gradlew build
java -jar build/libs/facturacion-descuentos-1.0.0.jar
```

Funciona porque `build.gradle` declara en el manifiesto `Main-Class: com.facturacion.app.AplicacionPrincipal`, y el proyecto no tiene dependencias en tiempo de ejecución (JUnit, Mockito y Karate son solo de prueba), por lo que no hace falta un "fat jar".

Para verificar el manifiesto:

```bash
unzip -p build/libs/facturacion-descuentos-1.0.0.jar META-INF/MANIFEST.MF
```

### 14.2 Versionado

La versión sale de `-PversionApp` (por defecto `1.0.0`):

```bash
./gradlew build -PversionApp=1.1.0      # genera facturacion-descuentos-1.1.0.jar
```

Se recomienda **versionado semántico** `MAYOR.MENOR.PARCHE`: parche para correcciones, menor para funcionalidades compatibles, mayor para cambios incompatibles.

### 14.3 Artefacto en cada ejecución de la CI

En GitHub: **Actions → (ejecución) → sección *Artifacts*** → `facturacion-descuentos-jar-N` (el jar) y `reportes-N` (HTML de pruebas, JaCoCo y Karate).

### 14.4 Artefacto de versión (release)

```bash
git switch main && git pull
git tag v1.1.0
git push origin v1.1.0
```

`release.yml` detecta la etiqueta, extrae la versión (`v1.1.0` → `1.1.0`), construye y prueba con esa versión y crea una **GitHub Release** con el jar adjunto y notas generadas automáticamente.

---

## 15. Checklist: orden recomendado de ejecución

1. [ ] `gradle wrapper --gradle-version 8.14.3` y comprobar `./gradlew build` en local.
2. [ ] `docker compose up -d` y crear el proyecto + token en <http://localhost:9000>.
3. [ ] `./gradlew sonar` (con `SONAR_TOKEN` y `SONAR_HOST_URL`) → capturar el **primer análisis** (línea base).
4. [ ] Revisar en Sonar: code smells, deuda, complejidad, cobertura, Quality Gate.
5. [ ] `git init`, primer commit y push a GitHub.
6. [ ] Configurar secretos y protección de rama (secciones 12.5 y 13.2).
7. [ ] Hacer 2–3 incrementos pequeños por PR (sección 12.4) y comprobar que la CI se pone en verde.
8. [ ] Descargar el artefacto desde *Actions*.
9. [ ] Crear la etiqueta `v1.0.0` y verificar la release con el jar.
10. [ ] Tomar capturas: panel de Sonar, reporte JaCoCo, reporte Karate, ejecución del pipeline, artefacto.

---

## 16. Problemas frecuentes

| Síntoma | Causa probable | Solución |
|---|---|---|
| `./gradlew: No such file` | No se generó el wrapper. | `gradle wrapper --gradle-version 8.14.3` y subir los archivos creados. |
| Sonar se queda en "waiting" o no abre `localhost:9000` | Aún arrancando o falta `vm.max_map_count` (Linux). | Esperar 1–2 min; `docker compose logs -f`; `sudo sysctl -w vm.max_map_count=524288`. |
| `Not authorized` / `401` | Token vacío, expirado o mal copiado. | Generar otro token y revisar `SONAR_TOKEN`. |
| `Project not found` | La `projectKey` del servidor no coincide con la de `build.gradle`. | Debe ser `facturacion-descuentos` en ambos. |
| El Quality Gate falla en el primer análisis | Todo el código cuenta como nuevo; cobertura < 80 %. | Es esperado: mejora pruebas/smells y vuelve a analizar. |
| Sonar muestra cobertura 0 % | No se generó el XML de JaCoCo antes del análisis. | Usar `./gradlew sonar` (ya depende de `check`) y verificar que exista `build/reports/jacoco/test/jacocoTestReport.xml`. |
| Karate: "no features found" | Ruta del runner incorrecta. | Los `.feature` deben estar en `src/test/resources/karate/` y el runner usar `classpath:karate`. |
| Letras como `?` en la consola (`Gómez` → `G?mez`) | La consola no está en UTF-8. | Windows: `chcp 65001`; o ejecutar con `JAVA_TOOL_OPTIONS=-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8`. No es un error del código. |
| Gradle pide JDK 17 y no lo encuentra | El *toolchain* exige Java 17. | Instalar JDK 17 y confirmar con `java -version`. |
| El paso de Sonar no corre en CI | No existe el secreto `SONAR_TOKEN` (o el PR viene de un fork). | Crear el secreto; es el comportamiento esperado en forks. |

---

## 17. Qué se verificó y qué no

Esta configuración se preparó en un entorno **sin acceso a los repositorios de dependencias** (Gradle, Maven Central, Plugin Portal), por lo que:

**✅ Verificado en el entorno:**
- El código principal **compila** con `javac` y el `main` **se ejecuta** correctamente.
- Los **valores esperados de los 7 casos de Karate** (680, 250, 75, 0, 100, 180, 540) se calcularon ejecutando las clases reales y **coinciden**.
- `build.gradle` **evalúa sin errores de sintaxis** y las tareas `pruebasAceptacion`, `jacocoTestReport` y `check` quedaron registradas (se comprobó sin el plugin `org.sonarqube`, que no se pudo descargar).
- Las versiones (plugin de Sonar `7.3.0.8198`, Karate `1.4.1`, imagen `sonarqube:community`) se consultaron en sus fuentes oficiales.

**⚠️ No verificado (hazlo en tu máquina):**
- La descarga de dependencias y la ejecución real de **JaCoCo, Karate y SonarQube**.
- Los **workflows de GitHub Actions** en un repositorio real.
- Los hallazgos concretos de Sonar (los de la sección 6 son probables, no confirmados) y los números finales de cobertura, deuda y complejidad.

Si algo falla al ejecutarlo, copia el mensaje de error completo: casi siempre es un ajuste pequeño.

---

## 18. Preguntas probables del profesor

**¿Qué diferencia hay entre complejidad ciclomática y cognitiva?**
La ciclomática cuenta los caminos de ejecución independientes (sirve para saber cuántas pruebas se necesitan). La cognitiva mide qué tan difícil es entender el código; penaliza el anidamiento y las condiciones encadenadas. Por eso un `if` dentro de un `for` suma más en la cognitiva que en la ciclomática.

**¿Qué es la deuda técnica y cómo la mide SonarQube?**
Es el costo futuro de haber escrito código difícil de mantener. SonarQube la mide en tiempo, sumando el esfuerzo estimado de corregir cada issue de mantenibilidad, y la convierte en un ratio (deuda ÷ costo de desarrollo) que da una calificación de A a E.

**¿Un code smell es un error?**
No. El programa funciona, pero el diseño o el estilo dificultan cambiarlo, leerlo o probarlo.

**¿Cobertura del 100 % significa que no hay errores?**
No. Solo indica que las pruebas *ejecutaron* ese código. Las pruebas pueden no verificar nada relevante. Por eso se combina cobertura con buenas aserciones y pruebas de casos límite.

**¿Qué es un Quality Gate y por qué hace fallar el pipeline?**
Es el conjunto de condiciones mínimas de calidad. Con `sonar.qualitygate.wait=true`, el análisis espera el veredicto y termina con error si falla, lo que pone el pipeline en rojo y bloquea el merge.

**¿Por qué las pruebas de aceptación van separadas de las unitarias?**
Porque validan cosas distintas (el negocio, con varias clases reales) y suelen ser más lentas. Separarlas en una tarea y una etapa propia permite ver rápidamente qué nivel de prueba falló.

**¿Por qué Karate si el proyecto no tiene API?**
Karate permite llamar clases Java directamente; sirve para describir el comportamiento en lenguaje de negocio hoy y, si el sistema se expone como API, el mismo enfoque cubre peticiones HTTP.

**¿Qué ventaja tiene el trunk-based frente a ramas largas?**
Los cambios se integran en pequeño y con frecuencia, los conflictos son menores y se detectan antes, y el trunk siempre está listo para liberar, apoyado en la integración continua.

**¿Qué es un artefacto y por qué se genera en el pipeline?**
Es el producto empaquetado y versionado de la construcción (aquí, un jar ejecutable). Generarlo en el pipeline garantiza que se construye siempre en un entorno limpio y reproducible, a partir de código que ya pasó las pruebas y el análisis.
