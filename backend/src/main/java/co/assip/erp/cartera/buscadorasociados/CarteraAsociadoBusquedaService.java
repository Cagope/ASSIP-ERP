package co.assip.erp.cartera.buscadorasociados;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio transversal de búsqueda de asociados
 * para los procesos de Cartera.
 */
@Service
@Transactional(readOnly = true)
public class CarteraAsociadoBusquedaService {

    // =========================================================
    // Dependencia
    // =========================================================

    private final CarteraAsociadoBusquedaRepository repository;

    public CarteraAsociadoBusquedaService(
            CarteraAsociadoBusquedaRepository repository
    ) {
        this.repository = repository;
    }

    // =========================================================
    // Búsqueda
    // =========================================================

    public List<CarteraAsociadoBusquedaDTO> buscar(
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido
    ) {

        String documentoNormalizado =
                normalizarTexto(
                        documento
                );

        String nombresNormalizados =
                normalizarTexto(
                        nombres
                );

        String primerApellidoNormalizado =
                normalizarTexto(
                        primerApellido
                );

        String segundoApellidoNormalizado =
                normalizarTexto(
                        segundoApellido
                );

        validarCriterios(
                documentoNormalizado,
                nombresNormalizados,
                primerApellidoNormalizado,
                segundoApellidoNormalizado
        );

        return repository.buscar(
                documentoNormalizado,
                nombresNormalizados,
                primerApellidoNormalizado,
                segundoApellidoNormalizado
        );
    }

    // =========================================================
    // Validaciones
    // =========================================================

    private void validarCriterios(
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido
    ) {

        boolean sinCriterios =
                documento == null
                        && nombres == null
                        && primerApellido == null
                        && segundoApellido == null;

        if (sinCriterios) {

            throw new IllegalArgumentException(
                    "Debe ingresar al menos un criterio de búsqueda."
            );
        }

        /*
         * Cuando se busca por documento se permite desde
         * un carácter porque puede tratarse de documentos
         * alfanuméricos.
         *
         * Para nombres y apellidos se exige mínimo dos
         * caracteres para evitar consultas demasiado amplias.
         */
        if (
                documento == null
                        && longitudMenorQueDos(
                        nombres
                )
                        && longitudMenorQueDos(
                        primerApellido
                )
                        && longitudMenorQueDos(
                        segundoApellido
                )
        ) {

            throw new IllegalArgumentException(
                    "Ingrese al menos dos caracteres en nombres o apellidos."
            );
        }
    }

    private boolean longitudMenorQueDos(
            String valor
    ) {

        return valor == null
                || valor.length() < 2;
    }

    // =========================================================
    // Utilidades
    // =========================================================

    private String normalizarTexto(
            String valor
    ) {

        if (valor == null) {
            return null;
        }

        String normalizado =
                valor
                        .trim()
                        .replaceAll(
                                "\\s+",
                                " "
                        );

        return normalizado.isBlank()
                ? null
                : normalizado;
    }

}