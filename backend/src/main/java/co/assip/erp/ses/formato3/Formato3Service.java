
package co.assip.erp.ses.formato3;

import co.assip.erp.ses.formatos.FormatosService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class Formato3Service {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("ddMMyyyy");

    private static final String ENCABEZADO_ENTIDAD =
            "SUPERINTENDENCIA DE LA ECONOMIA SOLIDARIA;;;;;;;;";

    private static final String ENCABEZADO_COLUMNAS =
            "CODENTIDAD;FECHACORTE;CODRENGLON;DESCRIPCIÓN;SALDO;C2;C3;C4;C5";

    private static final String SALTO_LINEA = "\r\n";

    private static final BigDecimal CERO =
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final Formato3Repository repository;
    private final NamedParameterJdbcTemplate jdbc;
    private final FormatosService formatosService;

    /**
     * Genera el Formato 3 a partir de los cierres mensuales.
     *
     * Los ajustes de excedentes o pérdidas se realizan únicamente
     * en memoria y no modifican los registros contables.
     */
    @Transactional(readOnly = true)
    public ArchivoFormato3 generar(int ano, int mes) {

        LocalDate fechaCorte = validarFecha(ano, mes);

        validarPeriodoCerrado(ano, mes);

        String codigoEntidad = consultarCodigoEntidad();

        List<Formato3Repository.SaldoContable> saldos =
                repository.consultarSaldos(ano, mes);

        Map<String, RenglonContable> renglones =
                prepararRenglones(saldos);

        aplicarResultadoEjercicio(renglones);

        List<RenglonContable> registros =
                obtenerRegistrosConSaldo(renglones);

        String contenido = construirArchivo(
                codigoEntidad,
                fechaCorte,
                registros
        );

        String codigoFormato = consultarCodigoFormato();

        String nombreArchivo = String.format(
                "SES-%s-%s-%s.txt",
                codigoEntidad,
                fechaCorte.format(DateTimeFormatter.BASIC_ISO_DATE),
                codigoFormato
        );

        return new ArchivoFormato3(
                nombreArchivo,
                contenido.getBytes(StandardCharsets.UTF_8),
                registros.size()
        );
    }

    /** Consulta el mismo conjunto de renglones utilizado para el TXT. */
    @Transactional(readOnly = true)
    public ConsultaFormato3 consultar(int ano, int mes) {
        LocalDate fechaCorte = validarFecha(ano, mes);
        validarPeriodoCerrado(ano, mes);
        Map<String, RenglonContable> renglones =
                prepararRenglones(repository.consultarSaldos(ano, mes));
        aplicarResultadoEjercicio(renglones);
        List<DetalleFormato3> detalle = obtenerRegistrosConSaldo(renglones)
                .stream()
                .map(r -> new DetalleFormato3(r.codigo, r.descripcion, r.saldo))
                .toList();
        return new ConsultaFormato3(fechaCorte, detalle.size(), detalle);
    }

    public record DetalleFormato3(String codigoRenglon, String descripcion, BigDecimal saldo) {}
    public record ConsultaFormato3(LocalDate fechaCorte, int cantidadRenglones, List<DetalleFormato3> detalle) {}

    /** Lista únicamente períodos cerrados en todas las agencias. */
    @Transactional(readOnly = true)
    public List<PeriodoFormato3> periodosDisponibles() {
        String sql = """
            SELECT p.ano, p.mes
            FROM contabilidad.periodos_contables p
            WHERE p.estado = 'CERRADO'
            GROUP BY p.ano, p.mes
            HAVING COUNT(DISTINCT p.id_agencia) = (
                SELECT COUNT(DISTINCT id_agencia)
                FROM contabilidad.catalogo_cuentas
            )
            ORDER BY p.ano DESC, p.mes DESC
            """;
        return jdbc.query(sql, (rs, row) -> new PeriodoFormato3(
                rs.getInt("ano"), rs.getInt("mes")));
    }

    public record PeriodoFormato3(int ano, int mes) {}

    /**
     * Valida el año y el mes.
     */
    private LocalDate validarFecha(int ano, int mes) {

        try {
            return LocalDate.of(ano, mes, 1)
                    .withDayOfMonth(
                            LocalDate.of(ano, mes, 1).lengthOfMonth()
                    );

        } catch (DateTimeException ex) {
            throw new IllegalArgumentException(
                    "El año o el mes seleccionado no es válido.",
                    ex
            );
        }
    }

    /**
     * Verifica que todas las agencias con catálogo contable
     * tengan el período mensual cerrado.
     */
    private void validarPeriodoCerrado(int ano, int mes) {

        String sql = """
            SELECT COUNT(*)
            FROM (
                SELECT DISTINCT c.id_agencia
                FROM contabilidad.catalogo_cuentas c
            ) agencias
            WHERE NOT EXISTS (
                SELECT 1
                FROM contabilidad.periodos_contables p
                WHERE p.id_agencia = agencias.id_agencia
                  AND p.ano = :ano
                  AND p.mes = :mes
                  AND p.estado = 'CERRADO'
            )
            """;

        MapSqlParameterSource parametros =
                new MapSqlParameterSource()
                        .addValue("ano", ano)
                        .addValue("mes", mes);

        Long pendientes = jdbc.queryForObject(
                sql,
                parametros,
                Long.class
        );

        if (pendientes == null || pendientes > 0) {
            throw new IllegalStateException(
                    "No se puede generar el Formato 3. " +
                            "El período contable debe estar cerrado " +
                            "en todas las agencias."
            );
        }
    }

    /**
     * Obtiene el código oficial de la entidad reportante.
     */
    private String consultarCodigoEntidad() {

        String sql = """
            SELECT TRIM(codigo_supersolidaria)
            FROM general.empresas
            WHERE codigo_supersolidaria IS NOT NULL
              AND TRIM(codigo_supersolidaria) <> ''
            ORDER BY documento_empresa
            LIMIT 1
            """;

        List<String> codigos = jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (rs, rowNum) -> rs.getString(1)
        );

        if (codigos.isEmpty()) {
            throw new IllegalStateException(
                    "No está configurado el código de " +
                            "Supersolidaria en general.empresas."
            );
        }

        return codigos.get(0);
    }

    /**
     * Prepara una copia de los saldos consolidados.
     *
     * Los códigos ya llegan con seis posiciones desde
     * Formato3Repository.
     */
    private Map<String, RenglonContable> prepararRenglones(
            List<Formato3Repository.SaldoContable> saldos
    ) {

        Map<String, RenglonContable> renglones =
                new TreeMap<>();

        for (Formato3Repository.SaldoContable saldo : saldos) {

            String codigo = saldo.codigoRenglon();

            BigDecimal valor = normalizar(saldo.saldo());

            RenglonContable existente = renglones.get(codigo);

            if (existente == null) {

                renglones.put(
                        codigo,
                        new RenglonContable(
                                codigo,
                                saldo.descripcion(),
                                valor
                        )
                );

            } else {

                existente.saldo = existente.saldo.add(valor);
            }
        }

        return renglones;
    }

    /**
     * Reproduce el tratamiento del resultado del ejercicio
     * utilizado por el sistema anterior en Visual Basic.
     *
     * RESULTADO = INGRESOS - GASTOS
     *
     * Para el cálculo de GASTOS se consideran las clases
     * 5, 6 y 7.
     *
     * Se utilizan los saldos originales, antes de aplicar
     * cualquier ajuste.
     */
    private void aplicarResultadoEjercicio(
            Map<String, RenglonContable> renglones
    ) {

        BigDecimal ingresos = obtenerSaldo(
                renglones,
                "400000"
        );

        BigDecimal gastos = obtenerSaldo(
                renglones,
                "500000"
        );

        BigDecimal costos = obtenerSaldo(
                renglones,
                "600000"
        );

        BigDecimal otrosCostos = obtenerSaldo(
                renglones,
                "700000"
        );

        BigDecimal resultado = ingresos
                .subtract(gastos)
                .subtract(costos)
                .subtract(otrosCostos)
                .setScale(2, RoundingMode.HALF_UP);

        /*
         * Si no existe resultado, no se generan
         * renglones adicionales.
         */
        if (resultado.signum() == 0) {
            return;
        }

        /*
         * PATRIMONIO
         *
         * La cuenta 3 incorpora el resultado
         * del ejercicio a su saldo existente.
         */
        sumarSaldoExistente(
                renglones,
                "300000",
                resultado
        );

        /*
         * RESULTADOS DEL EJERCICIO
         *
         * Estas cuentas reciben directamente
         * el resultado calculado.
         */
        asignarSaldo(
                renglones,
                "350000",
                "RESULTADOS EJERCICIO",
                resultado
        );

        asignarSaldo(
                renglones,
                "350500",
                "EXCEDENTES Y/O PERDIDAS",
                resultado
        );

        /*
         * EXCEDENTES O PERDIDAS
         *
         * El procedimiento VB registra el resultado
         * positivo en 350505.
         *
         * Cuando existe pérdida, registra su valor
         * absoluto en 350510.
         */
        if (resultado.signum() > 0) {

            asignarSaldo(
                    renglones,
                    "350505",
                    "EXCEDENTES",
                    resultado
            );

        } else {

            asignarSaldo(
                    renglones,
                    "350510",
                    "PERDIDAS",
                    resultado.abs()
            );
        }

        /*
         * GASTOS
         *
         * La cuenta 5 incorpora el resultado
         * a su saldo original.
         */
        sumarSaldoExistente(
                renglones,
                "500000",
                resultado
        );

        /*
         * EXCEDENTES Y/O PERDIDAS
         *
         * Cuenta 53 del procedimiento VB.
         */
        asignarSaldo(
                renglones,
                "530000",
                "EXCEDENTES Y/O PERDIDAS",
                resultado
        );
    }

    /**
     * Obtiene el saldo de una cuenta.
     * Si no existe, devuelve cero.
     */
    private BigDecimal obtenerSaldo(
            Map<String, RenglonContable> renglones,
            String codigo
    ) {

        RenglonContable renglon = renglones.get(codigo);

        return renglon == null
                ? CERO
                : renglon.saldo;
    }

    /**
     * Adiciona un valor a una cuenta existente.
     *
     * El procedimiento VB únicamente ajusta estas
     * cuentas cuando ya están presentes.
     */
    private void sumarSaldoExistente(
            Map<String, RenglonContable> renglones,
            String codigo,
            BigDecimal valor
    ) {

        RenglonContable renglon = renglones.get(codigo);

        if (renglon != null) {
            renglon.saldo = renglon.saldo.add(valor);
        }
    }

    /**
     * Asigna el saldo de una cuenta.
     *
     * Si no existe, crea el renglón exclusivamente
     * para la generación del archivo.
     */
    private void asignarSaldo(
            Map<String, RenglonContable> renglones,
            String codigo,
            String descripcion,
            BigDecimal valor
    ) {

        RenglonContable renglon = renglones.get(codigo);

        if (renglon == null) {

            renglones.put(
                    codigo,
                    new RenglonContable(
                            codigo,
                            descripcion,
                            normalizar(valor)
                    )
            );

        } else {

            renglon.saldo = normalizar(valor);
        }
    }

    /**
     * Excluye los saldos iguales a cero después
     * de aplicar los ajustes del resultado.
     */
    private List<RenglonContable> obtenerRegistrosConSaldo(
            Map<String, RenglonContable> renglones
    ) {

        List<RenglonContable> registros =
                new ArrayList<>();

        for (RenglonContable renglon : renglones.values()) {

            renglon.saldo = normalizar(renglon.saldo);

            if (renglon.saldo.signum() != 0) {
                registros.add(renglon);
            }
        }

        return registros;
    }

    /**
     * Construye el archivo plano con las nueve columnas
     * establecidas en la guía.
     */
    private String construirArchivo(
            String codigoEntidad,
            LocalDate fechaCorte,
            List<RenglonContable> registros
    ) {

        String fecha = fechaCorte.format(FORMATO_FECHA);

        StringBuilder archivo = new StringBuilder();

        archivo.append(ENCABEZADO_ENTIDAD)
                .append(SALTO_LINEA);

        archivo.append(ENCABEZADO_COLUMNAS)
                .append(SALTO_LINEA);

        for (RenglonContable renglon : registros) {

            archivo.append(codigoEntidad)
                    .append(';')
                    .append(fecha)
                    .append(';')
                    .append(renglon.codigo)
                    .append(';')
                    .append(limpiarDescripcion(renglon.descripcion))
                    .append(';')
                    .append(renglon.saldo.toPlainString())
                    .append(";;;;")
                    .append(SALTO_LINEA);
        }

        return archivo.toString();
    }

    /**
     * Normaliza los valores a dos decimales.
     */
    private BigDecimal normalizar(BigDecimal valor) {

        if (valor == null) {
            return CERO;
        }

        return valor.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }

    /**
     * Limpia los caracteres que pueden alterar
     * la estructura del archivo delimitado.
     */
    private String limpiarDescripcion(String descripcion) {

        if (descripcion == null) {
            return "";
        }

        return descripcion
                .replace(';', ' ')
                .replace('\r', ' ')
                .replace('\n', ' ')
                .trim();
    }

    /**
     * Representación temporal de un renglón.
     * No se almacena en la base de datos.
     */
    private static class RenglonContable {

        private final String codigo;
        private final String descripcion;
        private BigDecimal saldo;

        private RenglonContable(
                String codigo,
                String descripcion,
                BigDecimal saldo
        ) {

            this.codigo = codigo;
            this.descripcion = descripcion;
            this.saldo = saldo;
        }
    }

    /**
     * Resultado de la generación del archivo plano.
     */
    public record ArchivoFormato3(
            String nombreArchivo,
            byte[] contenido,
            int cantidadRegistros
    ) {
    }

    /**
     * Obtiene el código oficial del formato SES
     * desde el servicio centralizado.
     *
     * La configuración se consulta en
     * supersolidaria.formatos.
     *
     * No modifica información de la base de datos.
     */
    private String consultarCodigoFormato() {

        return formatosService.obtenerCodigoFormato(
                "CONTABILIDAD",
                "SALDOS CONTABLES"
        );
    }
}
