package co.assip.erp.contabilidad.auxiliares_contables;

import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * Repository JDBC para insertar en contabilidad.auxiliares_contables
 * (detalle contable real).
 */
@Repository
public class AuxiliaresContablesRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AuxiliaresContablesRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * ✅ Inserta un movimiento en auxiliares_contables.
     * Retorna el id_auxiliar_contable generado.
     */
    public Integer insertarAuxiliar(
            MovimientoContableDTO mov,
            Integer idUsuario
    ) {

        String sql = """
        INSERT INTO contabilidad.auxiliares_contables (
            id_catalogo_cuenta,
            id_agencia,
            id_datos_personal,
            fecha_auxiliar,
            tipo_comprobante,
            numero_comprobante,
            detalle_movimiento,
            estado_movimiento,
            valor_debito,
            valor_credito,
            valor_base,
            origen_modulo,
            documento_origen,
            fk_seguridad_creacion,
            fk_seguridad_edicion
        ) VALUES (
            :idCatalogoCuenta,
            :idAgencia,
            :idDatosPersonal,
            :fechaAuxiliar,
            :tipoComprobante,
            :numeroComprobante,
            :detalleMovimiento,
            'A',
            :valorDebito,
            :valorCredito,
            :valorBase,
            :origenModulo,
            :documentoOrigen,
            :usuario,
            :usuario
        )
        RETURNING id_auxiliar_contable
    """;

        Integer idAux = jdbc.queryForObject(sql,
                new MapSqlParameterSource()
                        .addValue("idCatalogoCuenta", mov.getIdCatalogoCuenta())
                        .addValue("idAgencia", mov.getIdAgencia())
                        .addValue("idDatosPersonal", mov.getIdDatosPersonal())
                        .addValue("fechaAuxiliar", mov.getFechaAuxiliar())
                        .addValue("tipoComprobante", mov.getTipoComprobante())
                        .addValue("numeroComprobante", mov.getNumeroComprobante())
                        .addValue("detalleMovimiento", mov.getDetalleMovimiento())
                        .addValue("valorDebito", mov.getValorDebito())
                        .addValue("valorCredito", mov.getValorCredito())
                        .addValue("valorBase", mov.getValorBase() == null ? BigDecimal.ZERO : mov.getValorBase())
                        .addValue("origenModulo", mov.getOrigenModulo() == null ? "CONTABILIDAD" : mov.getOrigenModulo())
                        .addValue("documentoOrigen", mov.getDocumentoOrigen())
                        .addValue("usuario", idUsuario),
                Integer.class
        );

        if (idAux == null) {
            throw new IllegalStateException("No se pudo insertar el auxiliar contable.");
        }

        return idAux;
    }
}
