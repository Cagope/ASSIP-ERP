
package co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido;

import co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido.dto.LibroDiarioResumidoFiltroDTO;
import co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido.dto.LibroDiarioResumidoFilaDTO;
import co.assip.erp.contabilidad.informes.especiales.libro_diario_resumido.dto.LibroDiarioResumidoResponseDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Servicio de consulta del Libro diario resumido.
 *
 * Valida los filtros, selecciona la modalidad de agrupación
 * y construye los totales generales del informe.
 */
@Service
public class LibroDiarioResumidoService {

    private static final String MODALIDAD_CUENTA = "CUENTA";
    private static final String MODALIDAD_TIPO_COMPROBANTE = "TIPO_COMPROBANTE";

    private final LibroDiarioResumidoRepository repository;

    public LibroDiarioResumidoService(
            LibroDiarioResumidoRepository repository
    ) {
        this.repository = repository;
    }

    /**
     * Consulta el libro diario resumido para una agencia y período.
     * Valida los filtros y calcula los totales de los movimientos activos.
     *
     * La autorización del permiso y la agencia debe realizarse
     * antes de invocar este método desde el controlador.
     */
    @Transactional(readOnly = true)
    public LibroDiarioResumidoResponseDTO consultar(
            LibroDiarioResumidoFiltroDTO filtro
    ) {
        validarFiltro(filtro);

        String modalidad = filtro.modalidad().trim().toUpperCase(Locale.ROOT);

        List<LibroDiarioResumidoFilaDTO> filas;

        if (MODALIDAD_CUENTA.equals(modalidad)) {
            filas = repository.consultarPorCuenta(
                    filtro.idAgencia(),
                    filtro.fechaInicial(),
                    filtro.fechaFinal()
            );
        } else {
            filas = repository.consultarPorTipoComprobante(
                    filtro.idAgencia(),
                    filtro.fechaInicial(),
                    filtro.fechaFinal()
            );
        }

        BigDecimal totalDebitos = BigDecimal.ZERO;
        BigDecimal totalCreditos = BigDecimal.ZERO;

        for (LibroDiarioResumidoFilaDTO fila : filas) {
            totalDebitos = totalDebitos.add(
                    fila.totalDebitos() != null
                            ? fila.totalDebitos()
                            : BigDecimal.ZERO
            );

            totalCreditos = totalCreditos.add(
                    fila.totalCreditos() != null
                            ? fila.totalCreditos()
                            : BigDecimal.ZERO
            );
        }

        BigDecimal diferencia = totalDebitos.subtract(totalCreditos);

        return new LibroDiarioResumidoResponseDTO(
                filtro.idAgencia(),
                filtro.fechaInicial(),
                filtro.fechaFinal(),
                modalidad,
                filas,
                totalDebitos,
                totalCreditos,
                diferencia
        );
    }

    /**
     * Verifica que los filtros sean completos y coherentes.
     * Impide consultas con fechas invertidas o modalidades desconocidas.
     */
    private void validarFiltro(
            LibroDiarioResumidoFiltroDTO filtro
    ) {
        if (filtro == null) {
            throw new IllegalArgumentException(
                    "Debe proporcionar los filtros del informe."
            );
        }

        if (filtro.idAgencia() == null || filtro.idAgencia() <= 0) {
            throw new IllegalArgumentException(
                    "Debe seleccionar una agencia válida."
            );
        }

        LocalDate fechaInicial = filtro.fechaInicial();
        LocalDate fechaFinal = filtro.fechaFinal();

        if (fechaInicial == null || fechaFinal == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar las fechas inicial y final."
            );
        }

        if (fechaInicial.isAfter(fechaFinal)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }

        if (filtro.modalidad() == null || filtro.modalidad().isBlank()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar la modalidad del informe."
            );
        }

        String modalidad = filtro.modalidad()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!MODALIDAD_CUENTA.equals(modalidad)
                && !MODALIDAD_TIPO_COMPROBANTE.equals(modalidad)) {
            throw new IllegalArgumentException(
                    "La modalidad seleccionada no es válida."
            );
        }
    }
}
