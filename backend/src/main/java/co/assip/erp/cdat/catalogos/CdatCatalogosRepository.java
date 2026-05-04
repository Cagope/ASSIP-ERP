package co.assip.erp.cdat.catalogos;

import co.assip.erp.cdat.catalogos.dto.CdatAmortizacionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CdatCatalogosRepository {

    private final JdbcTemplate jdbc;

    public List<CdatAmortizacionDTO> listarAmortizaciones() {
        String sql = """
            SELECT
                codigo_amortizacion,
                nombre_amortizacion,
                meses
            FROM cdat.amortizaciones_cdats
            ORDER BY meses
        """;

        return jdbc.query(sql, (rs, rowNum) -> CdatAmortizacionDTO.builder()
                .codigoAmortizacion(rs.getString("codigo_amortizacion"))
                .nombreAmortizacion(rs.getString("nombre_amortizacion"))
                .meses(rs.getInt("meses"))
                .build());
    }
}