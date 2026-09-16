package co.assip.erp.cartera.originacion.asociados;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OriginacionAsociadoService {

    private final OriginacionAsociadoRepository repository;


    public OriginacionAsociadoService(
            OriginacionAsociadoRepository repository
    ) {
        this.repository = repository;
    }


    // =========================================================
    // BUSCAR
    // =========================================================

    public List<OriginacionAsociadoDTO> buscar(
            String documento,
            String nombres,
            String primerApellido,
            String segundoApellido,
            Integer idAgencia
    ) {

        validarIdAgencia(
                idAgencia
        );

        String documentoNormalizado =
                normalizar(
                        documento
                );

        String nombresNormalizados =
                normalizar(
                        nombres
                );

        String primerApellidoNormalizado =
                normalizar(
                        primerApellido
                );

        String segundoApellidoNormalizado =
                normalizar(
                        segundoApellido
                );

        if (
                documentoNormalizado.isBlank()
                        && nombresNormalizados.isBlank()
                        && primerApellidoNormalizado.isBlank()
                        && segundoApellidoNormalizado.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Ingrese al menos un criterio de búsqueda."
            );
        }

        return repository.buscar(
                documentoNormalizado,
                nombresNormalizados,
                primerApellidoNormalizado,
                segundoApellidoNormalizado,
                idAgencia
        );
    }


    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public OriginacionAsociadoDTO buscarPorId(
            Integer idDatosPersonal,
            Integer idAgencia
    ) {

        validarIdAgencia(
                idAgencia
        );

        if (
                idDatosPersonal == null
                        || idDatosPersonal <= 0
        ) {

            throw new IllegalArgumentException(
                    "El id de datos personales es obligatorio."
            );
        }

        OriginacionAsociadoDTO asociado =
                repository.buscarPorId(
                        idDatosPersonal,
                        idAgencia
                );

        if (asociado == null) {

            throw new IllegalArgumentException(
                    "No se encontró el asociado indicado."
            );
        }

        return asociado;
    }


    // =========================================================
    // VALIDAR AGENCIA
    // =========================================================

    private void validarIdAgencia(
            Integer idAgencia
    ) {

        if (
                idAgencia == null
                        || idAgencia <= 0
        ) {

            throw new IllegalArgumentException(
                    "La agencia es obligatoria."
            );
        }
    }


    // =========================================================
    // NORMALIZAR
    // =========================================================

    private String normalizar(
            String valor
    ) {

        if (valor == null) {
            return "";
        }

        return valor
                .trim()
                .replaceAll(
                        "\\s+",
                        " "
                );
    }
}