package co.assip.erp.nomina.eventos_liquidacion;

import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionFormDTO;
import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionListDTO;
import co.assip.erp.nomina.eventos_liquidacion.dto.EventoLiquidacionSaveDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventosLiquidacionService {

    private final EventosLiquidacionRepository repository;

    public List<EventoLiquidacionListDTO> listar() {
        return repository.listar();
    }

    public EventoLiquidacionFormDTO obtenerPorId(Long idEventoLiquidacion) {
        return repository.obtenerPorId(idEventoLiquidacion)
                .orElseThrow(() -> new RuntimeException("No se encontró el evento de liquidación."));
    }

    public Long guardar(EventoLiquidacionSaveDTO dto, Integer idUsuario) {
        validar(dto);

        if (dto.getIdEventoLiquidacion() == null) {
            return repository.crear(dto, idUsuario);
        }

        validarPeriodoAbiertoPorEvento(dto.getIdEventoLiquidacion());
        repository.actualizar(dto, idUsuario);
        return dto.getIdEventoLiquidacion();
    }

    public void eliminar(Long idEventoLiquidacion, Integer idUsuario) {

        validarPeriodoAbiertoPorEvento(idEventoLiquidacion);

        Integer idPeriodo = repository.obtenerPeriodoQueCruzaEvento(idEventoLiquidacion);
        Integer idContrato = repository.obtenerContratoPorEvento(idEventoLiquidacion);

        if (idPeriodo != null && idContrato != null) {
            repository.eliminarNovedadesPreviewPorPeriodoYContrato(idPeriodo, idContrato);
        }

        repository.eliminar(idEventoLiquidacion);
    }

    private void validarPeriodoAbiertoPorEvento(Long idEventoLiquidacion) {
        String estadoPeriodo = repository.obtenerEstadoPeriodoDelEvento(idEventoLiquidacion);

        if (estadoPeriodo == null) {
            throw new RuntimeException("No se pudo determinar el período del evento.");
        }

        if (!"ABIERTO".equalsIgnoreCase(estadoPeriodo)) {
            throw new RuntimeException("El evento pertenece a un período cerrado y no puede modificarse.");
        }
    }

    private void validar(EventoLiquidacionSaveDTO dto) {
        if (dto.getIdContrato() == null) {
            throw new RuntimeException("El contrato es obligatorio.");
        }
        if (dto.getFechaDocumento() == null) {
            throw new RuntimeException("La fecha del documento es obligatoria.");
        }
        if (dto.getFechaInicio() == null || dto.getFechaFin() == null) {
            throw new RuntimeException("Las fechas de inicio y fin son obligatorias.");
        }
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new RuntimeException("La fecha fin no puede ser menor que la fecha inicio.");
        }
        if (dto.getTotalDias() == null || dto.getTotalDias() <= 0) {
            long dias = ChronoUnit.DAYS.between(dto.getFechaInicio(), dto.getFechaFin()) + 1;
            dto.setTotalDias((int) dias);
        }
        if (dto.getTipoEvento() == null || dto.getTipoEvento().isBlank()) {
            throw new RuntimeException("El tipo de evento es obligatorio.");
        }
        if (dto.getResponsablePago() == null || dto.getResponsablePago().isBlank()) {
            dto.setResponsablePago("EMPRESA");
        }
        if (dto.getPorcentajeResponsable() == null) {
            dto.setPorcentajeResponsable(100.0);
        }
        if (dto.getPorcentajeEmpresa() == null) {
            dto.setPorcentajeEmpresa(0.0);
        }
        if (dto.getDiasEmpresa100() == null) {
            dto.setDiasEmpresa100(0);
        }
        if (dto.getGeneraCxc() == null) {
            dto.setGeneraCxc(false);
        }
        if (dto.getLiquidaArl() == null) {
            dto.setLiquidaArl(true);
        }
        if (dto.getEsRemunerado() == null) {
            dto.setEsRemunerado(true);
        }
        if (dto.getEstado() == null || dto.getEstado().isBlank()) {
            dto.setEstado("ACTIVO");
        }
    }
}