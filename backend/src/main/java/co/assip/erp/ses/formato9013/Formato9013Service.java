
package co.assip.erp.ses.formato9013;

import co.assip.erp.ses.formato9013.dto.Formato9013DTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class Formato9013Service {

    private final Formato9013Repository repository;

    /**
     * Consulta el Formato 9013 - Aportes Sociales.
     *
     * Reglas:
     * - Fecha de corte obligatoria.
     * - La fecha debe corresponder al último día del mes.
     * - No permite fechas futuras.
     * - Requiere un cierre mensual definitivo de Depósitos.
     * - Consolida las cuentas de aportes de ambas agencias.
     * - No modifica información financiera.
     */
    @Transactional(readOnly = true)
    public List<Formato9013DTO> consultar(String fechaCorte) {

        LocalDate fecha = validarFechaCorte(fechaCorte);

        if (!repository.existeCierreDefinitivo(fecha.toString())) {
            throw new IllegalStateException(
                    "No existe un cierre mensual definitivo " +
                            "de Depósitos para la fecha " + fecha + "."
            );
        }

        List<Map<String, Object>> registros =
                repository.consultar(fecha.toString());

        List<Formato9013DTO> resultado =
                new ArrayList<>(registros.size());

        for (Map<String, Object> registro : registros) {
            resultado.add(convertirDTO(registro));
        }

        return resultado;
    }

    /**
     * Valida la fecha de corte solicitada.
     */
    private LocalDate validarFechaCorte(String fechaCorte) {

        if (fechaCorte == null || fechaCorte.isBlank()) {
            throw new IllegalArgumentException(
                    "La fecha de corte es obligatoria."
            );
        }

        LocalDate fecha;

        try {
            fecha = LocalDate.parse(fechaCorte);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "La fecha de corte debe tener el formato yyyy-MM-dd.",
                    ex
            );
        }

        if (fecha.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "No se permite consultar una fecha de corte futura."
            );
        }

        LocalDate ultimoDiaMes =
                YearMonth.from(fecha).atEndOfMonth();

        if (!fecha.equals(ultimoDiaMes)) {
            throw new IllegalArgumentException(
                    "La fecha de corte debe corresponder " +
                            "al último día del mes."
            );
        }

        return fecha;
    }

    /**
     * Convierte cada registro del repositorio
     * al DTO del Formato 9013.
     *
     * Los importes se manejan con BigDecimal
     * para conservar la precisión financiera.
     *
     * Los conceptos todavía no identificados
     * permanecen en NULL.
     */
    private Formato9013DTO convertirDTO(
            Map<String, Object> registro
    ) {

        Formato9013DTO dto = new Formato9013DTO();

        // ============================================
        // IDENTIFICACIÓN DEL REPORTE
        // ============================================

        dto.setCodigoEntidad(
                obtenerTexto(registro, "codigoEntidad")
        );

        dto.setFechaCorte(
                obtenerTexto(registro, "fechaCorte")
        );

        dto.setConsecutivo(
                obtenerEntero(registro, "consecutivo")
        );

        // ============================================
        // IDENTIFICACIÓN DEL ASOCIADO
        // ============================================

        dto.setTipoIdentificacion(
                obtenerTexto(registro, "tipoIdentificacion")
        );

        dto.setNumeroIdentificacion(
                obtenerTexto(registro, "numeroIdentificacion")
        );

        dto.setPrimerApellido(
                obtenerTexto(registro, "primerApellido")
        );

        dto.setSegundoApellido(
                obtenerTexto(registro, "segundoApellido")
        );

        dto.setNombres(
                obtenerTexto(registro, "nombres")
        );

        // ============================================
        // INFORMACIÓN DE APORTES SOCIALES
        // ============================================

        dto.setSaldoAportes(
                obtenerDecimal(registro, "saldoAportes")
        );

        dto.setValorAporteMensual(
                obtenerDecimal(registro, "valorAporteMensual")
        );

        dto.setAportesOrdinarios(
                obtenerDecimal(registro, "aportesOrdinarios")
        );

        dto.setAportesExtraordinarios(
                obtenerDecimal(registro, "aportesExtraordinarios")
        );

        dto.setValorRevalorizacion(
                obtenerDecimal(registro, "valorRevalorizacion")
        );

        dto.setMontoPromedio(
                obtenerDecimal(registro, "montoPromedio")
        );

        dto.setUltimaFecha(
                obtenerTexto(registro, "ultimaFecha")
        );

        dto.setContribuciones(
                obtenerDecimal(registro, "contribuciones")
        );

        return dto;
    }

    /**
     * Obtiene un texto sin convertir NULL
     * en la cadena literal "null".
     */
    private String obtenerTexto(
            Map<String, Object> registro,
            String campo
    ) {

        Object valor = registro.get(campo);

        return valor == null
                ? null
                : valor.toString().trim();
    }

    /**
     * Obtiene un entero para el consecutivo.
     */
    private Long obtenerEntero(
            Map<String, Object> registro,
            String campo
    ) {

        Object valor = registro.get(campo);

        if (valor == null) {
            return null;
        }

        if (valor instanceof Number numero) {
            return numero.longValue();
        }

        return Long.valueOf(valor.toString());
    }

    /**
     * Obtiene un valor monetario con precisión decimal.
     *
     * No convierte valores desconocidos en cero.
     */
    private BigDecimal obtenerDecimal(
            Map<String, Object> registro,
            String campo
    ) {

        Object valor = registro.get(campo);

        if (valor == null) {
            return null;
        }

        if (valor instanceof BigDecimal decimal) {
            return decimal;
        }

        if (valor instanceof Number numero) {
            return new BigDecimal(numero.toString());
        }

        return new BigDecimal(valor.toString());
    }
}
