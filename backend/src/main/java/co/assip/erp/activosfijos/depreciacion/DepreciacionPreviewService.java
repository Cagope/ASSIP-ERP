package co.assip.erp.activosfijos.depreciacion;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DepreciacionPreviewService {

    private final DepreciacionPreviewRepository previewRepository;

    public DepreciacionPreviewService(DepreciacionPreviewRepository previewRepository) {
        this.previewRepository = previewRepository;
    }

    /**
     * ✅ Ejecuta el preview del proceso de depreciación.
     * ✅ NO escribe en base de datos.
     * ✅ Filtra estrictamente por la agencia seleccionada.
     */
    public DepreciacionPreviewResult ejecutarPreview(DepreciacionRequestDTO request) {

        // =========================
        // 1️⃣ Validaciones básicas
        // =========================
        if (request == null) {
            throw new IllegalArgumentException("La solicitud es obligatoria");
        }

        if (request.getIdAgencia() == null) {
            throw new IllegalArgumentException("La agencia es obligatoria");
        }

        if (request.getFechaPeriodo() == null) {
            throw new IllegalArgumentException("La fecha del período es obligatoria");
        }

        if (request.getFechaContabilizacion() == null) {
            throw new IllegalArgumentException("La fecha de contabilización es obligatoria");
        }

        if (request.getTipoComprobante() == null || request.getTipoComprobante().isBlank()) {
            throw new IllegalArgumentException("El tipo de comprobante es obligatorio");
        }

        if (request.getNumeroComprobante() == null || request.getNumeroComprobante().isBlank()) {
            throw new IllegalArgumentException("El número de comprobante es obligatorio");
        }

        if (request.getConcepto() == null || request.getConcepto().isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }

        // =========================
        // 2️⃣ Obtener preview ✅ FILTRADO POR AGENCIA
        // =========================
        List<DepreciacionPreviewDTO> detalle =
                previewRepository.obtenerPreview(request.getIdAgencia());

        if (detalle.isEmpty()) {
            throw new IllegalStateException(
                    "No existen activos para depreciar para la agencia " + request.getIdAgencia()
            );
        }

        // =========================
        // 3️⃣ Calcular totales
        // =========================
        BigDecimal total = detalle.stream()
                .map(d -> d.getValorPeriodo() == null ? BigDecimal.ZERO : d.getValorPeriodo())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // =========================
        // 4️⃣ Armar resultado
        // =========================
        DepreciacionPreviewResult result = new DepreciacionPreviewResult();
        result.setDetalle(detalle);
        result.setTotalDebito(total);
        result.setTotalCredito(total);

        return result;
    }
}
