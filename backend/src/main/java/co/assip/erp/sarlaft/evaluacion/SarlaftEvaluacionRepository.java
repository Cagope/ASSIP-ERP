package co.assip.erp.sarlaft.evaluacion;

import co.assip.erp.sarlaft.evaluacion.dto.ReglasInput;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SarlaftEvaluacionRepository {

    private final JdbcTemplate jdbc;

    public void completarDatosPersona(ReglasInput input) {

        System.out.println("==== ENTRA SARLAFT EVALUACION REPOSITORY ====");
        System.out.println("input null = " + (input == null));
        System.out.println("idDatosPersonal = " + (input == null ? null : input.getIdDatosPersonal()));
        System.out.println("============================================");

        if (input == null || input.getIdDatosPersonal() == null) {
            return;
        }

        String sql = """
            SELECT
                hv.id_datos_personal,
                hv.tipo_documento,
                hv.fecha_nacimiento,
                hv.fecha_actualizacion,

                (
                    COALESCE(hv.valor_salario, 0)
                    + COALESCE(hv.valor_pension, 0)
                    + COALESCE(hv.ingresos_arriendo, 0)
                    + COALESCE(hv.ingresos_comisiones, 0)
                    + COALESCE(hv.otros_ingresos, 0)
                ) AS ingresos_mensuales,

                (
                    COALESCE(hv.egresos_familiares, 0)
                    + COALESCE(hv.egresos_arriendo, 0)
                    + COALESCE(hv.egresos_credito, 0)
                    + COALESCE(hv.otros_egresos, 0)
                ) AS egresos_mensuales,

                hv.total_activos,
                hv.total_pasivos

            FROM reporting.vw_hoja_vida_general_total_reciente hv
            WHERE hv.id_datos_personal = ?
            LIMIT 1
            """;

        jdbc.query(
                sql,
                rs -> {

                    if (input.getTipoDocumento() == null) {
                        input.setTipoDocumento(
                                rs.getString("tipo_documento")
                        );
                    }

                    if (input.getFechaNacimiento() == null) {
                        input.setFechaNacimiento(
                                rs.getDate("fecha_nacimiento")
                        );
                    }

                    if (input.getFechaUltimaActualizacion() == null) {
                        input.setFechaUltimaActualizacion(
                                rs.getTimestamp("fecha_actualizacion")
                        );
                    }

                    if (input.getIngresosMensuales() == null) {
                        input.setIngresosMensuales(
                                getDouble(rs, "ingresos_mensuales")
                        );
                    }

                    if (input.getEgresosMensuales() == null) {
                        input.setEgresosMensuales(
                                getDouble(rs, "egresos_mensuales")
                        );
                    }

                    if (input.getTotalActivos() == null) {
                        input.setTotalActivos(
                                getDouble(rs, "total_activos")
                        );
                    }

                    if (input.getTotalPasivos() == null) {
                        input.setTotalPasivos(
                                getDouble(rs, "total_pasivos")
                        );
                    }
                },
                input.getIdDatosPersonal()
        );
    }

    private Double getDouble(
            java.sql.ResultSet rs,
            String column
    ) throws java.sql.SQLException {

        Object value = rs.getObject(column);

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        return null;
    }
}