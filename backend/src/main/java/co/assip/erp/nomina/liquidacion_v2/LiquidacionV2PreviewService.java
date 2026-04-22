package co.assip.erp.nomina.liquidacion_v2;

import co.assip.erp.nomina.empleado_contratos.dto.EmpleadoContratoDTO;
import co.assip.erp.nomina.liquidacion.dto.LiquidacionDetalleDTO;
import co.assip.erp.nomina.liquidacion_v2.calculo.DeduccionesV2Calculator;
import co.assip.erp.nomina.liquidacion_v2.calculo.DevengadosV2Calculator;
import co.assip.erp.nomina.liquidacion_v2.calculo.IbcV2Calculator;
import co.assip.erp.nomina.liquidacion_v2.calculo.ResumenTiempoCalculator;
import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2DetalleDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2PreviewItemDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2PreviewResponseDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.LiquidacionV2RequestDTO;
import co.assip.erp.nomina.liquidacion_v2.dto.ResumenTiempoDTO;
import co.assip.erp.nomina.novedades_nomina.NovedadesNominaRepository;
import co.assip.erp.nomina.periodos_nomina.PeriodosNominaRepository;
import co.assip.erp.seguridad.repository.UsuarioAgenciaRepository;
import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LiquidacionV2PreviewService {

    private final PeriodosNominaRepository periodosRepo;
    private final UsuarioAgenciaRepository usuarioAgenciaRepo;
    private final LiquidacionV2Repository liquidacionRepo;
    private final NovedadesNominaRepository novedadesRepo;
    private final NamedParameterJdbcTemplate jdbc;

    private final ResumenTiempoCalculator resumenTiempoCalculator;
    private final DevengadosV2Calculator devengadosCalculator;
    private final IbcV2Calculator ibcV2Calculator;
    private final DeduccionesV2Calculator deduccionesV2Calculator;

    public PeriodosNominaRepository.PeriodoLabel obtenerPrimerPeriodoDisponible(Integer fkAgencia) {

        validarAgencia(fkAgencia);

        Integer idPeriodo = liquidacionRepo.obtenerPeriodoOperativo(fkAgencia);

        if (idPeriodo == null) {
            return null;
        }

        return periodosRepo.obtenerPeriodoLabel(idPeriodo);
    }

    public LiquidacionV2PreviewResponseDTO preview(LiquidacionV2RequestDTO request) {

        Integer idPeriodo = request.getIdPeriodoNomina();

        if (idPeriodo == null) {
            idPeriodo = liquidacionRepo.obtenerPeriodoOperativo(request.getFkAgencia());
        }

        if (idPeriodo == null) {
            throw new IllegalStateException(
                    "No existe un período de nómina ABIERTO para la agencia"
            );
        }

        validarAgencia(request.getFkAgencia());
        periodosRepo.validarPeriodoAbierto(idPeriodo, request.getFkAgencia());

        List<EmpleadoContratoDTO> contratos =
                liquidacionRepo.obtenerContratosParaLiquidacion(
                        idPeriodo,
                        request.getFkAgencia()
                );

        if (contratos.isEmpty()) {
            throw new IllegalStateException(
                    "No existen contratos activos para liquidar"
            );
        }

        List<LiquidacionV2PreviewItemDTO> items = new ArrayList<>();

        BigDecimal totalDev = BigDecimal.ZERO;
        BigDecimal totalDed = BigDecimal.ZERO;
        BigDecimal totalProv = BigDecimal.ZERO;
        BigDecimal totalNeto = BigDecimal.ZERO;

        for (EmpleadoContratoDTO contrato : contratos) {

            novedadesRepo.eliminarNovedadesPreviewPorPeriodoYContrato(
                    idPeriodo,
                    contrato.getIdContrato()
            );

            generarNovedadesDesdeEventos(idPeriodo, contrato);

            ResumenTiempoDTO resumenTiempo = resumenTiempoCalculator.calcular(
                    idPeriodo,
                    contrato
            );

            List<LiquidacionDetalleDTO> devengados = devengadosCalculator.calcular(
                    idPeriodo,
                    contrato
            );

            BigDecimal ibc = ibcV2Calculator.calcular(devengados);

            List<LiquidacionDetalleDTO> deducciones = deduccionesV2Calculator.calcular(
                    idPeriodo,
                    contrato,
                    ibc
            );

            List<LiquidacionDetalleDTO> detalleCompleto = new ArrayList<>();
            detalleCompleto.addAll(devengados);
            detalleCompleto.addAll(deducciones);

            BigDecimal subtotalDev = sumarPorTipo(detalleCompleto, "DEVENGADO");
            BigDecimal subtotalDed = sumarPorTipo(detalleCompleto, "DEDUCCION");
            BigDecimal subtotalProv = BigDecimal.ZERO;
            BigDecimal neto = subtotalDev.subtract(subtotalDed);

            totalDev = totalDev.add(subtotalDev);
            totalDed = totalDed.add(subtotalDed);
            totalProv = totalProv.add(subtotalProv);
            totalNeto = totalNeto.add(neto);

            items.add(
                    LiquidacionV2PreviewItemDTO.builder()
                            .idContrato(contrato.getIdContrato())
                            .idEmpleado(contrato.getIdEmpleado())
                            .documentoEmpleado(contrato.getDocumentoEmpleado())
                            .nombreEmpleado(contrato.getNombreEmpleado())
                            .salarioBase(contrato.getSalarioBase())
                            .diasLaborados(resumenTiempo.getDiasLaboradosSafe())
                            .ibc(ibc)
                            .totalDevengados(subtotalDev)
                            .totalDeducciones(subtotalDed)
                            .totalProvisiones(subtotalProv)
                            .netoPagar(neto)
                            .detalle(mapearDetalle(detalleCompleto))
                            .build()
            );
        }

        return LiquidacionV2PreviewResponseDTO.builder()
                .idPeriodoNomina(idPeriodo)
                .fkAgencia(request.getFkAgencia())
                .totalContratos(items.size())
                .totalDevengados(totalDev)
                .totalDeducciones(totalDed)
                .totalProvisiones(totalProv)
                .totalNetoPagar(totalNeto)
                .items(items)
                .build();
    }

    private void generarNovedadesDesdeEventos(
            Integer idPeriodo,
            EmpleadoContratoDTO contrato
    ) {

        final Integer idUsuarioFinal =
                SecurityUtils.getIdUsuario() != null ? SecurityUtils.getIdUsuario() : 1;

        PeriodoRango periodo = obtenerRangoPeriodo(idPeriodo);
        if (periodo == null) {
            return;
        }

        String sqlEventos = """
        SELECT
            e.id_evento_liquidacion,
            e.tipo_evento,
            e.fecha_inicio,
            e.fecha_fin,
            e.total_dias,
            e.responsable_pago,
            e.porcentaje_responsable,
            e.porcentaje_empresa,
            e.dias_empresa_100
        FROM nomina.eventos_liquidacion e
        WHERE e.id_contrato = :idContrato
          AND e.estado = 'ACTIVO'
          AND e.fecha_inicio <= :fechaFinPeriodo
          AND e.fecha_fin >= :fechaInicioPeriodo
        ORDER BY e.fecha_inicio, e.id_evento_liquidacion
        """;

        MapSqlParameterSource paramsEventos = new MapSqlParameterSource()
                .addValue("idContrato", contrato.getIdContrato())
                .addValue("fechaInicioPeriodo", periodo.fechaInicio())
                .addValue("fechaFinPeriodo", periodo.fechaFin());

        jdbc.query(sqlEventos, paramsEventos, rsEvento -> {

            String tipoEvento = rsEvento.getString("tipo_evento");

            LocalDate fechaInicioEvento = rsEvento.getDate("fecha_inicio").toLocalDate();
            LocalDate fechaFinEvento = rsEvento.getDate("fecha_fin").toLocalDate();

            LocalDate inicioTramo = fechaInicioEvento.isAfter(periodo.fechaInicio())
                    ? fechaInicioEvento
                    : periodo.fechaInicio();

            LocalDate finTramo = fechaFinEvento.isBefore(periodo.fechaFin())
                    ? fechaFinEvento
                    : periodo.fechaFin();

            if (finTramo.isBefore(inicioTramo)) {
                return;
            }

            final BigDecimal diasTramo = BigDecimal.valueOf(
                    ChronoUnit.DAYS.between(inicioTramo, finTramo) + 1
            );

            if (diasTramo.compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }

            BigDecimal diasEmpresa100Tmp = rsEvento.getBigDecimal("dias_empresa_100");
            final BigDecimal diasEmpresa100Final =
                    (diasEmpresa100Tmp == null || diasEmpresa100Tmp.compareTo(BigDecimal.ZERO) < 0)
                            ? BigDecimal.valueOf(2)
                            : diasEmpresa100Tmp;

            final BigDecimal porcentajeResponsable =
                    rsEvento.getBigDecimal("porcentaje_responsable");

            final BigDecimal porcentajeEmpresa =
                    rsEvento.getBigDecimal("porcentaje_empresa");

            String sqlReglas = """
            SELECT
                r.codigo_concepto,
                r.orden,
                r.tipo_dias,
                r.fuente_porcentaje,
                r.porcentaje_fijo
            FROM nomina.eventos_liquidacion_conceptos r
            WHERE r.tipo_evento = :tipoEvento
              AND r.activo = true
            ORDER BY r.orden
            """;

            MapSqlParameterSource paramsReglas = new MapSqlParameterSource()
                    .addValue("tipoEvento", tipoEvento);

            jdbc.query(sqlReglas, paramsReglas, rsRegla -> {

                String codigoConcepto = rsRegla.getString("codigo_concepto");
                String tipoDias = rsRegla.getString("tipo_dias");
                String fuentePorcentaje = rsRegla.getString("fuente_porcentaje");
                BigDecimal porcentajeFijo = rsRegla.getBigDecimal("porcentaje_fijo");

                BigDecimal diasRegla = calcularDiasSegunRegla(
                        tipoDias,
                        diasTramo,
                        diasEmpresa100Final
                );

                if (diasRegla.compareTo(BigDecimal.ZERO) <= 0) {
                    return;
                }

                BigDecimal porcentajeAplicado = obtenerPorcentajeAplicado(
                        fuentePorcentaje,
                        porcentajeFijo,
                        porcentajeResponsable,
                        porcentajeEmpresa
                );

                if (!novedadesRepo.existeNovedad(idPeriodo, contrato.getIdContrato(), codigoConcepto)) {
                    novedadesRepo.insertarNovedadPreview(
                            idPeriodo,
                            contrato.getIdEmpleado(),
                            contrato.getIdContrato(),
                            codigoConcepto,
                            inicioTramo,
                            finTramo,
                            diasRegla,
                            BigDecimal.ZERO,
                            idUsuarioFinal
                    );
                }
            });
        });
    }

    private BigDecimal calcularDiasSegunRegla(
            String tipoDias,
            BigDecimal diasTramo,
            BigDecimal diasEmpresa100
    ) {
        if (tipoDias == null) {
            return BigDecimal.ZERO;
        }

        return switch (tipoDias.trim().toUpperCase()) {
            case "TRAMO" -> diasTramo;
            case "PRIMEROS_DIAS" -> diasTramo.min(diasEmpresa100);
            case "RESTO_DIAS" -> {
                BigDecimal resto = diasTramo.subtract(diasEmpresa100);
                yield resto.compareTo(BigDecimal.ZERO) > 0 ? resto : BigDecimal.ZERO;
            }
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal obtenerPorcentajeAplicado(
            String fuentePorcentaje,
            BigDecimal porcentajeFijo,
            BigDecimal porcentajeResponsable,
            BigDecimal porcentajeEmpresa
    ) {
        if (fuentePorcentaje == null) {
            return BigDecimal.ZERO;
        }

        return switch (fuentePorcentaje.trim().toUpperCase()) {
            case "FIJO" -> porcentajeFijo != null ? porcentajeFijo : BigDecimal.valueOf(100);
            case "RESPONSABLE" -> porcentajeResponsable != null ? porcentajeResponsable : BigDecimal.ZERO;
            case "EMPRESA" -> porcentajeEmpresa != null ? porcentajeEmpresa : BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    private PeriodoRango obtenerRangoPeriodo(Integer idPeriodo) {

        String sql = """
            SELECT
                p.fecha_inicio,
                p.fecha_fin
            FROM nomina.periodos_nomina p
            WHERE p.id_periodo = :idPeriodo
            """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource("idPeriodo", idPeriodo),
                rs -> {
                    if (!rs.next()) return null;

                    return new PeriodoRango(
                            rs.getDate("fecha_inicio").toLocalDate(),
                            rs.getDate("fecha_fin").toLocalDate()
                    );
                }
        );
    }

    private void validarAgencia(Integer fkAgencia) {
        Integer idUsuario = SecurityUtils.getIdUsuario();

        if (!usuarioAgenciaRepo.existsByIdUsuarioAndIdAgencia(idUsuario, fkAgencia)) {
            throw new IllegalStateException(
                    "El usuario no tiene acceso a la agencia"
            );
        }
    }

    private BigDecimal sumarPorTipo(List<LiquidacionDetalleDTO> detalle, String tipo) {
        BigDecimal total = BigDecimal.ZERO;

        for (LiquidacionDetalleDTO d : detalle) {
            if (tipo.equalsIgnoreCase(d.getTipo())) {
                total = total.add(
                        d.getValorTotal() != null ? d.getValorTotal() : BigDecimal.ZERO
                );
            }
        }

        return total;
    }

    private List<LiquidacionV2DetalleDTO> mapearDetalle(List<LiquidacionDetalleDTO> detalle) {
        List<LiquidacionV2DetalleDTO> out = new ArrayList<>();

        for (LiquidacionDetalleDTO d : detalle) {
            out.add(
                    LiquidacionV2DetalleDTO.builder()
                            .tipo(d.getTipo())
                            .codigoConcepto(d.getCodigoConcepto())
                            .cantidad(d.getCantidad())
                            .valorUnitario(d.getValorUnitario())
                            .valorTotal(d.getValorTotal())
                            .baseCalculo(d.getBaseCalculo())
                            .origen(d.getOrigen())
                            .idNovedad(d.getIdNovedadNomina())
                            .build()
            );
        }

        return out;
    }

    private record PeriodoRango(
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {}
}