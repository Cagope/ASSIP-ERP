package co.assip.erp.superintendencia.aportes;

import co.assip.erp.superintendencia.aportes.dto.AportesDTO;
import co.assip.erp.superintendencia.aportes.repository.AportesRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AportesService {

    private final AportesRepository repo;

    public AportesService(AportesRepository repo) {
        this.repo = repo;
    }

    public List<AportesDTO> consultar(String fechaCorte) {

        List<Map<String, Object>> rows = repo.consultar(fechaCorte);
        List<AportesDTO> lista = new ArrayList<>();

        for (Map<String, Object> row : rows) {

            AportesDTO dto = new AportesDTO();

            // ================================
            // CAMPOS EXACTOS DEL SELECT REAL
            // ================================

            dto.setNumeroIdentificacion(
                    row.get("cedula") != null ? row.get("cedula").toString() : null
            );

            // 🟩 NUEVO — tipo_identificacion agregado desde el repository
            dto.setTipoIdentificacion(
                    row.get("tipo_identificacion") != null ? row.get("tipo_identificacion").toString() : null
            );

            dto.setNombreCompleto(
                    row.get("nombre") != null ? row.get("nombre").toString() : null
            );

            dto.setCodigoCuenta(
                    row.get("codigo_cuenta") != null ? row.get("codigo_cuenta").toString() : null
            );

            dto.setFechaIngreso(
                    row.get("fecha_apertura") != null ? row.get("fecha_apertura").toString() : null
            );

            dto.setSaldoAportes(getDouble(row, "saldo"));
            dto.setValorRevalorizacion(getDouble(row, "revalorizacion"));
            dto.setAportesOrdinarios(getDouble(row, "aportes_ordinarios"));
            dto.setPromedioDiaAnual(getDouble(row, "promedio_dia_anual"));

            dto.setFechaUltimoPago(
                    row.get("fecha_ultimo_pago") != null ?
                            row.get("fecha_ultimo_pago").toString() : null
            );

            // ================================
            // CAMPOS QUE NO EXISTEN EN EL SELECT
            // ================================
            dto.setPrimerApellido(null);
            dto.setSegundoApellido(null);
            dto.setNombres(null);
            dto.setValorAporteMensual(null);
            dto.setAportesExtraordinarios(null);

            lista.add(dto);
        }

        return lista;
    }

    private Double getDouble(Map<String, Object> row, String key) {
        return row.get(key) != null ? ((Number) row.get(key)).doubleValue() : 0.0;
    }
}
