package co.assip.erp.hojavida.condicionesproteccion;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio — Condiciones de Protección
 * ------------------------------------------------------------
 * Gestiona las reglas de negocio y auditoría del proceso.
 *
 * Regla principal:
 * Una persona solamente puede tener un registro de
 * condiciones de protección.
 */
@Service
@Transactional
public class CondicionProteccionService {

    private final CondicionProteccionRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public CondicionProteccionService(
            CondicionProteccionRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    /**
     * Lista todos los registros.
     */
    @Transactional(readOnly = true)
    public List<CondicionProteccion> listar() {
        return repository.findAll();
    }

    /**
     * Busca un registro por su identificador.
     */
    @Transactional(readOnly = true)
    public Optional<CondicionProteccion> buscarPorId(
            Long idCondicionProteccion
    ) {
        validarIdCondicionProteccion(idCondicionProteccion);

        return repository.findById(idCondicionProteccion);
    }

    /**
     * Busca las condiciones de protección de una persona.
     */
    @Transactional(readOnly = true)
    public Optional<CondicionProteccion> buscarPorPersona(
            Integer idDatosPersonal
    ) {
        validarIdDatosPersonal(idDatosPersonal);

        return repository.findByIdDatosPersonal(idDatosPersonal);
    }

    /**
     * Crea un nuevo registro.
     */
    public CondicionProteccion crear(
            CondicionProteccion condicion
    ) {
        validarObjeto(condicion);
        validarIdDatosPersonal(condicion.getIdDatosPersonal());
        normalizar(condicion);

        if (
                repository.existsByIdDatosPersonal(
                        condicion.getIdDatosPersonal()
                )
        ) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La persona ya tiene un registro de condiciones de protección."
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();
        LocalDateTime ahora = LocalDateTime.now();

        condicion.setIdCondicionProteccion(null);

        condicion.setFkSeguridadCreacion(idUsuario);
        condicion.setFechaCreacion(ahora);

        condicion.setFkSeguridadEdicion(idUsuario);
        condicion.setFechaEdicion(ahora);

        return repository.save(condicion);
    }

    /**
     * Actualiza un registro existente.
     */
    public CondicionProteccion actualizar(
            Long idCondicionProteccion,
            CondicionProteccion datos
    ) {
        validarIdCondicionProteccion(idCondicionProteccion);
        validarObjeto(datos);
        validarIdDatosPersonal(datos.getIdDatosPersonal());
        normalizar(datos);

        CondicionProteccion actual = repository
                .findById(idCondicionProteccion)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el registro de condiciones de protección."
                ));

        boolean existeOtraPersonaDuplicada =
                repository
                        .existsByIdDatosPersonalAndIdCondicionProteccionNot(
                                datos.getIdDatosPersonal(),
                                idCondicionProteccion
                        );

        if (existeOtraPersonaDuplicada) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La persona ya tiene otro registro de condiciones de protección."
            );
        }

        copiarCamposEditables(actual, datos);

        actual.setFkSeguridadEdicion(
                usuarioSesionService.idUsuario()
        );

        actual.setFechaEdicion(
                LocalDateTime.now()
        );

        return repository.save(actual);
    }

    /**
     * Elimina un registro.
     */
    public void eliminar(
            Long idCondicionProteccion
    ) {
        validarIdCondicionProteccion(idCondicionProteccion);

        CondicionProteccion actual = repository
                .findById(idCondicionProteccion)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró el registro de condiciones de protección."
                ));

        repository.delete(actual);
    }

    /**
     * Copia solamente los campos que el usuario puede editar.
     *
     * Los campos de auditoría de creación se conservan.
     */
    private void copiarCamposEditables(
            CondicionProteccion destino,
            CondicionProteccion origen
    ) {
        destino.setIdDatosPersonal(
                origen.getIdDatosPersonal()
        );

        destino.setAdministraRecursosPublicos(
                origen.isAdministraRecursosPublicos()
        );

        destino.setGrupoProteccionEspecialConstitucional(
                origen.isGrupoProteccionEspecialConstitucional()
        );

        destino.setPersonaMayor60Anos(
                origen.isPersonaMayor60Anos()
        );

        destino.setDiscapacidadFisica(
                origen.isDiscapacidadFisica()
        );

        destino.setVictimaConflictoArmado(
                origen.isVictimaConflictoArmado()
        );

        destino.setPobrezaExtrema(
                origen.isPobrezaExtrema()
        );

        destino.setPoblacionIndigena(
                origen.isPoblacionIndigena()
        );

        destino.setPoblacionAfrodescendiente(
                origen.isPoblacionAfrodescendiente()
        );

        destino.setPoblacionLgbtiqMas(
                origen.isPoblacionLgbtiqMas()
        );

        destino.setPerteneceGrupoProteccionConstitucional(
                origen.isPerteneceGrupoProteccionConstitucional()
        );

        destino.setObservaciones(
                origen.getObservaciones()
        );
    }

    /**
     * Limpia y normaliza los campos de texto.
     */
    private void normalizar(
            CondicionProteccion condicion
    ) {
        condicion.setObservaciones(
                limpiarTexto(
                        condicion.getObservaciones()
                )
        );
    }

    /**
     * Convierte textos vacíos en null.
     */
    private String limpiarTexto(
            String valor
    ) {
        if (valor == null) {
            return null;
        }

        String limpio = valor.trim();

        return limpio.isEmpty()
                ? null
                : limpio;
    }

    /**
     * Valida que el objeto recibido exista.
     */
    private void validarObjeto(
            CondicionProteccion condicion
    ) {
        if (condicion == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La información de condiciones de protección es obligatoria."
            );
        }
    }

    /**
     * Valida el identificador del registro.
     */
    private void validarIdCondicionProteccion(
            Long idCondicionProteccion
    ) {
        if (
                idCondicionProteccion == null
                        || idCondicionProteccion <= 0
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El identificador de condiciones de protección no es válido."
            );
        }
    }

    /**
     * Valida el identificador de la persona.
     */
    private void validarIdDatosPersonal(
            Integer idDatosPersonal
    ) {
        if (
                idDatosPersonal == null
                        || idDatosPersonal <= 0
        ) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El identificador de la persona es obligatorio."
            );
        }
    }
}