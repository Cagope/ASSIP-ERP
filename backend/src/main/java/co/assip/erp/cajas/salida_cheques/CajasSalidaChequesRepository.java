
package co.assip.erp.cajas.salida_cheques;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CajasSalidaChequesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public record CajaTraslado(
            Long idCaja,
            Integer idAgencia,
            String tipoCaja,
            Integer idCatalogoCuentaCaja,
            String codigoCuentaCaja,
            String nombreCuentaCaja
    ) {}

    public record ChequeTraslado(
            Long idChequeRecibido,
            Long idCaja,
            Long idMovimientoOriginal,
            String numeroCheque,
            BigDecimal valorCheque,
            Integer codigoBanco
    ) {}

    // =========================================================
    // CAJA ORIGEN
    // =========================================================

    public CajaTraslado obtenerCaja(Long idCaja) {

        String sql = """
            SELECT
                c.id_caja,
                c.id_agencia,
                c.tipo_caja,
                c.id_catalogo_cuenta_caja,
                cc.codigo_cuenta AS codigo_cuenta_caja,
                cc.nombre_cuenta AS nombre_cuenta_caja
            FROM cajas.cajas c
            LEFT JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta = c.id_catalogo_cuenta_caja
            WHERE c.id_caja = :idCaja
              AND c.activa = TRUE
              AND c.permite_cheques = TRUE
            """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("idCaja", idCaja),
                rs -> rs.next()
                        ? new CajaTraslado(
                        rs.getLong("id_caja"),
                        rs.getInt("id_agencia"),
                        rs.getString("tipo_caja"),
                        rs.getObject(
                                "id_catalogo_cuenta_caja",
                                Integer.class
                        ),
                        rs.getString("codigo_cuenta_caja"),
                        rs.getString("nombre_cuenta_caja")
                )
                        : null
        );
    }

    // =========================================================
    // CAJA CONTROL DE LA AGENCIA
    // =========================================================

    public CajaTraslado obtenerCajaControl(Integer idAgencia) {

        String sql = """
            SELECT
                c.id_caja,
                c.id_agencia,
                c.tipo_caja,
                c.id_catalogo_cuenta_caja,
                cc.codigo_cuenta AS codigo_cuenta_caja,
                cc.nombre_cuenta AS nombre_cuenta_caja
            FROM cajas.cajas c
            LEFT JOIN contabilidad.catalogo_cuentas cc
                ON cc.id_catalogo_cuenta = c.id_catalogo_cuenta_caja
            WHERE c.id_agencia = :idAgencia
              AND c.tipo_caja = 'CONTROL'
              AND c.activa = TRUE
              AND c.permite_cheques = TRUE
            ORDER BY c.id_caja
            """;

        List<CajaTraslado> cajas = jdbc.query(
                sql,
                new MapSqlParameterSource("idAgencia", idAgencia),
                (rs, i) -> new CajaTraslado(
                        rs.getLong("id_caja"),
                        rs.getInt("id_agencia"),
                        rs.getString("tipo_caja"),
                        rs.getObject(
                                "id_catalogo_cuenta_caja",
                                Integer.class
                        ),
                        rs.getString("codigo_cuenta_caja"),
                        rs.getString("nombre_cuenta_caja")
                )
        );

        if (cajas.size() != 1) {
            throw new IllegalStateException(
                    "La agencia debe tener exactamente una caja CONTROL "
                            + "activa que admita cheques"
            );
        }

        return cajas.get(0);
    }

    // =========================================================
    // BLOQUEO DE CHEQUES PARA TRASLADO
    // =========================================================

    /**
     * Bloquea los cheques seleccionados para evitar traslados simultáneos.
     *
     * Debe ejecutarse dentro de una transacción activa.
     */
    public List<ChequeTraslado> bloquearCheques(
            Long idCajaOrigen,
            List<Long> idsCheques
    ) {

        if (idsCheques == null || idsCheques.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un cheque"
            );
        }

        if (idsCheques.stream().anyMatch(id -> id == null)
                || idsCheques.stream().distinct().count()
                != idsCheques.size()) {

            throw new IllegalArgumentException(
                    "La selección contiene identificadores nulos o repetidos"
            );
        }

        String sql = """
            SELECT
                id_cheque_recibido,
                id_caja,
                id_movimiento_caja,
                numero_cheque,
                valor_cheque,
                codigo_banco
            FROM cajas.cheques_recibidos
            WHERE id_caja = :idCajaOrigen
              AND estado_cheque = 'RECIBIDO'
              AND id_cheque_recibido IN (:idsCheques)
            ORDER BY id_cheque_recibido
            FOR UPDATE
            """;

        List<ChequeTraslado> cheques = jdbc.query(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCajaOrigen", idCajaOrigen)
                        .addValue("idsCheques", idsCheques),
                (rs, i) -> new ChequeTraslado(
                        rs.getLong("id_cheque_recibido"),
                        rs.getLong("id_caja"),
                        rs.getLong("id_movimiento_caja"),
                        rs.getString("numero_cheque"),
                        rs.getBigDecimal("valor_cheque"),
                        rs.getInt("codigo_banco")
                )
        );

        if (cheques.size() != idsCheques.size()) {
            throw new IllegalStateException(
                    "Uno o más cheques no están disponibles en la caja origen"
            );
        }

        return cheques;
    }

    // =========================================================
    // TRASLADO DE CUSTODIA
    // =========================================================

    /**
     * Actualiza la caja que tiene actualmente la custodia del cheque.
     *
     * Conserva:
     * - id_movimiento_caja original
     * - datos del recaudo
     * - estado RECIBIDO
     *
     * La trazabilidad del traslado queda registrada
     * mediante los movimientos de caja.
     */
    public void trasladarCustodia(
            Long idCheque,
            Long idCajaOrigen,
            Long idCajaControl,
            LocalDate fecha,
            Integer idUsuario
    ) {

        String sql = """
            UPDATE cajas.cheques_recibidos
            SET
                id_caja = :idCajaControl,
                fecha_estado = :fecha,
                fk_seguridad_edicion = :idUsuario,
                fecha_edicion = CURRENT_TIMESTAMP
            WHERE id_cheque_recibido = :idCheque
              AND id_caja = :idCajaOrigen
              AND estado_cheque = 'RECIBIDO'
            """;

        int actualizados = jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("idCheque", idCheque)
                        .addValue("idCajaOrigen", idCajaOrigen)
                        .addValue("idCajaControl", idCajaControl)
                        .addValue("fecha", fecha)
                        .addValue("idUsuario", idUsuario)
        );

        if (actualizados != 1) {
            throw new IllegalStateException(
                    "No fue posible transferir la custodia del cheque "
                            + idCheque
            );
        }
    }

    // =========================================================
    // CHEQUES DISPONIBLES PARA ENTREGA
    //
    // La caja origen debe obtenerse previamente desde
    // la vinculación activa del usuario autenticado.
    //
    // No bloquea registros: es una consulta de lectura.
    // =========================================================

    public List<ChequeTraslado> listarDisponibles(
            Long idCajaOrigen
    ) {

        if (idCajaOrigen == null) {
            throw new IllegalArgumentException(
                    "La caja origen es obligatoria"
            );
        }

        String sql = """
            SELECT
                id_cheque_recibido,
                id_caja,
                id_movimiento_caja,
                numero_cheque,
                valor_cheque,
                codigo_banco
            FROM cajas.cheques_recibidos
            WHERE id_caja = :idCajaOrigen
              AND estado_cheque = 'RECIBIDO'
            ORDER BY id_cheque_recibido
            """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(
                        "idCajaOrigen",
                        idCajaOrigen
                ),
                (rs, i) -> new ChequeTraslado(
                        rs.getLong("id_cheque_recibido"),
                        rs.getLong("id_caja"),
                        rs.getLong("id_movimiento_caja"),
                        rs.getString("numero_cheque"),
                        rs.getBigDecimal("valor_cheque"),
                        rs.getInt("codigo_banco")
                )
        );
    }

    // =========================================================
// TERCERO CONTABLE DEL CAJERO AUTENTICADO
// =========================================================

    public Integer obtenerIdDatosPersonalCajero(Integer idUsuario) {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El usuario autenticado es obligatorio."
            );
        }

        String sql = """
            SELECT id_datos_personal
            FROM seguridad.usuarios
            WHERE id_usuario = :idUsuario
              AND activo = TRUE
            """;

        Integer idDatosPersonal = jdbc.query(
                sql,
                new MapSqlParameterSource("idUsuario", idUsuario),
                rs -> rs.next()
                        ? rs.getObject("id_datos_personal", Integer.class)
                        : null
        );

        if (idDatosPersonal == null) {
            throw new IllegalStateException(
                    "El cajero autenticado no tiene un tercero contable "
                            + "asociado o su usuario está inactivo."
            );
        }

        return idDatosPersonal;
    }

}
