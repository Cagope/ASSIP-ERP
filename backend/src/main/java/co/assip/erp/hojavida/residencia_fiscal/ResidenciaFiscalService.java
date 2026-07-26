package co.assip.erp.hojavida.residencia_fiscal;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ResidenciaFiscalService {

    private final ResidenciaFiscalRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public ResidenciaFiscalService(
            ResidenciaFiscalRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    @Transactional(readOnly = true)
    public List<ResidenciaFiscal> listar() {
        return repository.findAllByOrderByFechaEdicionDesc();
    }

    @Transactional(readOnly = true)
    public Optional<ResidenciaFiscal> buscarPorId(
            Long idResidenciaFiscal
    ) {
        validarIdResidenciaFiscal(idResidenciaFiscal);

        return repository.findById(idResidenciaFiscal);
    }

    @Transactional(readOnly = true)
    public Optional<ResidenciaFiscal> buscarPorPersona(
            Integer idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return repository.findByIdDatosPersonal(idDatosPersonal);
    }

    // ============================================================
    // CREAR
    // ============================================================

    public ResidenciaFiscal crear(
            ResidenciaFiscal nuevo
    ) {
        if (nuevo == null) {
            throw new IllegalArgumentException(
                    "Debe proporcionar la información de residencia fiscal."
            );
        }

        validarYNormalizar(nuevo);

        Integer idDatosPersonal =
                nuevo.getIdDatosPersonal();

        if (
                repository
                        .findByIdDatosPersonal(idDatosPersonal)
                        .isPresent()
        ) {
            throw new IllegalArgumentException(
                    "La persona ya tiene información de residencia fiscal registrada."
            );
        }

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        LocalDateTime ahora =
                LocalDateTime.now();

        nuevo.setIdResidenciaFiscal(null);

        nuevo.setFechaCreacion(
                Timestamp.valueOf(ahora)
        );

        nuevo.setFechaEdicion(
                Timestamp.valueOf(ahora)
        );

        // 🔐 Auditoría
        nuevo.setFkSeguridadCreacion(idUsuario);
        nuevo.setFkSeguridadEdicion(idUsuario);

        return repository.save(nuevo);
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================

    public Optional<ResidenciaFiscal> actualizar(
            Long idResidenciaFiscal,
            ResidenciaFiscal actualizado
    ) {
        validarIdResidenciaFiscal(idResidenciaFiscal);

        if (actualizado == null) {
            throw new IllegalArgumentException(
                    "Debe proporcionar la información de residencia fiscal."
            );
        }

        return repository
                .findById(idResidenciaFiscal)
                .map(existente -> {

                    validarYNormalizar(actualizado);

                    Integer idDatosPersonalActualizado =
                            actualizado.getIdDatosPersonal();

                    repository
                            .findByIdDatosPersonal(
                                    idDatosPersonalActualizado
                            )
                            .filter(registro ->
                                    !registro
                                            .getIdResidenciaFiscal()
                                            .equals(idResidenciaFiscal)
                            )
                            .ifPresent(registro -> {
                                throw new IllegalArgumentException(
                                        "La persona ya tiene información de residencia fiscal registrada."
                                );
                            });

                    Integer idUsuario =
                            usuarioSesionService.idUsuario();

                    LocalDateTime ahora =
                            LocalDateTime.now();

                    actualizado.setIdResidenciaFiscal(
                            idResidenciaFiscal
                    );

                    actualizado.setFechaCreacion(
                            existente.getFechaCreacion()
                    );

                    actualizado.setFechaEdicion(
                            Timestamp.valueOf(ahora)
                    );

                    // 🔐 Auditoría
                    actualizado.setFkSeguridadCreacion(
                            existente.getFkSeguridadCreacion()
                    );

                    actualizado.setFkSeguridadEdicion(
                            idUsuario
                    );

                    return repository.save(actualizado);
                });
    }

    // ============================================================
    // ELIMINAR
    // ============================================================

    public boolean eliminar(
            Long idResidenciaFiscal
    ) {
        validarIdResidenciaFiscal(idResidenciaFiscal);

        if (!repository.existsById(idResidenciaFiscal)) {
            return false;
        }

        repository.deleteById(idResidenciaFiscal);

        return true;
    }

    // ============================================================
    // VALIDACIONES Y NORMALIZACIÓN
    // ============================================================

    private void validarYNormalizar(
            ResidenciaFiscal residenciaFiscal
    ) {
        validarIdDatosPersonal(
                residenciaFiscal.getIdDatosPersonal()
        );

        normalizarBooleanos(residenciaFiscal);
        normalizarTextos(residenciaFiscal);
        aplicarReglasResidenciaFiscal(residenciaFiscal);
        validarCamposCondicionales(residenciaFiscal);
    }

    private void normalizarBooleanos(
            ResidenciaFiscal residenciaFiscal
    ) {
        if (
                residenciaFiscal.getCiudadanoEstadosUnidos()
                        == null
        ) {
            residenciaFiscal.setCiudadanoEstadosUnidos(false);
        }

        if (
                residenciaFiscal
                        .getResidenteFiscalEstadosUnidos()
                        == null
        ) {
            residenciaFiscal.setResidenteFiscalEstadosUnidos(
                    false
            );
        }

        if (
                residenciaFiscal.getResidenteFiscalExterior()
                        == null
        ) {
            residenciaFiscal.setResidenteFiscalExterior(false);
        }
    }

    private void normalizarTextos(
            ResidenciaFiscal residenciaFiscal
    ) {
        residenciaFiscal.setPaisResidenciaFiscal(
                limpiarTexto(
                        residenciaFiscal.getPaisResidenciaFiscal()
                )
        );

        residenciaFiscal.setCiudadResidenciaFiscal(
                limpiarTexto(
                        residenciaFiscal.getCiudadResidenciaFiscal()
                )
        );

        residenciaFiscal.setDireccionResidenciaFiscal(
                limpiarTexto(
                        residenciaFiscal
                                .getDireccionResidenciaFiscal()
                )
        );

        residenciaFiscal.setTipoIdentificacionFiscal(
                limpiarTexto(
                        residenciaFiscal
                                .getTipoIdentificacionFiscal()
                )
        );

        residenciaFiscal.setNumeroIdentificacionFiscal(
                limpiarTexto(
                        residenciaFiscal
                                .getNumeroIdentificacionFiscal()
                )
        );

        residenciaFiscal.setObservaciones(
                limpiarTexto(
                        residenciaFiscal.getObservaciones()
                )
        );
    }

    private void aplicarReglasResidenciaFiscal(
            ResidenciaFiscal residenciaFiscal
    ) {
        boolean residenteEstadosUnidos =
                Boolean.TRUE.equals(
                        residenciaFiscal
                                .getResidenteFiscalEstadosUnidos()
                );

        boolean residenteExterior =
                Boolean.TRUE.equals(
                        residenciaFiscal
                                .getResidenteFiscalExterior()
                );

        /*
         * Si no existe residencia fiscal fuera de Colombia,
         * no deben conservarse datos de ubicación fiscal exterior.
         */
        if (!residenteExterior && !residenteEstadosUnidos) {
            residenciaFiscal.setPaisResidenciaFiscal(null);
            residenciaFiscal.setCiudadResidenciaFiscal(null);
            residenciaFiscal.setDireccionResidenciaFiscal(null);
            residenciaFiscal.setTipoIdentificacionFiscal(null);
            residenciaFiscal.setNumeroIdentificacionFiscal(null);
        }
    }

    private void validarCamposCondicionales(
            ResidenciaFiscal residenciaFiscal
    ) {
        boolean residenteEstadosUnidos =
                Boolean.TRUE.equals(
                        residenciaFiscal
                                .getResidenteFiscalEstadosUnidos()
                );

        boolean residenteExterior =
                Boolean.TRUE.equals(
                        residenciaFiscal
                                .getResidenteFiscalExterior()
                );

        if (
                residenteExterior &&
                        estaVacio(
                                residenciaFiscal.getPaisResidenciaFiscal()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Debe indicar el país de residencia fiscal."
            );
        }

        if (
                residenteEstadosUnidos &&
                        estaVacio(
                                residenciaFiscal
                                        .getTipoIdentificacionFiscal()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Debe indicar el tipo de identificación fiscal."
            );
        }

        if (
                residenteEstadosUnidos &&
                        estaVacio(
                                residenciaFiscal
                                        .getNumeroIdentificacionFiscal()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Debe indicar el número de identificación fiscal."
            );
        }
    }

    private void validarIdResidenciaFiscal(
            Long idResidenciaFiscal
    ) {
        if (
                idResidenciaFiscal == null ||
                        idResidenciaFiscal <= 0
        ) {
            throw new IllegalArgumentException(
                    "El identificador de residencia fiscal no es válido."
            );
        }
    }

    private void validarIdDatosPersonal(
            Integer idDatosPersonal
    ) {
        if (
                idDatosPersonal == null ||
                        idDatosPersonal <= 0
        ) {
            throw new IllegalArgumentException(
                    "Debe especificar la persona asociada a la residencia fiscal."
            );
        }
    }

    private String limpiarTexto(
            String valor
    ) {
        if (valor == null) {
            return null;
        }

        String texto =
                valor.trim();

        return texto.isEmpty()
                ? null
                : texto;
    }

    private boolean estaVacio(
            String valor
    ) {
        return valor == null ||
                valor.trim().isEmpty();
    }
}