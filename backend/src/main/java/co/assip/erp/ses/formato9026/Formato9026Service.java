package co.assip.erp.ses.formato9026;

import co.assip.erp.ses.formato9026.dto.Formato9026DTO;
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
public class Formato9026Service {

    private final Formato9026Repository repository;

    /**
     * Consulta el Formato SES 9026 - Ahorros.
     *
     * Valida la fecha de corte y la existencia de los cierres
     * mensuales de Depósitos y CDAT.
     *
     * Consolida los registros históricos de ambos módulos
     * sin modificar información financiera.
     */
    @Transactional(readOnly = true)
    public List<Formato9026DTO> consultar(String fechaCorte) {

        LocalDate fecha = validarFechaCorte(fechaCorte);

        String fechaConsulta = fecha.toString();

        if (!repository.existeCierreDepositos(fechaConsulta)) {
            throw new IllegalStateException(
                    "No existe un cierre mensual definitivo " +
                            "de Depósitos para la fecha " + fecha + "."
            );
        }

        if (!repository.existeCierreCdat(fechaConsulta)) {
            throw new IllegalStateException(
                    "No existe un cierre mensual de CDAT " +
                            "en estado GENERADO para la fecha " + fecha + "."
            );
        }

        List<Map<String, Object>> registros =
                repository.consultar(fechaConsulta);

        List<Formato9026DTO> resultado =
                new ArrayList<>(registros.size());

        for (Map<String, Object> registro : registros) {
            resultado.add(convertirDTO(registro));
        }

        return resultado;
    }

    /**
     * Valida la fecha de corte.
     *
     * Reglas:
     * - Es obligatoria.
     * - Debe tener formato yyyy-MM-dd.
     * - No puede ser futura.
     * - Debe corresponder al último día del mes.
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
     * Convierte una fila del repositorio al DTO del Formato 9026.
     *
     * Conserva los 26 campos oficiales y los tres campos
     * complementarios utilizados por Angular.
     *
     * Los valores numéricos ausentes se presentan como cero.
     * Los códigos regulatorios pendientes de validación
     * permanecen nulos.
     */
    private Formato9026DTO convertirDTO(
            Map<String, Object> registro
    ) {

        Formato9026DTO dto = new Formato9026DTO();

        // Identificación del reporte.

        dto.setCodigoEntidad(
                obtenerTexto(registro, "codigoEntidad")
        );

        dto.setFechaCorte(
                obtenerTexto(registro, "fechaCorte")
        );

        dto.setConsecutivo(
                obtenerLong(registro, "consecutivo")
        );

        // Identificación del titular.

        dto.setTipoIdentificacion(
                obtenerTexto(registro, "tipoIdentificacion")
        );

        dto.setNit(
                obtenerTexto(registro, "nit")
        );

        // Clasificación y condiciones del depósito.

        dto.setCodigoContable(
                obtenerTexto(registro, "codigoContable")
        );

        dto.setNombreDeposito(
                obtenerTexto(registro, "nombreDeposito")
        );

        dto.setTipoAhorro(
                obtenerInteger(registro, "tipoAhorro")
        );

        dto.setAmortizacion(
                obtenerTexto(registro, "amortizacion")
        );

        dto.setFechaApertura(
                obtenerTexto(registro, "fechaApertura")
        );

        dto.setPlazo(
                obtenerInteger(registro, "plazo")
        );

        dto.setFechaVencimiento(
                obtenerTexto(registro, "fechaVencimiento")
        );

        dto.setModalidad(
                obtenerTexto(registro, "modalidad")
        );

        dto.setTasaInteresNominal(
                obtenerDecimal(registro, "tasaInteresNominal")
        );

        dto.setTasaInteresEfectiva(
                obtenerDecimal(registro, "tasaInteresEfectiva")
        );

        dto.setInteresesCausados(
                obtenerDecimal(registro, "interesesCausados")
        );

        dto.setSaldo(
                obtenerDecimal(registro, "saldo")
        );

        dto.setDepositoInicial(
                obtenerDecimal(registro, "depositoInicial")
        );

        dto.setNumeroCuenta(
                obtenerTexto(registro, "numeroCuenta")
        );

        // GMF y características de la cuenta.

        dto.setExentaGmf(
                obtenerTexto(registro, "exentaGmf")
        );

        dto.setFechaAceptacionGmf(
                obtenerTexto(registro, "fechaAceptacionGmf")
        );

        dto.setEstado(
                obtenerTexto(registro, "estado")
        );

        dto.setCtaBajoMonto(
                obtenerTexto(registro, "ctaBajoMonto")
        );

        dto.setCotitulares(
                obtenerInteger(registro, "cotitulares")
        );

        dto.setConjuntaColectivo(
                obtenerTexto(registro, "conjuntaColectivo")
        );

        dto.setPaso(
                obtenerTexto(registro, "paso")
        );

        // Información complementaria para Angular.

        dto.setModuloOrigen(
                obtenerTexto(registro, "moduloOrigen")
        );

        dto.setIdAgencia(
                obtenerInteger(registro, "idAgencia")
        );

        dto.setNombreTitular(
                obtenerTexto(registro, "nombreTitular")
        );

        return dto;
    }

    /**
     * Obtiene un campo de texto.
     *
     * Conserva los códigos y números de cuenta como cadenas
     * para no perder ceros iniciales.
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
     * Convierte un campo numérico a Long.
     *
     * Se utiliza para el consecutivo del informe.
     */
    private Long obtenerLong(
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
     * Convierte un campo numérico a Integer.
     *
     * Se utiliza para tipo de ahorro, plazo,
     * cotitulares e identificador interno de agencia.
     */
    private Integer obtenerInteger(
            Map<String, Object> registro,
            String campo
    ) {

        Object valor = registro.get(campo);

        if (valor == null) {
            return null;
        }

        if (valor instanceof Number numero) {
            return numero.intValue();
        }

        return Integer.valueOf(valor.toString());
    }

    /**
     * Convierte un importe o tasa a BigDecimal.
     *
     * Conserva la precisión financiera y devuelve cero
     * cuando el repositorio no proporciona un valor.
     */
    private BigDecimal obtenerDecimal(
            Map<String, Object> registro,
            String campo
    ) {

        Object valor = registro.get(campo);

        if (valor == null) {
            return BigDecimal.ZERO;
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