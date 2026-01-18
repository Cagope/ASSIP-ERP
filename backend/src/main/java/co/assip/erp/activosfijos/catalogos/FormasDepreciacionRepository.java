package co.assip.erp.activosfijos.catalogos;

import co.assip.erp.activosfijos.catalogos.dto.FormaDepreciacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FormasDepreciacionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<FormaDepreciacionDTO> listar() {

        String sql = """
            SELECT
                id_forma_depreciacion,
                codigo_forma,
                nombre_forma
            FROM activos_fijos.formas_depreciacion
            ORDER BY nombre_forma
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            FormaDepreciacionDTO dto = new FormaDepreciacionDTO();
            dto.setIdFormaDepreciacion(rs.getInt("id_forma_depreciacion"));
            dto.setCodigoForma(rs.getString("codigo_forma"));
            dto.setNombreForma(rs.getString("nombre_forma"));
            return dto;
        });
    }
}
