package co.assip.erp.activosfijos.catalogos;

import co.assip.erp.activosfijos.catalogos.dto.EstadoActivoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EstadosActivoRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<EstadoActivoDTO> listar() {

        String sql = """
            SELECT
                id_estado_activo,
                nombre_estado
            FROM activos_fijos.estados_activo
            ORDER BY nombre_estado
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            EstadoActivoDTO dto = new EstadoActivoDTO();
            dto.setIdEstadoActivo(rs.getInt("id_estado_activo"));
            dto.setNombreEstadoActivo(rs.getString("nombre_estado"));
            return dto;
        });
    }
}
