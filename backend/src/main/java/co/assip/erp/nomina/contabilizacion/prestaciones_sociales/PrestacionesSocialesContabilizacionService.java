package co.assip.erp.nomina.contabilizacion.prestaciones_sociales;

import co.assip.erp.contabilidad.auxiliares_contables.ContabilidadRegistroService;
import co.assip.erp.contabilidad.auxiliares_contables.dto.MovimientoContableDTO;
import co.assip.erp.contabilidad.origen_comprobantes.dto.OrigenComprobanteDTO;
import co.assip.erp.nomina.contabilizacion.prestaciones_sociales.dto.PrestacionesSocialesComprobanteGeneradoDTO;
import co.assip.erp.nomina.contabilizacion.prestaciones_sociales.dto.PrestacionesSocialesContabilizacionResultadoDTO;
import co.assip.erp.nomina.contabilizacion.liquidacion.dto.LiquidacionMovimientoContableDTO;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrestacionesSocialesContabilizacionService {

    private final PrestacionesSocialesContabilizacionRepository repository;
    private final ContabilidadRegistroService contabilidadRegistroService;
    private final PeriodosNominaRepository periodosNominaRepository;
    private final UsuarioSesionService usuarioSesionService;

    // =========================================================
    // PREVIEW CONTABLE
    // =========================================================
    public List<LiquidacionMovimientoContableDTO> preview(Integer idPeriodoNomina) {

        String comprobanteActivo =
                repository.obtenerComprobanteExistente(idPeriodoNomina);

        if (comprobanteActivo != null) {
            throw new IllegalStateException(
                    "El mes ya fue contabilizado en prestaciones sociales."
            );
        }

        if (!repository.periodosDelMesEstanContabilizados(idPeriodoNomina)) {
            throw new IllegalStateException(
                    "No se puede contabilizar prestaciones sociales porque no todos los períodos del mes están en estado CONTABILIZADO."
            );
        }

        return repository.preview(idPeriodoNomina);
    }

    // =========================================================
    // CONTABILIZAR
    // =========================================================
    @Transactional
    public PrestacionesSocialesContabilizacionResultadoDTO contabilizar(Integer idPeriodoNomina) {

        repository.bloquearPeriodoNomina(idPeriodoNomina);

        String comprobanteActivo =
                repository.obtenerComprobanteExistente(idPeriodoNomina);

        if (comprobanteActivo != null) {
            throw new IllegalStateException(
                    "El mes ya fue contabilizado en prestaciones sociales. Comprobante: " + comprobanteActivo
            );
        }

        if (!repository.periodosDelMesEstanContabilizados(idPeriodoNomina)) {
            throw new IllegalStateException(
                    "No se puede contabilizar prestaciones sociales porque no todos los períodos del mes están en estado CONTABILIZADO."
            );
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        List<Integer> agencias = repository.listarAgenciasConMovimientos(idPeriodoNomina);

        if (agencias.isEmpty()) {
            throw new IllegalStateException(
                    "No existen movimientos de prestaciones sociales para contabilizar en el mes seleccionado."
            );
        }

        List<PrestacionesSocialesComprobanteGeneradoDTO> comprobantes = new ArrayList<>();

        LocalDate fechaComprobante = repository.obtenerFechaComprobante(idPeriodoNomina);

        var periodoLabel = periodosNominaRepository.obtenerPeriodoLabel(idPeriodoNomina);

        if (periodoLabel == null) {
            throw new IllegalStateException(
                    "No se pudo obtener el label del período de nómina " + idPeriodoNomina
            );
        }

        String periodoTexto = String.format(
                "%d-%02d",
                periodoLabel.anio(),
                periodoLabel.mes()
        );

        for (Integer idAgencia : agencias) {

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

            BigDecimal totalDebito = movimientos.stream()
                    .map(m -> m.getDebito() == null ? BigDecimal.ZERO : m.getDebito())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalCredito = movimientos.stream()
                    .map(m -> m.getCredito() == null ? BigDecimal.ZERO : m.getCredito())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalDebito.compareTo(totalCredito) != 0) {
                throw new IllegalStateException(
                        "El comprobante de prestaciones sociales de la agencia " + idAgencia +
                                " no está cuadrado. Débito=" + totalDebito +
                                " Crédito=" + totalCredito
                );
            }

            String tipoComprobante = "NM";

            Integer consecutivoActual =
                    repository.obtenerConsecutivoActualConLock(tipoComprobante, idAgencia);

            Integer nuevoConsecutivo = consecutivoActual + 1;

            String numeroComprobante = String.format("%07d", nuevoConsecutivo);

            String concepto =
                    "Contabilización prestaciones sociales mes " + periodoTexto;

            if (repository.comprobanteExiste(tipoComprobante, numeroComprobante, idAgencia)) {
                throw new IllegalStateException(
                        "El comprobante " + tipoComprobante + "-" + numeroComprobante +
                                " ya existe para la agencia " + idAgencia
                );
            }

            List<MovimientoContableDTO> movimientosContables = new ArrayList<>();

            for (LiquidacionMovimientoContableDTO mov : movimientos) {

                MovimientoContableDTO m = new MovimientoContableDTO();

                m.setIdCatalogoCuenta(mov.getIdCatalogoCuenta());
                m.setIdAgencia(idAgencia);
                m.setIdDatosPersonal(mov.getIdTercero());
                m.setFechaAuxiliar(fechaComprobante);

                m.setDetalleMovimiento(
                        "Prestaciones sociales mes " + periodoTexto + " - " +
                                " - " + (mov.getNombreEmpleadoReferencia() != null
                                ? mov.getNombreEmpleadoReferencia()
                                : "")
                );

                m.setValorDebito(mov.getDebito());
                m.setValorCredito(mov.getCredito());
                m.setValorBase(mov.getValorBase());

                movimientosContables.add(m);
            }

            OrigenComprobanteDTO origen = new OrigenComprobanteDTO();
            origen.setOrigenTipo("AUTO");
            origen.setModuloOrigen("NOMINA");
            origen.setProcesoOrigen("PRESTACIONES_SOCIALES_NOMINA");
            origen.setTablaOrigen("nomina.periodos_nomina");
            origen.setIdOrigen(idPeriodoNomina.longValue());

            contabilidadRegistroService.registrarComprobante(
                    idAgencia,
                    tipoComprobante,
                    numeroComprobante,
                    concepto,
                    origen,
                    movimientosContables,
                    idUsuario
            );

            repository.actualizarConsecutivo(
                    tipoComprobante,
                    idAgencia,
                    nuevoConsecutivo,
                    idUsuario
            );

            comprobantes.add(
                    PrestacionesSocialesComprobanteGeneradoDTO.builder()
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

        return PrestacionesSocialesContabilizacionResultadoDTO.builder()
                .idPeriodoNomina(idPeriodoNomina)
                .cantidadAgencias(comprobantes.size())
                .comprobantes(comprobantes)
                .build();
    }

    // =========================================================
    // REVERSAR CONTABILIZACION
    // =========================================================
    @Transactional
    public void reversarContabilizacion(Integer idPeriodoNomina) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        List<Integer> agencias = repository.listarAgenciasConMovimientos(idPeriodoNomina);

        LocalDate fechaComprobante = repository.obtenerFechaComprobante(idPeriodoNomina);

        var periodoLabel = periodosNominaRepository.obtenerPeriodoLabel(idPeriodoNomina);

        if (periodoLabel == null) {
            throw new IllegalStateException(
                    "No se pudo obtener el label del período de nómina " + idPeriodoNomina
            );
        }

        String periodoTexto = String.format(
                "%d-%02d",
                periodoLabel.anio(),
                periodoLabel.mes()
        );

        for (Integer idAgencia : agencias) {

            String tipoComprobante = "NM";

            String numeroOriginal =
                    repository.obtenerNumeroComprobantePrestaciones(idPeriodoNomina, idAgencia);

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
                        "REVERSIÓN prestaciones sociales nómina mes " + periodoTexto
                );

                m.setValorDebito(mov.getCredito());
                m.setValorCredito(mov.getDebito());
                m.setValorBase(mov.getValorBase());

                movimientosReversion.add(m);
            }

            Integer consecutivoActual =
                    repository.obtenerConsecutivoActualConLock(tipoComprobante, idAgencia);

            Integer nuevoConsecutivo = consecutivoActual + 1;

            String numeroComprobante = String.format("%07d", nuevoConsecutivo);

            String concepto =
                    "REVERSIÓN contabilización prestaciones sociales mes " + periodoTexto;

            OrigenComprobanteDTO origen = new OrigenComprobanteDTO();
            origen.setOrigenTipo("AUTO");
            origen.setModuloOrigen("NOMINA");
            origen.setProcesoOrigen("REVERSO_PRESTACIONES_SOCIALES_NOMINA");
            origen.setTablaOrigen("nomina.periodos_nomina");
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

            repository.marcarOrigenComoReversado(
                    idPeriodoNomina,
                    idUsuario
            );
        }
    }

    public String obtenerComprobanteExistente(Integer idPeriodoNomina) {
        return repository.obtenerComprobanteExistente(idPeriodoNomina);
    }

}