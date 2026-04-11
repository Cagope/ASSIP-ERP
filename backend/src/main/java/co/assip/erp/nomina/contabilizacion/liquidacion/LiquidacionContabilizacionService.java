package co.assip.erp.nomina.contabilizacion.liquidacion;

import co.assip.erp.nomina.contabilizacion.liquidacion.dto.*;
import co.assip.erp.seguridad.utils.SecurityUtils;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiquidacionContabilizacionService {

    private final LiquidacionContabilizacionRepository repository;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final PeriodosNominaRepository periodosNominaRepository;

    // =========================================================
    // PREVIEW CONTABLE
    // =========================================================
    public List<LiquidacionMovimientoContableDTO> preview(Integer idPeriodoNomina) {

        String comprobanteActivo =
                repository.obtenerComprobanteExistente(idPeriodoNomina);

        if (comprobanteActivo != null) {
            throw new IllegalStateException(
                    "El período ya fue contabilizado."
            );
        }

        if (!repository.periodoEstaCerrado(idPeriodoNomina)) {
            throw new IllegalStateException(
                    "El período de nómina debe estar en estado CERRADO para contabilizar."
            );
        }

        return repository.preview(idPeriodoNomina);
    }

    // =========================================================
    // CONTABILIZAR
    // =========================================================
    @Transactional
    public LiquidacionContabilizacionResultadoDTO contabilizar(Integer idPeriodoNomina) {

        // 🔒 BLOQUEAR PERIODO NOMINA
        repository.bloquearPeriodoNomina(idPeriodoNomina);

        if (!repository.periodoEstaCerrado(idPeriodoNomina)) {
            throw new IllegalStateException(
                    "El período de nómina debe estar en estado CERRADO para contabilizar."
            );
        }

        Integer idUsuario = SecurityUtils.getIdUsuario();

        List<Integer> agencias = repository.listarAgenciasConMovimientos(idPeriodoNomina);

        if (agencias.isEmpty()) {
            throw new IllegalStateException(
                    "No existen movimientos de liquidación para contabilizar en el período " + idPeriodoNomina
            );
        }

        List<LiquidacionComprobanteGeneradoDTO> comprobantes = new ArrayList<>();

        LocalDate fechaComprobante = repository.obtenerFechaComprobante(idPeriodoNomina);

        var periodoLabel = periodosNominaRepository.obtenerPeriodoLabel(idPeriodoNomina);

        if (periodoLabel == null) {
            throw new IllegalStateException(
                    "No se pudo obtener el label del período de nómina " + idPeriodoNomina
            );
        }

        String periodoTexto = String.format(
                "%d-%02d-%d",
                periodoLabel.anio(),
                periodoLabel.mes(),
                periodoLabel.numeroPeriodo()
        );

        // =====================================================
        // RECORRER AGENCIAS
        // =====================================================

        for (Integer idAgencia : agencias) {

            // -------------------------------------------------
            // VALIDAR PERIODO CONTABLE
            // -------------------------------------------------

            if (repository.periodoContableCerrado(idAgencia, fechaComprobante)) {
                throw new IllegalStateException(
                        "El período contable está cerrado para la agencia "
                                + idAgencia + " y la fecha " + fechaComprobante
                );
            }

            List<LiquidacionMovimientoContableDTO> movimientos =
                    repository.previewPorAgencia(idPeriodoNomina, idAgencia);

            if (movimientos.isEmpty()) {
                continue;
            }

            List<String> cuentasNoOperables =
                    repository.listarCuentasNoOperablesEnPreview(idPeriodoNomina, idAgencia);

            if (!cuentasNoOperables.isEmpty()) {
                throw new IllegalStateException(
                        "Existen cuentas no operables en la agencia "
                                + idAgencia + ": " + String.join(", ", cuentasNoOperables)
                );
            }

            // -------------------------------------------------
            // CALCULAR TOTALES
            // -------------------------------------------------

            BigDecimal totalDebito = movimientos.stream()
                    .map(m -> m.getDebito() == null ? BigDecimal.ZERO : m.getDebito())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCredito = movimientos.stream()
                    .map(m -> m.getCredito() == null ? BigDecimal.ZERO : m.getCredito())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // -------------------------------------------------
            // VALIDAR CUADRE CONTABLE
            // -------------------------------------------------

            if (totalDebito.compareTo(totalCredito) != 0) {
                throw new IllegalStateException(
                        "El comprobante de la agencia " + idAgencia +
                                " no está cuadrado. Débito=" + totalDebito +
                                " Crédito=" + totalCredito
                );
            }

            // -------------------------------------------------
            // GENERAR CONSECUTIVO
            // -------------------------------------------------

            String tipoComprobante = "NM";

            Integer consecutivoActual =
                    repository.obtenerConsecutivoActualConLock(tipoComprobante, idAgencia);

            Integer nuevoConsecutivo = consecutivoActual + 1;

            String numeroComprobante = String.format("%010d", nuevoConsecutivo);

            String concepto =
                    "Contabilización liquidación nómina  " + periodoTexto;

            // -------------------------------------------------
            // VALIDAR QUE EL COMPROBANTE NO EXISTA
            // -------------------------------------------------

            if (repository.comprobanteExiste(tipoComprobante, numeroComprobante, idAgencia)) {
                throw new IllegalStateException(
                        "El comprobante " + tipoComprobante + "-" + numeroComprobante +
                                " ya existe para la agencia " + idAgencia
                );
            }

            // -------------------------------------------------
            // CONVERTIR MOVIMIENTOS A CONTABILIDAD
            // -------------------------------------------------

            List<MovimientoContableDTO> movimientosContables = new ArrayList<>();

            for (LiquidacionMovimientoContableDTO mov : movimientos) {

                MovimientoContableDTO m = new MovimientoContableDTO();

                m.setIdCatalogoCuenta(mov.getIdCatalogoCuenta());
                m.setIdAgencia(idAgencia);
                m.setIdDatosPersonal(mov.getIdTercero());

                m.setFechaAuxiliar(fechaComprobante);

                m.setDetalleMovimiento(
                        "Liquidación nómina " + periodoTexto +
                                " - " + (mov.getNombreEmpleadoReferencia() != null
                                ? mov.getNombreEmpleadoReferencia()
                                : "")
                );

                m.setValorDebito(mov.getDebito());
                m.setValorCredito(mov.getCredito());
                m.setValorBase(mov.getValorBase());

                movimientosContables.add(m);
            }

            // -------------------------------------------------
            // ORIGEN DEL COMPROBANTE
            // -------------------------------------------------

            OrigenComprobanteDTO origen = new OrigenComprobanteDTO();

            origen.setOrigenTipo("AUTO");
            origen.setModuloOrigen("NOMINA");
            origen.setProcesoOrigen("LIQUIDACION_NOMINA");
            origen.setTablaOrigen("nomina.liquidaciones");
            origen.setIdOrigen(idPeriodoNomina.longValue());

            // -------------------------------------------------
            // REGISTRAR CONTABILIDAD
            // -------------------------------------------------

            contabilidadRegistroService.registrarComprobante(
                    idAgencia,
                    tipoComprobante,
                    numeroComprobante,
                    concepto,
                    origen,
                    movimientosContables,
                    idUsuario
            );

            // -------------------------------------------------
            // ACTUALIZAR CONSECUTIVO
            // -------------------------------------------------

            repository.actualizarConsecutivo(
                    tipoComprobante,
                    idAgencia,
                    nuevoConsecutivo,
                    idUsuario
            );

            // -------------------------------------------------
            // RESULTADO
            // -------------------------------------------------

            comprobantes.add(
                    LiquidacionComprobanteGeneradoDTO.builder()
                            .idAgencia(idAgencia)
                            .tipoComprobante(tipoComprobante)
                            .numeroComprobante(numeroComprobante)
                            .conceptoComprobante(concepto)
                            .fechaComprobante(fechaComprobante)
                            .cantidadMovimientos(movimientos.size())
                            .totalDebito(totalDebito)
                            .totalCredito(totalCredito)
                            .build()
            );
        }

        // =====================================================
        // MARCAR PERIODO COMO CONTABILIZADO
        // =====================================================

        repository.marcarPeriodoComoContabilizado(
                idPeriodoNomina,
                idUsuario
        );

        // =====================================================
        // RESPUESTA FINAL
        // =====================================================

        return LiquidacionContabilizacionResultadoDTO.builder()
                .idPeriodoNomina(idPeriodoNomina)
                .cantidadAgencias(comprobantes.size())
                .comprobantes(comprobantes)
                .build();
    }

    @Transactional
    public void reversarContabilizacion(Integer idPeriodoNomina) {

        Integer idUsuario = SecurityUtils.getIdUsuario();

        List<Integer> agencias = repository.listarAgenciasConMovimientos(idPeriodoNomina);

        LocalDate fechaComprobante = repository.obtenerFechaComprobante(idPeriodoNomina);

        var periodoLabel = periodosNominaRepository.obtenerPeriodoLabel(idPeriodoNomina);

        if (periodoLabel == null) {
            throw new IllegalStateException(
                    "No se pudo obtener el label del período de nómina " + idPeriodoNomina
            );
        }

        String periodoTexto = String.format(
                "%d-%02d-%d",
                periodoLabel.anio(),
                periodoLabel.mes(),
                periodoLabel.numeroPeriodo()
        );


        for (Integer idAgencia : agencias) {

            String tipoComprobante = "NM";

            String numeroOriginal =
                    repository.obtenerNumeroComprobanteNomina(idPeriodoNomina, idAgencia);

            List<LiquidacionMovimientoContableDTO> movimientos =
                    repository.obtenerMovimientosComprobante(
                            tipoComprobante,
                            numeroOriginal,
                            idAgencia
                    );

            List<MovimientoContableDTO> movimientosReversion = new ArrayList<>();

            for (LiquidacionMovimientoContableDTO mov : movimientos) {

                MovimientoContableDTO m = new MovimientoContableDTO();

                m.setIdCatalogoCuenta(mov.getIdCatalogoCuenta());
                m.setIdAgencia(idAgencia);
                m.setIdDatosPersonal(mov.getIdTercero());
                m.setFechaAuxiliar(fechaComprobante);

                m.setDetalleMovimiento(
                        "REVERSIÓN liquidación nómina período " + periodoTexto
                );

                // 🔥 invertir débitos y créditos
                m.setValorDebito(mov.getCredito());
                m.setValorCredito(mov.getDebito());
                m.setValorBase(mov.getValorBase());

                movimientosReversion.add(m);
            }

            Integer consecutivoActual =
                    repository.obtenerConsecutivoActualConLock(tipoComprobante, idAgencia);

            Integer nuevoConsecutivo = consecutivoActual + 1;

            String numeroComprobante = String.format("%010d", nuevoConsecutivo);

            String concepto =
                    "REVERSIÓN contabilización nómina período " + periodoTexto;

            OrigenComprobanteDTO origen = new OrigenComprobanteDTO();

            origen.setOrigenTipo("AUTO");
            origen.setModuloOrigen("NOMINA");
            origen.setProcesoOrigen("REVERSO_LIQUIDACION_NOMINA");
            origen.setTablaOrigen("nomina.liquidaciones");
            origen.setIdOrigen(idPeriodoNomina.longValue());

            contabilidadRegistroService.registrarComprobante(
                    idAgencia,
                    tipoComprobante,
                    numeroComprobante,
                    concepto,
                    origen,
                    movimientosReversion,
                    idUsuario
            );

            repository.actualizarConsecutivo(
                    tipoComprobante,
                    idAgencia,
                    nuevoConsecutivo,
                    idUsuario
            );

            // marcar comprobante original como REVERSADO
            repository.marcarOrigenComoReversado(
                    idPeriodoNomina,
                    idUsuario
            );

            // devolver período a CERRADO
            repository.marcarPeriodoComoCerrado(
                    idPeriodoNomina,
                    idUsuario
            );

        }

        repository.marcarPeriodoComoCerrado(idPeriodoNomina, idUsuario);
    }

    public String obtenerComprobanteExistente(Integer idPeriodoNomina) {
        return repository.obtenerComprobanteExistente(idPeriodoNomina);
    }

}