package co.assip.erp.depositos.informes.rangos;

import co.assip.erp.depositos.informes.rangos.dto.RangosFiltroDTO;
import co.assip.erp.depositos.informes.rangos.repository.RangosRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RangosService {

    private final RangosRepository repository;

    public RangosService(RangosRepository repository) {
        this.repository = repository;
    }

    /**
     * 🧠 consultar()
     * ------------------------------------------------------------
     * Valida el request y enruta según tipo:
     *  - EDAD
     *  - SALDO
     *  - ANTIGUEDAD
     * Usando:
     *  - agencia
     *  - fechaCorte
     *  - rangos (4)
     */
    public List<RangosItemDTO> consultar(RangosRequest request) {

        // ============================
        // VALIDACIONES GENERALES
        // ============================

        if (request.getTipo() == null || request.getTipo().isBlank()) {
            throw new IllegalArgumentException("Debe indicar el tipo de informe (EDAD, SALDO, ANTIGUEDAD)");
        }

        if (request.getFechaCorte() == null || request.getFechaCorte().isBlank()) {
            throw new IllegalArgumentException("Debe enviar fechaCorte (YYYY-MM-DD)");
        }

        if (request.getAgencia() == null || request.getAgencia().isBlank()) {
            request.setAgencia("0"); // todas por defecto
        }

        if (request.getRangos() == null || request.getRangos().isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un rango");
        }

        if (request.getRangos().size() != 4) {
            throw new IllegalArgumentException("Debe enviar exactamente 4 rangos");
        }

        for (RangosFiltroDTO r : request.getRangos()) {
            if (r.getDesde() == null || r.getHasta() == null) {
                throw new IllegalArgumentException("Cada rango debe tener valores 'desde' y 'hasta'");
            }
        }

        // Normalizar
        String tipo = request.getTipo().trim().toUpperCase();

        // ============================
        // RUTEO POR TIPO
        // ============================
        switch (tipo) {
            case "EDAD":
                return repository.consultarPorEdad(request);

            case "SALDO":
                return repository.consultarPorSaldo(request);

            case "ANTIGUEDAD":
                return repository.consultarPorAntiguedad(request);

            default:
                throw new IllegalArgumentException("Tipo no válido: " + tipo);
        }
    }
}
