package com.facturacion.aceptacion;

import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ejecutor (runner) de las pruebas de aceptación escritas con Karate.
 *
 * Karate lee los archivos .feature de la carpeta src/test/resources/karate,
 * que describen el comportamiento del negocio en lenguaje Gherkin
 * (Given / When / Then), y los ejecuta contra las clases reales del sistema.
 *
 * La etiqueta "aceptacion" permite que Gradle separe estas pruebas de las
 * unitarias (ver las tareas "test" y "pruebasAceptacion" en build.gradle).
 */
@Tag("aceptacion")
class AceptacionKarateTest {

    @Test
    void debePasarTodosLosEscenariosDeAceptacion() {
        Results resultados = Runner.path("classpath:karate")
                .outputHtmlReport(true)
                .reportDir("build/karate-reports")
                .parallel(1);

        // Si algún escenario falla, se muestra el detalle del error
        assertEquals(0, resultados.getFailCount(), resultados.getErrorMessages());
    }
}
