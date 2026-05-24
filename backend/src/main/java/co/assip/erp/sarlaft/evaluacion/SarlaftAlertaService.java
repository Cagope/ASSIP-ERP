package co.assip.erp.sarlaft.evaluacion;

import co.assip.erp.sarlaft.alertas.AlertaRepository;
import co.assip.erp.sarlaft.domain.Alerta;
import co.assip.erp.sarlaft.evaluacion.dto.EvaluacionResultado;
import co.assip.erp.sarlaft.evaluacion.dto.ReglasInput;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class SarlaftAlertaService {

    private final AlertaEngine engine;
    private final AlertaRepository alertaRepository;
    private final SarlaftEvaluacionRepository evaluacionRepository;

    public SarlaftAlertaService(
            AlertaEngine engine,
            AlertaRepository alertaRepository,
            SarlaftEvaluacionRepository evaluacionRepository
    ) {
        this.engine = engine;
        this.alertaRepository = alertaRepository;
        this.evaluacionRepository = evaluacionRepository;
    }

    public EvaluacionResultado evaluarOperacion(ReglasInput input) {

        if (input == null) {
            input = new ReglasInput();
        }

        evaluacionRepository.completarDatosPersona(input);

        EvaluacionResultado resultado = engine.evaluar(input);

        if (
                !resultado.isAlerta()
                        || resultado.getAlertas() == null
                        || resultado.getAlertas().isEmpty()
        ) {
            return resultado;
        }

        for (var alertaResultado : resultado.getAlertas()) {

            Alerta alerta = new Alerta();

            alerta.setIdDatosPersonal(input.getIdDatosPersonal());
            alerta.setIdAgencia(input.getIdAgencia());
            alerta.setCodigoModulo(input.getCodigoModulo());
            alerta.setSeveridad(alertaResultado.getSeveridad());
            alerta.setDescripcion(alertaResultado.getDescripcion());
            alerta.setAccionSistema("Evaluación SARLAFT");
            alerta.setCodigoRegla(alertaResultado.getNombreRegla());
            alerta.setFkSeguridadCreacion(input.getIdUsuario());
            alerta.setFkSeguridadEdicion(input.getIdUsuario());

            Alerta guardada = alertaRepository.save(alerta);

            alertaResultado.setIdAlerta(guardada.getIdAlerta());
        }

        resultado.setDescripcion("Evaluación SARLAFT con alertas.");
        resultado.setBloqueaOperacion(false);

        return resultado;
    }

    public EvaluacionResultado evaluarOperacionPreview(ReglasInput input) {

        if (input == null) {
            input = new ReglasInput();
        }

        evaluacionRepository.completarDatosPersona(input);

        EvaluacionResultado resultado =
                engine.evaluar(input);

        resultado.setBloqueaOperacion(false);

        return resultado;
    }

    public EvaluacionResultado evaluarOperacionBasica(
            Long idDatosPersonal,
            Integer idAgencia,
            String codigoModulo,
            String accion,
            BigDecimal monto,
            String codigoFormaAhorro,
            Integer idUsuario
    ) {

        ReglasInput input = new ReglasInput();

        input.setIdDatosPersonal(idDatosPersonal);
        input.setIdAgencia(idAgencia);
        input.setIdUsuario(idUsuario);
        input.setCodigoModulo(codigoModulo);
        input.setAccion(accion);
        input.setCodigoFormaAhorro(codigoFormaAhorro);

        if (monto != null) {
            input.setMonto(monto.doubleValue());
        }

        return evaluarOperacion(input);
    }
}