package co.assip.erp.shared.referencias.catalogos.acciones_cuentas_conjuntas;

import co.assip.erp.shared.referencias.catalogos.acciones_cuentas_conjuntas.dto.AccionCuentaConjuntaDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AccionesCuentasConjuntasRepository {

    private final JdbcTemplate jdbc;

    public List<AccionCuentaConjuntaDTO> listar() {

        String sql = """
                SELECT
                    codigo_accion,
                    descripcion_accion
                FROM depositos.acciones_cuentas_conjuntas
                ORDER BY codigo_accion
                """;

        return jdbc.query(
                sql,
                (rs, rowNum) -> new AccionCuentaConjuntaDTO(
                        rs.getString("codigo_accion"),
                        rs.getString("descripcion_accion")
                )
        );
    }
}