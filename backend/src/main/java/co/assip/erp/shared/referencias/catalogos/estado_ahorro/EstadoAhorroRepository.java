package co.assip.erp.shared.referencias.catalogos.estado_ahorro;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EstadoAhorroRepository {

    private final JdbcTemplate jdbc;

    // ============================================================
    // 🔹 Listar TODOS los estados
    // ============================================================
    public List<EstadoAhorro> listar() {

        String sql = """
            SELECT 
                codigo_estado_ahorro,
                descripcion_estado_ahorro,
                observaciones_estado_ahorro,
                operativo
            FROM depositos.estados_ahorros
            ORDER BY codigo_estado_ahorro
            """;

        return jdbc.query(sql, (rs, rowNum) -> {
            EstadoAhorro e = new EstadoAhorro();
            e.setCodigoEstadoAhorro(rs.getString("codigo_estado_ahorro"));
            e.setDescripcionEstadoAhorro(rs.getString("descripcion_estado_ahorro"));
            e.setObservacionesEstadoAhorro(rs.getString("observaciones_estado_ahorro"));
            e.setOperativo(rs.getBoolean("operativo"));
            return e;
        });
    }

    // ============================================================
    // 🔹 Listar SOLO operativos (para combos)
    // ============================================================
    public List<EstadoAhorro> listarOperativos() {

        String sql = """
            SELECT 
                codigo_estado_ahorro,
                descripcion_estado_ahorro,
                observaciones_estado_ahorro,
                operativo
            FROM depositos.estados_ahorros
            WHERE operativo = true
            ORDER BY codigo_estado_ahorro
            """;

        return jdbc.query(sql, (rs, rowNum) -> {
            EstadoAhorro e = new EstadoAhorro();
            e.setCodigoEstadoAhorro(rs.getString("codigo_estado_ahorro"));
            e.setDescripcionEstadoAhorro(rs.getString("descripcion_estado_ahorro"));
            e.setObservacionesEstadoAhorro(rs.getString("observaciones_estado_ahorro"));
            e.setOperativo(rs.getBoolean("operativo"));
            return e;
        });
    }
}
