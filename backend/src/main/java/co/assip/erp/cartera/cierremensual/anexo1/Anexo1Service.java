package co.assip.erp.cartera.cierremensual.anexo1;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class Anexo1Service {

    private final Anexo1Repository repository;

    // =========================================================
    // SOLO EDAD CONTABLE
    // =========================================================

    @Transactional
    public int calcularEdadContable(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        validarParametros(
                idCierreCartera,
                idUsuario
        );

        return repository.calcularEdadContablePorArrastre(
                idCierreCartera,
                idUsuario
        );
    }


    // =========================================================
    // PROCESAR ANEXO 1 COMPLETO
    // =========================================================

    @Transactional
    public ResultadoAnexo1 procesarAnexo1(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        validarParametros(
                idCierreCartera,
                idUsuario
        );

        int edades =
                repository.calcularEdadContablePorArrastre(
                        idCierreCartera,
                        idUsuario
                );

        int capital =
                repository.calcularDeterioroCapital(
                        idCierreCartera,
                        idUsuario
                );

        int intereses =
                repository.calcularDeterioroIntereses(
                        idCierreCartera,
                        idUsuario
                );

        return new ResultadoAnexo1(
                edades,
                capital,
                intereses
        );
    }


    // =========================================================
    // VALIDACIÓN
    // =========================================================

    private void validarParametros(
            Integer idCierreCartera,
            Integer idUsuario
    ) {

        if (idCierreCartera == null) {
            throw new IllegalArgumentException(
                    "El id del cierre de cartera es obligatorio."
            );
        }

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El id del usuario es obligatorio."
            );
        }
    }


    // =========================================================
    // RESULTADO
    // =========================================================

    public record ResultadoAnexo1(
            int edadesContables,
            int deteriorosCapital,
            int deteriorosIntereses
    ) {
    }
}