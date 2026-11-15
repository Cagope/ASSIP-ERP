
package co.assip.erp.ses.formato3;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/ses/contabilidad/formato-3")
@RequiredArgsConstructor
public class Formato3Controller {

    private final Formato3Service formato3Service;

    @PreAuthorize("hasAuthority('SES_FORMATOS_GENERAR')")
    @GetMapping("/periodos")
    public java.util.List<Formato3Service.PeriodoFormato3> periodos() {
        return formato3Service.periodosDisponibles();
    }

    @PreAuthorize("hasAuthority('SES_FORMATOS_GENERAR')")
    @GetMapping("/consulta")
    public Formato3Service.ConsultaFormato3 consultar(
            @RequestParam("ano") int ano,
            @RequestParam("mes") int mes) {
        return formato3Service.consultar(ano, mes);
    }

    /**
     * Genera y descarga el archivo plano del Formato 3.
     *
     * El período debe estar cerrado en todas las agencias.
     * La información proviene de los cierres mensuales.
     * No se almacenan archivos ni se modifica la contabilidad.
     *
     * El nombre oficial del archivo se genera en el servicio
     * y se entrega mediante Content-Disposition.
     *
     * Ejemplo:
     * GET /api/v1/ses/contabilidad/formato-3?ano=2025&mes=12
     */
    @PreAuthorize("hasAuthority('SES_FORMATOS_GENERAR')")
    @GetMapping
    public ResponseEntity<byte[]> descargar(
            @RequestParam("ano") int ano,
            @RequestParam("mes") int mes
    ) {

        Formato3Service.ArchivoFormato3 archivo =
                formato3Service.generar(ano, mes);

        /*
         * Nombre oficial generado por el backend.
         *
         * Estructura:
         * SES-CODIGO_ENTIDAD-FECHA_CORTE-CODIGO_FORMATO.txt
         *
         * Ejemplo:
         * SES-3402-20251231-3.txt
         */
        ContentDisposition disposition = ContentDisposition
                .attachment()
                .filename(
                        archivo.nombreArchivo(),
                        StandardCharsets.UTF_8
                )
                .build();

        HttpHeaders headers = new HttpHeaders();

        /*
         * Nombre del archivo para su descarga.
         */
        headers.setContentDisposition(disposition);

        /*
         * Permite que Angular consulte Content-Disposition
         * cuando la solicitud se realiza mediante CORS.
         */
        headers.add(
                HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                HttpHeaders.CONTENT_DISPOSITION
        );

        /*
         * Tipo de contenido del archivo plano.
         */
        headers.setContentType(
                new MediaType(
                        "text",
                        "plain",
                        StandardCharsets.UTF_8
                )
        );

        /*
         * Tamaño exacto del archivo generado.
         */
        headers.setContentLength(
                archivo.contenido().length
        );

        /*
         * Evita almacenar archivos contables
         * en la caché del navegador.
         */
        headers.setCacheControl("no-store");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(archivo.contenido());
    }
}
