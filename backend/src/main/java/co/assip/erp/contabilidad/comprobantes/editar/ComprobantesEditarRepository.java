package co.assip.erp.contabilidad.comprobantes.editar;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Acceso transaccional para editar comprobantes manuales existentes, sin crear consecutivos. */
@Repository
@RequiredArgsConstructor
public class ComprobantesEditarRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public record ComprobanteBloqueado(String origenTipo, String moduloOrigen,
                                       String procesoOrigen, String estadoOrigen) {}

    public record MovimientoExistente(Integer idAuxiliarContable, LocalDate fechaAuxiliar,
                                      String estadoMovimiento) {}

    /** Bloquea el origen para serializar las ediciones del mismo comprobante. */
    public Optional<ComprobanteBloqueado> bloquearComprobante(
            Integer agencia, String tipo, String numero) {
        String sql = """
                SELECT origen_tipo, modulo_origen, proceso_origen, estado_origen
                  FROM contabilidad.origen_comprobantes
                 WHERE id_agencia = :agencia
                   AND TRIM(tipo_comprobante) = :tipo
                   AND TRIM(numero_comprobante) = :numero
                 FOR UPDATE
                """;
        List<ComprobanteBloqueado> filas = jdbc.query(sql, clave(agencia, tipo, numero),
                (rs, i) -> new ComprobanteBloqueado(rs.getString("origen_tipo"),
                        rs.getString("modulo_origen"), rs.getString("proceso_origen"),
                        rs.getString("estado_origen")));
        return filas.stream().findFirst();
    }

    /** Bloquea el período existente; nunca lo crea ni cambia su estado. */
    public Optional<String> bloquearEstadoPeriodo(Integer agencia, LocalDate fecha) {
        String sql = """
                SELECT TRIM(estado) AS estado
                  FROM contabilidad.periodos_contables
                 WHERE id_agencia = :agencia
                   AND ano = :ano AND mes = :mes
                 FOR UPDATE
                """;
        var params = new MapSqlParameterSource("agencia", agencia)
                .addValue("ano", fecha.getYear()).addValue("mes", fecha.getMonthValue());
        List<String> estados = jdbc.query(sql, params, (rs, i) -> rs.getString("estado"));
        return estados.stream().findFirst();
    }

    /** Recupera y bloquea las líneas activas para comprobar su identidad y fecha. */
    public List<MovimientoExistente> bloquearMovimientos(
            Integer agencia, String tipo, String numero) {
        String sql = """
                SELECT id_auxiliar_contable, fecha_auxiliar,
                       TRIM(estado_movimiento) AS estado_movimiento
                  FROM contabilidad.auxiliares_contables
                 WHERE id_agencia = :agencia
                   AND TRIM(tipo_comprobante) = :tipo
                   AND TRIM(numero_comprobante) = :numero
                 ORDER BY id_auxiliar_contable
                 FOR UPDATE
                """;
        return jdbc.query(sql, clave(agencia, tipo, numero),
                (rs, i) -> new MovimientoExistente(rs.getInt("id_auxiliar_contable"),
                        rs.getObject("fecha_auxiliar", LocalDate.class),
                        rs.getString("estado_movimiento")));
    }

    /** Modifica el concepto general sin alterar la identidad del comprobante. */
    public int actualizarConcepto(Integer agencia, String tipo, String numero,
                                  String concepto, Integer usuario) {
        String sql = """
                UPDATE contabilidad.conceptos_contables
                   SET concepto_comprobante = :concepto,
                       fk_seguridad_edicion = :usuario,
                       fecha_edicion = CURRENT_TIMESTAMP
                 WHERE id_agencia = :agencia
                   AND TRIM(tipo_comprobante) = :tipo
                   AND TRIM(numero_comprobante) = :numero
                """;
        return jdbc.update(sql, clave(agencia, tipo, numero)
                .addValue("concepto", concepto).addValue("usuario", usuario));
    }

    /** Actualiza una línea existente y sus campos de auditoría, sin cambiar fecha ni clave. */
    public int actualizarMovimiento(Integer agencia, String tipo, String numero,
                                    Integer idAuxiliar, Integer cuenta, Integer tercero,
                                    String detalle, BigDecimal debito, BigDecimal credito,
                                    BigDecimal base, Integer usuario) {
        String sql = """
                UPDATE contabilidad.auxiliares_contables
                   SET id_catalogo_cuenta = :cuenta,
                       id_datos_personal = :tercero,
                       detalle_movimiento = :detalle,
                       valor_debito = :debito,
                       valor_credito = :credito,
                       valor_base = :base,
                       fk_seguridad_edicion = :usuario,
                       fecha_edicion = CURRENT_TIMESTAMP
                 WHERE id_auxiliar_contable = :auxiliar
                   AND id_agencia = :agencia
                   AND TRIM(tipo_comprobante) = :tipo
                   AND TRIM(numero_comprobante) = :numero
                   AND TRIM(estado_movimiento) = 'A'
                """;
        return jdbc.update(sql, clave(agencia, tipo, numero)
                .addValue("auxiliar", idAuxiliar).addValue("cuenta", cuenta)
                .addValue("tercero", tercero).addValue("detalle", detalle)
                .addValue("debito", debito).addValue("credito", credito)
                .addValue("base", base).addValue("usuario", usuario));
    }

    /** Recupera los identificadores de control asociados a una línea. */
    public List<Integer> listarIdsControles(Integer idAuxiliar) {
        String sql = """
                SELECT id_controles_auxiliares
                  FROM contabilidad.controles_auxiliares
                 WHERE id_auxiliar_contable = :auxiliar
                 ORDER BY id_controles_auxiliares
                """;
        return jdbc.query(sql, new MapSqlParameterSource("auxiliar", idAuxiliar),
                (rs, i) -> rs.getInt("id_controles_auxiliares"));
    }

    /** Actualiza el control documental existente, conservando su identificador. */
    public int actualizarControl(Integer idControl, Integer idAuxiliar,
                                 Integer tipoControl, String documento, Integer usuario) {
        String sql = """
                UPDATE contabilidad.controles_auxiliares
                   SET id_tipos_control = :tipoControl,
                       numero_documento = :documento,
                       fk_seguridad_edicion = :usuario,
                       fecha_edicion = CURRENT_TIMESTAMP
                 WHERE id_controles_auxiliares = :control
                   AND id_auxiliar_contable = :auxiliar
                """;
        return jdbc.update(sql, new MapSqlParameterSource("control", idControl)
                .addValue("auxiliar", idAuxiliar).addValue("tipoControl", tipoControl)
                .addValue("documento", documento).addValue("usuario", usuario));
    }

    /** Inserta un control nuevo cuando la cuenta editada lo requiere. */
    public int insertarControl(Integer idAuxiliar, Integer tipoControl,
                               String documento, Integer usuario) {
        String sql = """
                INSERT INTO contabilidad.controles_auxiliares
                    (id_auxiliar_contable, id_tipos_control, numero_documento,
                     fk_seguridad_creacion, fk_seguridad_edicion)
                VALUES (:auxiliar, :tipoControl, :documento, :usuario, :usuario)
                """;
        return jdbc.update(sql, new MapSqlParameterSource("auxiliar", idAuxiliar)
                .addValue("tipoControl", tipoControl).addValue("documento", documento)
                .addValue("usuario", usuario));
    }

    /** Elimina un control que deja de corresponder a la cuenta contable. */
    public int eliminarControl(Integer idControl, Integer idAuxiliar) {
        String sql = """
                DELETE FROM contabilidad.controles_auxiliares
                 WHERE id_controles_auxiliares = :control
                   AND id_auxiliar_contable = :auxiliar
                """;
        return jdbc.update(sql, new MapSqlParameterSource("control", idControl)
                .addValue("auxiliar", idAuxiliar));
    }

    /** Construye la clave de un comprobante ya existente. */
    private MapSqlParameterSource clave(Integer agencia, String tipo, String numero) {
        return new MapSqlParameterSource("agencia", agencia)
                .addValue("tipo", tipo.trim().toUpperCase())
                .addValue("numero", numero.trim());
    }
}
