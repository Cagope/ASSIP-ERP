package co.assip.erp.cajas.operaciones_pendientes;

import co.assip.erp.cajas.operaciones_pendientes.dto.CajaOperacionPendienteDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CajasOperacionesPendientesRepository {

    private final NamedParameterJdbcTemplate jdbc;


    // =========================================================
    // LISTAR PENDIENTES
    // =========================================================

    public List<CajaOperacionPendienteDTO> listarPendientes(
            Long idCaja,
            LocalDate fechaContable
    ) {

        String sql = """
                SELECT
                    m.id_movimiento_caja,
                    m.id_provision,
                    m.id_caja,
                    m.fecha_contable,
                    m.hora_movimiento,

                    m.id_operacion_caja,
                    o.codigo_operacion,
                    o.nombre_operacion,

                    m.naturaleza,
                    m.medio_pago,
                    m.valor,
                    m.concepto,
                    m.estado,

                    m.modulo_origen,
                    m.proceso_origen,
                    m.tabla_origen,
                    m.id_origen,
                    m.codigo_referencia,

                    m.tipo_comprobante,
                    m.numero_comprobante,

                    dp.id_datos_personal,
                    dp.tipo_documento,
                    dp.documento,

                    TRIM(
                        CONCAT_WS(
                            ' ',
                            NULLIF(TRIM(dp.nombres), ''),
                            NULLIF(TRIM(dp.primer_apellido), ''),
                            NULLIF(TRIM(dp.segundo_apellido), '')
                        )
                    ) AS nombre_completo

                FROM cajas.movimientos_caja m

                INNER JOIN cajas.operaciones_caja o
                    ON o.id_operacion_caja = m.id_operacion_caja

                LEFT JOIN cdat.cuentas_cdats c
                    ON m.tabla_origen = 'cdat.cuentas_cdats'
                   AND c.id_cuenta_cdat = m.id_origen

                LEFT JOIN depositos.cuentas_ahorro ca
                    ON m.tabla_origen = 'depositos.cuentas_ahorro'
                   AND ca.id_cuenta_ahorro = m.id_origen
                   
                LEFT JOIN cartera.carteras_creditos cc
                    ON m.tabla_origen = 'cartera.carteras_creditos'
                   AND cc.id_cartera_credito = m.id_origen

                LEFT JOIN hoja_vida.datos_personales dp
                     ON dp.id_datos_personal = COALESCE(
                         CASE
                             WHEN m.tabla_origen = 'hoja_vida.datos_personales'
                             THEN m.id_origen
                         END,
                         c.id_datos_personal,
                         ca.id_datos_personal,
                         cc.id_datos_personal
                     )

                WHERE m.id_caja = :idCaja
                  AND m.fecha_contable = :fechaContable
                  AND m.estado = 'PENDIENTE'

                ORDER BY
                    m.hora_movimiento,
                    m.id_movimiento_caja
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue("idCaja", idCaja)
                        .addValue("fechaContable", fechaContable);

        return jdbc.query(
                sql,
                params,
                (rs, rowNum) -> {

                    CajaOperacionPendienteDTO dto =
                            new CajaOperacionPendienteDTO();

                    dto.setIdMovimientoCaja(
                            rs.getLong("id_movimiento_caja")
                    );

                    dto.setIdProvision(
                            rs.getLong("id_provision")
                    );

                    dto.setIdCaja(
                            rs.getLong("id_caja")
                    );

                    dto.setFechaContable(
                            rs.getObject(
                                    "fecha_contable",
                                    LocalDate.class
                            )
                    );

                    dto.setHoraMovimiento(
                            rs.getTime("hora_movimiento") != null
                                    ? rs.getTime("hora_movimiento")
                                    .toLocalTime()
                                    : null
                    );

                    dto.setIdOperacionCaja(
                            rs.getLong("id_operacion_caja")
                    );

                    dto.setCodigoOperacion(
                            rs.getString("codigo_operacion")
                    );

                    dto.setNombreOperacion(
                            rs.getString("nombre_operacion")
                    );

                    dto.setNaturaleza(
                            rs.getString("naturaleza")
                    );

                    dto.setMedioPago(
                            rs.getString("medio_pago")
                    );

                    dto.setValor(
                            rs.getBigDecimal("valor")
                    );

                    dto.setConcepto(
                            rs.getString("concepto")
                    );

                    dto.setEstado(
                            rs.getString("estado")
                    );

                    dto.setModuloOrigen(
                            rs.getString("modulo_origen")
                    );

                    dto.setProcesoOrigen(
                            rs.getString("proceso_origen")
                    );

                    dto.setTablaOrigen(
                            rs.getString("tabla_origen")
                    );

                    dto.setIdOrigen(
                            rs.getLong("id_origen")
                    );

                    dto.setCodigoReferencia(
                            rs.getString("codigo_referencia")
                    );

                    dto.setTipoComprobante(
                            rs.getString("tipo_comprobante")
                    );

                    dto.setNumeroComprobante(
                            rs.getString("numero_comprobante")
                    );

                    dto.setIdDatosPersonal(
                            rs.getInt("id_datos_personal")
                    );

                    dto.setTipoDocumento(
                            rs.getString("tipo_documento")
                    );

                    dto.setDocumento(
                            rs.getString("documento")
                    );

                    dto.setNombreCompleto(
                            rs.getString("nombre_completo")
                    );

                    return dto;
                }
        );
    }


    // =========================================================
    // CONFIRMAR
    // =========================================================

    public int confirmar(
            Long idMovimientoCaja,
            Long idCaja,
            Integer idUsuario
    ) {

        String sql = """
                UPDATE cajas.movimientos_caja
                SET
                    estado = 'REGISTRADO',
                    fk_seguridad_edicion = :idUsuario,
                    fecha_edicion = CURRENT_TIMESTAMP
                WHERE id_movimiento_caja = :idMovimientoCaja
                  AND id_caja = :idCaja
                  AND estado = 'PENDIENTE'
                """;

        MapSqlParameterSource params =
                new MapSqlParameterSource()
                        .addValue(
                                "idMovimientoCaja",
                                idMovimientoCaja
                        )
                        .addValue(
                                "idCaja",
                                idCaja
                        )
                        .addValue(
                                "idUsuario",
                                idUsuario
                        );

        return jdbc.update(
                sql,
                params
        );
    }
}