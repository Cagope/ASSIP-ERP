package co.assip.erp.shared.referencias.catalogos.tipo_gmf;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TipoGmfRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public List<TipoGmf> listar() {
        String sql = """
            SELECT 
                codigo_tipo_gmf,
                descripcion_tipos_gmf,
                observaciones_tipos_gmf
            FROM depositos.tipos_gmf
            ORDER BY codigo_tipo_gmf
        """;

        return jdbc.query(sql, (rs, rowNum) -> {
            TipoGmf t = new TipoGmf();
            t.setCodigoTipoGmf(rs.getString("codigo_tipo_gmf"));
            t.setDescripcionTiposGmf(rs.getString("descripcion_tipos_gmf"));
            t.setObservacionesTiposGmf(rs.getString("observaciones_tipos_gmf"));
            return t;
        });
    }
}
