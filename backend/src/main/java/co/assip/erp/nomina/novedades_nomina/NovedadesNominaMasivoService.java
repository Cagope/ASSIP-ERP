package co.assip.erp.nomina.novedades_nomina;

import co.assip.erp.nomina.novedades_nomina.dto.NovedadMasivaRequestDTO;
import co.assip.erp.nomina.novedades_nomina.dto.NovedadMasivaResultDTO;
import co.assip.erp.nomina.periodos_nomina.PeriodoNominaActivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import co.assip.erp.shared.math.MathUtils;

@Service
@RequiredArgsConstructor
public class NovedadesNominaMasivoService {

    private final NovedadesNominaMasivoRepository repo;
    private final PeriodoNominaActivoService periodoActivoService;

    // =====================================================
    // 🔍 PREVIEW MASIVO (NO GRABA)
    // =====================================================
    public List<Map<String, Object>> previewMasivo(
            NovedadMasivaRequestDTO req
    ) {

        if (req == null) {
            throw new IllegalArgumentException("Datos obligatorios");
        }

        if (req.getCodigoConcepto() == null || req.getCodigoConcepto().isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }

        String codigoConcepto = req.getCodigoConcepto().trim().toUpperCase();

        // -------------------------------------------------
        // 🔥 PERÍODO OPERATIVO (FUENTE ÚNICA)
        // -------------------------------------------------
        Integer idPeriodo = periodoActivoService.obtenerPeriodoActivo();

        Map<String, Object> periodo = repo.obtenerPeriodo(idPeriodo);
        if (periodo == null || periodo.isEmpty()) {
            throw new IllegalStateException(
                    "No existe un período operativo válido"
            );
        }

        Integer idAgencia = (Integer) periodo.get("id_agencia");
        Date sqlIni = (Date) periodo.get("fecha_inicio");
        Date sqlFin = (Date) periodo.get("fecha_fin");

        if (idAgencia == null || sqlIni == null || sqlFin == null) {
            throw new IllegalStateException("Período operativo incompleto");
        }

        LocalDate fechaInicial = sqlIni.toLocalDate();
        LocalDate fechaFinal   = sqlFin.toLocalDate();

        // -------------------------------------------------
        // VALORES
        // -------------------------------------------------
        BigDecimal cantidad = MathUtils.pesos(req.getCantidad());

        BigDecimal valorManual = MathUtils.pesos(req.getValor());

        // -------------------------------------------------
        // PREVIEW
        // -------------------------------------------------
        return repo.previewMasivo(
                idPeriodo,
                idAgencia,
                codigoConcepto,
                fechaInicial,
                fechaFinal,
                cantidad,
                valorManual
        );
    }

    // =====================================================
    // ✅ APLICAR / INSERT MASIVO
    // =====================================================
    public NovedadMasivaResultDTO generarMasivo(
            NovedadMasivaRequestDTO req,
            Integer idUsuario
    ) {

        if (req == null) {
            throw new IllegalArgumentException("Datos obligatorios");
        }

        if (req.getCodigoConcepto() == null || req.getCodigoConcepto().isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }

        String codigoConcepto = req.getCodigoConcepto().trim().toUpperCase();

        // -------------------------------------------------
        // 🔥 PERÍODO OPERATIVO (FUENTE ÚNICA)
        // -------------------------------------------------
        Integer idPeriodo = periodoActivoService.obtenerPeriodoActivo();

        Map<String, Object> periodo = repo.obtenerPeriodo(idPeriodo);
        if (periodo == null || periodo.isEmpty()) {
            throw new IllegalStateException(
                    "No existe un período operativo válido"
            );
        }

        Integer idAgencia = (Integer) periodo.get("id_agencia");
        Date sqlIni = (Date) periodo.get("fecha_inicio");
        Date sqlFin = (Date) periodo.get("fecha_fin");

        if (idAgencia == null || sqlIni == null || sqlFin == null) {
            throw new IllegalStateException("Período operativo incompleto");
        }

        LocalDate fechaInicial = sqlIni.toLocalDate();
        LocalDate fechaFinal   = sqlFin.toLocalDate();

        // -------------------------------------------------
        // VALORES
        // -------------------------------------------------
        BigDecimal cantidad = MathUtils.pesos(req.getCantidad());

        BigDecimal valorManual = MathUtils.pesos(req.getValor());

        String observacion = req.getObservacion();
        String estado = "ABIERTO";

        // -------------------------------------------------
        // CONTAR CONTRATOS
        // -------------------------------------------------
        int total = repo.contarContratosElegibles(
                idAgencia,
                fechaInicial,
                fechaFinal
        );

        // -------------------------------------------------
        // INSERT MASIVO
        // -------------------------------------------------
        int insertados = repo.insertarMasivo(
                idPeriodo,
                idAgencia,
                codigoConcepto,
                fechaInicial,
                fechaFinal,
                cantidad,
                valorManual,
                observacion,
                estado,
                idUsuario
        );

        int omitidos = Math.max(total - insertados, 0);

        return NovedadMasivaResultDTO.builder()
                .totalContratos(total)
                .insertados(insertados)
                .omitidos(omitidos)
                .build();
    }
}