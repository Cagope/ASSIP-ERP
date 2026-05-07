package co.assip.erp.contabilidad.consecutivos_comprobantes;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsecutivosComprobantesService {

    private final ConsecutivosComprobantesRepository repository;

    @Transactional(readOnly = true)
    public String obtenerNumeroSugerido(
            String tipoComprobante,
            Integer idAgencia
    ) {
        validar(tipoComprobante, idAgencia);

        Integer cscActual = repository.obtenerCscActual(
                tipoComprobante,
                idAgencia
        );

        Integer siguiente = (cscActual == null ? 0 : cscActual) + 1;

        return formatear(siguiente);
    }

    @Transactional
    public String generarNumeroDefinitivo(
            String tipoComprobante,
            Integer idAgencia,
            Integer idUsuario
    ) {
        validar(tipoComprobante, idAgencia);

        if (idUsuario == null) {
            throw new RuntimeException("El usuario es obligatorio para generar el comprobante.");
        }

        Integer cscActual = repository.obtenerCscActualConLock(
                tipoComprobante,
                idAgencia
        );

        Integer nuevoCsc = (cscActual == null ? 0 : cscActual) + 1;

        repository.actualizarCsc(
                tipoComprobante,
                idAgencia,
                nuevoCsc,
                idUsuario
        );

        return formatear(nuevoCsc);
    }

    private void validar(String tipoComprobante, Integer idAgencia) {

        if (tipoComprobante == null || tipoComprobante.isBlank()) {
            throw new RuntimeException("El tipo de comprobante es obligatorio.");
        }

        if (idAgencia == null) {
            throw new RuntimeException("La agencia es obligatoria.");
        }
    }

    private String formatear(Integer consecutivo) {
        return String.format("%010d", consecutivo);
    }
}