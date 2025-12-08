package co.assip.erp.depositos.revalorizacion;

import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionEntradaDTO;
import co.assip.erp.depositos.revalorizacion.dto.RevalorizacionItemDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 🎯 RevalorizacionService (sin impacto en BD)
 * ----------------------------------------------------
 * 1. Ejecuta el cálculo técnico (promedios, saldos, revalorización)
 * 2. NO inserta movimientos ni actualiza cuentas (pruebas seguras)
 * 3. Devuelve el listado hacia el front
 */
@Service
@RequiredArgsConstructor
public class RevalorizacionService {

    private final RevalorizacionRepository repository;
    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Ejecuta el proceso SOLO DE CÁLCULO.
     * NO modifica tablas.
     */
    @Transactional(readOnly = true)
    public List<RevalorizacionItemDTO> ejecutar(RevalorizacionEntradaDTO input, Integer usuarioId) {

        // =====================================================
        // 1️⃣ VALIDACIÓN — Revisar forma seleccionada
        // =====================================================
        Map<String, Object> forma = jdbc.queryForMap("""
            SELECT 
                id_forma_ahorro,
                tiempo_liquidacion
            FROM depositos.formas_ahorro
            WHERE id_forma_ahorro = :formaId
        """, Map.of("formaId", input.getFormaId()));

        Integer tiempo = ((Number) forma.get("tiempo_liquidacion")).intValue();

        // ✔ Revalorización solo aplica a liquidación anual (360)
        if (tiempo != 360) {
            throw new IllegalStateException(
                    "ERROR_VALIDACION|La forma seleccionada NO permite revalorización. "
                            + "(tiempo_liquidacion = " + tiempo + ")"
            );
        }

        // =====================================================
        // 2️⃣ Ejecutar cálculo técnico desde el repository
        // =====================================================
        List<Map<String, Object>> rows = repository.calcular(input);

        List<RevalorizacionItemDTO> resultado = new ArrayList<>();

        for (Map<String, Object> row : rows) {

            String documento = (String) row.get("documento");

            BigDecimal saldoActual = num(row.get("saldo_actual"));
            BigDecimal valorPromedio = num(row.get("valor_promedio"));
            BigDecimal valorReval = num(row.get("valor_revalorizacion"));

            // ❌  IMPACTO EN BD (NO eliminar)
            // if (valorReval.compareTo(BigDecimal.ZERO) > 0) {
            //     registrarMovimientoReval(documento, saldoActual, valorReval, input.getFechaContabilizacion(), usuarioId);
            //     actualizarSaldoCuenta(documento, valorReval, usuarioId);
            // }

            // 3️⃣ Armar DTO salida
            RevalorizacionItemDTO dto = new RevalorizacionItemDTO();
            dto.setTipoDocumento((String) row.get("tipo_documento"));
            dto.setDocumento(documento);
            dto.setNombreCompleto((String) row.get("nombre_completo"));
            dto.setSaldoActual(saldoActual);
            dto.setValorPromedio(valorPromedio);
            dto.setValorRevalorizacion(valorReval);
            dto.setEstadoCuenta((String) row.get("estado_cuenta"));

            resultado.add(dto);
        }

        return resultado;
    }

    // ---------------------------------------------------------------------
    // ⚠ Métodos de impacto en BD — Deshabilitados para pruebas (NO BORRAR)
    // ---------------------------------------------------------------------

//    private void registrarMovimientoReval(
//            String documento,
//            BigDecimal saldoActual,
//            BigDecimal valorReval,
//            LocalDate fechaContable,
//            Integer usuarioId
//    ) {
//        // Método intencionalmente deshabilitado
//    }

//    private void actualizarSaldoCuenta(String documento, BigDecimal valorReval, Integer usuarioId) {
//        // Método intencionalmente deshabilitado
//    }

    // Utilidad para convertir posibles nulls a BigDecimal
    private BigDecimal num(Object o) {
        return o == null ? BigDecimal.ZERO : new BigDecimal(o.toString());
    }
}
