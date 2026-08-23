package co.assip.erp.cartera.cierremensual.anexo2;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class Anexo2Repository {

    private final Anexo2ControlRepository controlRepository;
    private final Anexo2PoblacionRepository poblacionRepository;
    private final Anexo2ModeloRepository modeloRepository;
    private final Anexo2DeterioroRepository deterioroRepository;
    private final Anexo2HomologacionRepository homologacionRepository;
    private final Anexo2ResultadosRepository resultadosRepository;

    public boolean existeCierre(Integer idCierreCartera) {
        return controlRepository.existeCierre(idCierreCartera);
    }

    public Integer iniciarProceso(Integer idCierreCartera, Integer idUsuario) {
        return controlRepository.iniciarProceso(idCierreCartera, idUsuario);
    }

    public int crearPoblacionTemporal(Integer idCierreCartera) {
        return poblacionRepository.crearPoblacionTemporal(idCierreCartera);
    }

    public long crearMorasTemporales(Integer idCierreCartera) {
        return poblacionRepository.crearMorasTemporales(idCierreCartera);
    }

    public Integer cantidadPeriodosMora() {
        return poblacionRepository.cantidadPeriodosMora();
    }

    public Integer maximoPeriodoMora() {
        return poblacionRepository.maximoPeriodoMora();
    }

    public int cantidadCortesHistoricos() {
        return poblacionRepository.cantidadCortesHistoricos();
    }

    public boolean validarCorte40(Integer idCierreCartera) {
        return poblacionRepository.validarCorte40(idCierreCartera);
    }

    public int crearVariablesTemporales() {
        return poblacionRepository.crearVariablesTemporales();
    }

    public java.util.List<java.util.Map<String, Object>> consultarVariablesMuestra() {
        return poblacionRepository.consultarVariablesMuestra();
    }

    public void finalizarProceso(Integer idPeProceso) {
        controlRepository.finalizarProceso(idPeProceso);
    }

    public void marcarProcesoError(Integer idPeProceso) {
        controlRepository.marcarProcesoError(idPeProceso);
    }

    public int calcularZPuntajeCalificacion() {
        return modeloRepository.calcularZPuntajeCalificacion();
    }

    public int calcularDefaultEdadDeterioroYPi() {
        return modeloRepository.calcularDefaultEdadDeterioroYPi();
    }

    public int calcularVea(Integer idCierreCartera) {
        return deterioroRepository.calcularVea(idCierreCartera);
    }

    public int calcularPdiYPerdida() {
        return deterioroRepository.calcularPdiYPerdida();
    }

    public int calcularHomologacionYAlineacion() {
        return homologacionRepository.calcularHomologacionYAlineacion();
    }

    public int persistirResultadosPe(Integer idCierreCartera, Integer idUsuario) {
        return resultadosRepository.persistirResultadosPe(idCierreCartera, idUsuario);
    }

}
