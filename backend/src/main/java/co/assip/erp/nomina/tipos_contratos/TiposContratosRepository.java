package co.assip.erp.nomina.tipos_contratos;

import co.assip.erp.nomina.tipos_contratos.dto.TipoContratoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TiposContratosRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<TipoContratoDTO> listar() {

        String sql = """
            SELECT
                tc.id_tipo_contrato,
                tc.codigo,
                tc.nombre,
                tc.activo,
                tc.codigo_superintendencia,
                tc.aplica_salud,
                tc.aplica_pension,
                tc.aplica_arl,
                tc.aplica_caja_compensacion,
                tc.aplica_cesantias,
                tc.aplica_prima,
                tc.aplica_vacaciones,
                tc.aplica_parafiscales
            FROM nomina.tipos_contrato tc
            WHERE tc.activo = true
            ORDER BY tc.nombre
        """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (rs, rowNum) -> TipoContratoDTO.builder()
                        .idTipoContrato(rs.getInt("id_tipo_contrato"))
                        .codigo(rs.getString("codigo"))
                        .nombre(rs.getString("nombre"))
                        .activo(rs.getBoolean("activo"))
                        .codigoSuperintendencia(rs.getString("codigo_superintendencia"))

                        // 🔥 NUEVO
                        .aplicaSalud(rs.getBoolean("aplica_salud"))
                        .aplicaPension(rs.getBoolean("aplica_pension"))
                        .aplicaArl(rs.getBoolean("aplica_arl"))
                        .aplicaCajaCompensacion(rs.getBoolean("aplica_caja_compensacion"))
                        .aplicaCesantias(rs.getBoolean("aplica_cesantias"))
                        .aplicaPrima(rs.getBoolean("aplica_prima"))
                        .aplicaVacaciones(rs.getBoolean("aplica_vacaciones"))
                        .aplicaParafiscales(rs.getBoolean("aplica_parafiscales"))

                        .build()
        );
    }
}