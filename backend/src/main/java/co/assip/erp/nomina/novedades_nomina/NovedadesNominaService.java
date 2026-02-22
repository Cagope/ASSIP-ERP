package co.assip.erp.nomina.novedades_nomina;

import co.assip.erp.nomina.novedades_nomina.dto.NovedadNominaFormDTO;
import co.assip.erp.nomina.novedades_nomina.dto.NovedadNominaListDTO;
import co.assip.erp.nomina.periodos_nomina.PeriodoNominaActivoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class NovedadesNominaService {

    private final NovedadesNominaRepository repo;
    private final NovedadCalculoService calculoService;
    private final PeriodoNominaActivoService periodoActivoService;

    public NovedadesNominaService(
            NovedadesNominaRepository repo,
            NovedadCalculoService calculoService,
            PeriodoNominaActivoService periodoActivoService
    ) {
        this.repo = repo;
        this.calculoService = calculoService;
        this.periodoActivoService = periodoActivoService;
    }

    // =========================================================
    // LISTAR (PERÍODO ACTIVO)
    // =========================================================
    public List<NovedadNominaListDTO> listar(Integer idEmpleado) {
        Integer idPeriodo = periodoActivoService.obtenerPeriodoActivo();
        return repo.listar(idPeriodo, idEmpleado);
    }

    // =========================================================
    // OBTENER
    // =========================================================
    public NovedadNominaFormDTO obtener(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException("ID novedad es obligatorio");
        }

        return repo.obtener(id);
    }

    // =========================================================
    // CREAR (PERÍODO AUTOMÁTICO)
    // =========================================================
    public Integer crear(NovedadNominaFormDTO dto, Integer idUsuario) {

        Integer idPeriodo = periodoActivoService.obtenerPeriodoActivo();
        dto.setIdPeriodo(idPeriodo);

        // 🔥 COMPLETAR FECHAS DESDE PERÍODO ACTIVO
        if (dto.getFechaInicial() == null || dto.getFechaFinal() == null) {

            var periodo = periodoActivoService.obtenerPeriodoActivoDetalle();

            if (periodo == null) {
                throw new IllegalStateException(
                        "No se pudo obtener el período de la novedad"
                );
            }

            dto.setFechaInicial(periodo.getFechaInicio());
            dto.setFechaFinal(periodo.getFechaFin());
        }


        // ✅ AHORA SÍ VALIDAR
        validar(dto);

        validarPeriodoEditable(idPeriodo);

        if (!repo.contratoVigenteEnPeriodo(dto.getIdContrato(), idPeriodo)) {
            throw new IllegalArgumentException(
                    "El contrato no está vigente en el período operativo"
            );
        }

        BigDecimal valorCalculado = calculoService.calcularValor(
                dto.getIdContrato(),
                dto.getCodigoConcepto(),
                dto.getCantidad()
        );

        dto.setValor(
                determinarValorFinal(dto.getValor(), valorCalculado)
        );

        return repo.crear(dto, idUsuario);
    }

    // =========================================================
    // ACTUALIZAR (MISMO PERÍODO DE LA NOVEDAD)
    // =========================================================
    public void actualizar(Integer id, NovedadNominaFormDTO dto, Integer idUsuario) {

        if (id == null) {
            throw new IllegalArgumentException("ID novedad es obligatorio");
        }

        validar(dto);

        Integer idPeriodo = repo.obtenerPeriodoDeNovedad(id);
        if (idPeriodo == null) {
            throw new IllegalArgumentException(
                    "No se encontró el período de la novedad"
            );
        }

        dto.setIdPeriodo(idPeriodo);

        validarPeriodoEditable(idPeriodo);
        validarNovedadEditable(id);

        if (!repo.contratoVigenteEnPeriodo(dto.getIdContrato(), idPeriodo)) {
            throw new IllegalArgumentException(
                    "El contrato no está vigente en el período operativo"
            );
        }

        BigDecimal valorCalculado = calculoService.calcularValor(
                dto.getIdContrato(),
                dto.getCodigoConcepto(),
                dto.getCantidad()
        );

        dto.setValor(
                determinarValorFinal(dto.getValor(), valorCalculado)
        );

        repo.actualizar(id, dto, idUsuario);
    }

    // =========================================================
    // ELIMINAR
    // =========================================================
    public void eliminar(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException("ID novedad es obligatorio");
        }

        Integer idPeriodo = repo.obtenerPeriodoDeNovedad(id);
        if (idPeriodo == null) {
            throw new IllegalArgumentException(
                    "No se encontró el período de la novedad"
            );
        }

        validarPeriodoEditable(idPeriodo);
        validarNovedadEditable(id);

        repo.eliminar(id);
    }

    // =========================================================
    // VALIDACIONES INPUT
    // =========================================================
    private void validar(NovedadNominaFormDTO dto) {

        if (dto == null) throw new IllegalArgumentException("Datos obligatorios");
        if (dto.getIdEmpleado() == null) throw new IllegalArgumentException("Empleado es obligatorio");
        if (dto.getIdContrato() == null) throw new IllegalArgumentException("Contrato es obligatorio");

        if (dto.getCodigoConcepto() == null || dto.getCodigoConcepto().isBlank()) {
            throw new IllegalArgumentException("Concepto es obligatorio");
        }

        if (dto.getFechaFinal().isBefore(dto.getFechaInicial())) {
            throw new IllegalArgumentException("Fecha final inválida");
        }

        if (dto.getCantidad() == null) {
            dto.setCantidad(BigDecimal.ZERO);
        }

        if (dto.getValor() != null && dto.getValor().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor no puede ser negativo");
        }
    }

    // =========================================================
    // BLOQUEO PERÍODO
    // =========================================================
    private void validarPeriodoEditable(Integer idPeriodo) {

        String estado = repo.obtenerEstadoPeriodo(idPeriodo);
        if (estado == null) estado = "";

        String st = estado.trim().toUpperCase();

        if ("ABIERTO".equals(st) || "BORRADOR".equals(st)) return;

        throw new IllegalStateException(
                "El período no permite modificaciones. Estado: " + st
        );
    }

    // =========================================================
    // BLOQUEO NOVEDAD
    // =========================================================
    private void validarNovedadEditable(Integer idNovedad) {

        String estado = repo.obtenerEstadoNovedad(idNovedad);
        if (estado == null) estado = "";

        if ("BORRADOR".equalsIgnoreCase(estado.trim())) return;

        throw new IllegalStateException(
                "La novedad no es editable. Estado: " + estado
        );
    }

    // =========================================================
    // BLINDAJE VALOR ±5%
    // =========================================================
    private BigDecimal determinarValorFinal(
            BigDecimal valorEnviado,
            BigDecimal valorCalculado
    ) {

        if (valorCalculado == null || valorCalculado.compareTo(BigDecimal.ZERO) == 0) {
            return valorEnviado != null ? valorEnviado : BigDecimal.ZERO;
        }

        if (valorEnviado == null) return valorCalculado;

        BigDecimal inf = valorCalculado.multiply(BigDecimal.valueOf(0.95));
        BigDecimal sup = valorCalculado.multiply(BigDecimal.valueOf(1.05));

        if (valorEnviado.compareTo(inf) < 0 || valorEnviado.compareTo(sup) > 0) {
            throw new IllegalArgumentException(
                    "El valor está fuera del rango permitido (±5%)"
            );
        }

        return valorEnviado;
    }
}