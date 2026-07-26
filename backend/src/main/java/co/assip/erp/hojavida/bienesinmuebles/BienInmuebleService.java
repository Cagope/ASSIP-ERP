package co.assip.erp.hojavida.bienesinmuebles;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class BienInmuebleService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private static final String ESTADO_ACTIVO = "A";
    private static final String ESTADO_INACTIVO = "I";

    private final BienInmuebleRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public BienInmuebleService(
            BienInmuebleRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    @Transactional(readOnly = true)
    public List<BienInmueble> listarPorPersona(Long idDatosPersonal) {

        validarIdPositivo(
                idDatosPersonal,
                "El asociado es obligatorio."
        );

        return repository.listarPorPersona(idDatosPersonal);
    }

    @Transactional(readOnly = true)
    public Optional<BienInmueble> buscarPorIdBien(Long idBien) {

        validarIdPositivo(
                idBien,
                "El bien es obligatorio."
        );

        return repository.buscarPorIdBien(idBien);
    }

    // ============================================================
    // PROCESOS
    // ============================================================

    public BienInmueble registrarBienInmueble(BienInmueble dto) {

        normalizarDatos(dto);
        validarParaGuardar(dto);

        Integer idUsuario = usuarioSesionService.idUsuario();

        Long idBien = repository.insertarBien(
                dto,
                idUsuario
        );

        Long idBienPersona = repository.insertarBienPersona(
                dto,
                idBien,
                idUsuario
        );

        Long idBienInmueble = repository.insertarInmueble(
                dto,
                idBien,
                idUsuario
        );

        dto.setIdBien(idBien);
        dto.setIdBienPersona(idBienPersona);
        dto.setIdBienInmueble(idBienInmueble);

        return repository.buscarPorIdBien(idBien)
                .orElse(dto);
    }

    public Optional<BienInmueble> actualizarBienInmueble(
            Long idBien,
            BienInmueble dto
    ) {

        validarIdPositivo(
                idBien,
                "El bien es obligatorio para actualizar."
        );

        if (!repository.existeBien(idBien)) {
            return Optional.empty();
        }

        normalizarDatos(dto);
        validarParaGuardar(dto);

        BienInmueble existente = repository.buscarPorIdBien(idBien)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No fue posible consultar la información actual del bien inmueble."
                ));

        Integer idUsuario = usuarioSesionService.idUsuario();

        repository.actualizarBien(
                idBien,
                dto,
                idUsuario
        );

        if (existente.getIdBienPersona() == null) {

            repository.insertarBienPersona(
                    dto,
                    idBien,
                    idUsuario
            );

        } else {

            repository.actualizarBienPersona(
                    existente.getIdBienPersona(),
                    dto,
                    idUsuario
            );
        }

        if (existente.getIdBienInmueble() == null) {

            repository.insertarInmueble(
                    dto,
                    idBien,
                    idUsuario
            );

        } else {

            repository.actualizarInmueble(
                    existente.getIdBienInmueble(),
                    dto,
                    idUsuario
            );
        }

        return repository.buscarPorIdBien(idBien);
    }

    public boolean eliminarBienInmueble(Long idBien) {

        validarIdPositivo(
                idBien,
                "El bien es obligatorio para eliminar."
        );

        if (!repository.existeBien(idBien)) {
            return false;
        }

        repository.eliminarPorBien(idBien);

        return true;
    }

    // ============================================================
    // VALIDACIÓN GENERAL
    // ============================================================

    private void validarParaGuardar(BienInmueble dto) {

        validarAsociado(dto);
        validarBienGeneral(dto);
        validarEstadoBien(dto);
        validarPropiedad(dto);

        validarDetalleInmueble(dto);
        validarZonaInmueble(dto);
        validarUbicacion(dto);

        validarGravamen(dto);
        validarAreas(dto);

        validarEscritura(dto);
        validarFechas(dto);
    }

    // ============================================================
    // VALIDACIÓN DEL ASOCIADO
    // ============================================================

    private void validarAsociado(BienInmueble dto) {

        validarIdPositivo(
                dto.getIdDatosPersonal(),
                "Debe seleccionar el asociado propietario del bien."
        );
    }

    // ============================================================
    // VALIDACIÓN DEL BIEN GENERAL
    // ============================================================

    private void validarBienGeneral(BienInmueble dto) {

        validarIdPositivo(
                dto.getIdTipoBien(),
                "Debe seleccionar el tipo de bien."
        );

        if (dto.getDescripcionGeneral() == null) {
            throw new IllegalArgumentException(
                    "La descripción general del bien es obligatoria."
            );
        }

        validarLongitud(
                dto.getDescripcionGeneral(),
                250,
                "La descripción general no puede superar 250 caracteres."
        );

        if (dto.getValorComercial() == null) {
            throw new IllegalArgumentException(
                    "El valor comercial es obligatorio."
            );
        }

        if (dto.getValorComercial().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El valor comercial no puede ser negativo."
            );
        }

        if (dto.getValorGravamen() == null) {
            throw new IllegalArgumentException(
                    "El valor del gravamen es obligatorio."
            );
        }

        if (dto.getValorGravamen().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El valor del gravamen no puede ser negativo."
            );
        }

        if (dto.getValorGravamen().compareTo(dto.getValorComercial()) > 0) {
            throw new IllegalArgumentException(
                    "El valor del gravamen no puede superar el valor comercial del bien."
            );
        }

        if (dto.getFechaAdquisicion() == null) {
            throw new IllegalArgumentException(
                    "La fecha de adquisición del bien es obligatoria."
            );
        }

        validarLongitud(
                dto.getObservacionesBien(),
                1000,
                "Las observaciones generales del bien no pueden superar 1000 caracteres."
        );
    }

    private void validarEstadoBien(BienInmueble dto) {

        if (dto.getEstadoBien() == null) {
            throw new IllegalArgumentException(
                    "El estado del bien es obligatorio."
            );
        }

        if (!ESTADO_ACTIVO.equals(dto.getEstadoBien())
                && !ESTADO_INACTIVO.equals(dto.getEstadoBien())) {

            throw new IllegalArgumentException(
                    "El estado del bien debe ser A para activo o I para inactivo."
            );
        }

        if (dto.getFechaEstado() == null) {
            throw new IllegalArgumentException(
                    "La fecha del estado del bien es obligatoria."
            );
        }
    }

    // ============================================================
    // VALIDACIÓN DE LA PROPIEDAD
    // ============================================================

    private void validarPropiedad(BienInmueble dto) {

        if (dto.getPorcentajePropiedad() == null) {
            throw new IllegalArgumentException(
                    "El porcentaje de propiedad es obligatorio."
            );
        }

        if (dto.getPorcentajePropiedad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "El porcentaje de propiedad debe ser mayor que cero."
            );
        }

        if (dto.getPorcentajePropiedad().compareTo(CIEN) > 0) {
            throw new IllegalArgumentException(
                    "El porcentaje de propiedad no puede superar el 100%."
            );
        }
    }

    // ============================================================
    // VALIDACIÓN DEL INMUEBLE
    // ============================================================

    private void validarDetalleInmueble(BienInmueble dto) {

        validarIdPositivo(
                dto.getIdTipoInmueble(),
                "Debe seleccionar el tipo de inmueble."
        );

        if (dto.getDireccion() == null) {
            throw new IllegalArgumentException(
                    "La dirección del inmueble es obligatoria."
            );
        }

        validarLongitud(
                dto.getDireccion(),
                150,
                "La dirección del inmueble no puede superar 150 caracteres."
        );

        validarLongitud(
                dto.getNumeroMatriculaInmobiliaria(),
                50,
                "La matrícula inmobiliaria no puede superar 50 caracteres."
        );

        validarLongitud(
                dto.getCedulaCatastral(),
                50,
                "La cédula catastral no puede superar 50 caracteres."
        );

        validarLongitud(
                dto.getBarrioVereda(),
                100,
                "El barrio o vereda no puede superar 100 caracteres."
        );

        validarLongitud(
                dto.getObservaciones(),
                500,
                "Las observaciones del inmueble no pueden superar 500 caracteres."
        );
    }

    private void validarZonaInmueble(BienInmueble dto) {

        validarIdPositivo(
                dto.getIdTipoZonaInmueble(),
                "Debe seleccionar la zona del inmueble."
        );
    }

    private void validarUbicacion(BienInmueble dto) {

        validarIdPositivo(
                dto.getIdPais(),
                "Debe seleccionar el país del inmueble."
        );

        validarIdPositivo(
                dto.getIdDepartamento(),
                "Debe seleccionar el departamento del inmueble."
        );

        validarIdPositivo(
                dto.getIdCiudad(),
                "Debe seleccionar la ciudad del inmueble."
        );
    }

    // ============================================================
    // VALIDACIÓN DEL GRAVAMEN
    // ============================================================

    private void validarGravamen(BienInmueble dto) {

        validarIdPositivo(
                dto.getIdTipoGravamen(),
                "Debe seleccionar el tipo de gravamen más material."
        );

        if (dto.getValorGravamen().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        if (dto.getValorComercial().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException(
                    "Si el bien tiene gravamen, el valor comercial debe ser mayor que cero."
            );
        }
    }

    // ============================================================
    // VALIDACIÓN DE ÁREAS
    // ============================================================

    private void validarAreas(BienInmueble dto) {

        if (dto.getAreaTerreno() != null
                && dto.getAreaTerreno().compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El área del terreno no puede ser negativa."
            );
        }

        if (dto.getAreaConstruida() != null
                && dto.getAreaConstruida().compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El área construida no puede ser negativa."
            );
        }
    }

    // ============================================================
    // VALIDACIÓN DE ESCRITURA Y REGISTRO
    // ============================================================

    private void validarEscritura(BienInmueble dto) {

        validarLongitud(
                dto.getNumeroEscritura(),
                50,
                "El número de escritura no puede superar 50 caracteres."
        );

        validarLongitud(
                dto.getNotaria(),
                100,
                "La notaría no puede superar 100 caracteres."
        );

        validarLongitud(
                dto.getOficinaRegistro(),
                150,
                "La oficina de registro no puede superar 150 caracteres."
        );

        boolean tieneDatosNotaria =
                dto.getIdPaisNotaria() != null
                        || dto.getIdDepartamentoNotaria() != null
                        || dto.getIdCiudadNotaria() != null;

        if (!tieneDatosNotaria) {
            return;
        }

        validarIdPositivo(
                dto.getIdPaisNotaria(),
                "Debe seleccionar el país de la notaría."
        );

        validarIdPositivo(
                dto.getIdDepartamentoNotaria(),
                "Debe seleccionar el departamento de la notaría."
        );

        validarIdPositivo(
                dto.getIdCiudadNotaria(),
                "Debe seleccionar la ciudad de la notaría."
        );
    }

    // ============================================================
    // VALIDACIÓN DE FECHAS
    // ============================================================

    private void validarFechas(BienInmueble dto) {

        LocalDate hoy = LocalDate.now();

        if (dto.getFechaAdquisicion().isAfter(hoy)) {
            throw new IllegalArgumentException(
                    "La fecha de adquisición no puede ser posterior a la fecha actual."
            );
        }

        if (dto.getFechaEstado().isBefore(dto.getFechaAdquisicion())) {
            throw new IllegalArgumentException(
                    "La fecha del estado no puede ser anterior a la fecha de adquisición."
            );
        }

        if (dto.getFechaEstado().isAfter(hoy)) {
            throw new IllegalArgumentException(
                    "La fecha del estado no puede ser posterior a la fecha actual."
            );
        }

        if (dto.getFechaEscritura() != null
                && dto.getFechaEscritura().isAfter(hoy)) {

            throw new IllegalArgumentException(
                    "La fecha de la escritura no puede ser posterior a la fecha actual."
            );
        }

        if (dto.getFechaRegistroEscritura() != null
                && dto.getFechaRegistroEscritura().isAfter(hoy)) {

            throw new IllegalArgumentException(
                    "La fecha de registro de la escritura no puede ser posterior a la fecha actual."
            );
        }

        if (dto.getFechaEscritura() != null
                && dto.getFechaRegistroEscritura() != null
                && dto.getFechaRegistroEscritura()
                .isBefore(dto.getFechaEscritura())) {

            throw new IllegalArgumentException(
                    "La fecha de registro no puede ser anterior a la fecha de la escritura."
            );
        }
    }

    // ============================================================
    // NORMALIZACIÓN
    // ============================================================

    private void normalizarDatos(BienInmueble dto) {

        if (dto == null) {
            throw new IllegalArgumentException(
                    "La información del bien inmueble es obligatoria."
            );
        }

        // --------------------------------------------------------
        // Bien general
        // --------------------------------------------------------
        dto.setDescripcionGeneral(
                limpiar(dto.getDescripcionGeneral())
        );

        dto.setObservacionesBien(
                limpiar(dto.getObservacionesBien())
        );

        dto.setEstadoBien(
                normalizarEstado(dto.getEstadoBien())
        );

        // --------------------------------------------------------
        // Inmueble
        // --------------------------------------------------------
        dto.setNumeroMatriculaInmobiliaria(
                limpiar(dto.getNumeroMatriculaInmobiliaria())
        );

        dto.setCedulaCatastral(
                limpiar(dto.getCedulaCatastral())
        );

        dto.setDireccion(
                limpiar(dto.getDireccion())
        );

        dto.setBarrioVereda(
                limpiar(dto.getBarrioVereda())
        );

        dto.setNumeroEscritura(
                limpiar(dto.getNumeroEscritura())
        );

        dto.setNotaria(
                limpiar(dto.getNotaria())
        );

        dto.setOficinaRegistro(
                limpiar(dto.getOficinaRegistro())
        );

        dto.setObservaciones(
                limpiar(dto.getObservaciones())
        );

        // --------------------------------------------------------
        // Valores predeterminados
        // --------------------------------------------------------
        if (dto.getPorcentajePropiedad() == null) {
            dto.setPorcentajePropiedad(CIEN);
        }

        if (dto.getValorComercial() == null) {
            dto.setValorComercial(BigDecimal.ZERO);
        }

        if (dto.getValorGravamen() == null) {
            dto.setValorGravamen(BigDecimal.ZERO);
        }

        if (dto.getAreaTerreno() == null) {
            dto.setAreaTerreno(BigDecimal.ZERO);
        }

        if (dto.getAreaConstruida() == null) {
            dto.setAreaConstruida(BigDecimal.ZERO);
        }

        if (dto.getEstadoBien() == null) {
            dto.setEstadoBien(ESTADO_ACTIVO);
        }

        if (dto.getFechaEstado() == null) {
            dto.setFechaEstado(LocalDate.now());
        }
    }

    // ============================================================
    // UTILIDADES
    // ============================================================

    private String limpiar(String valor) {

        if (valor == null) {
            return null;
        }

        String limpio = valor.trim();

        return limpio.isEmpty()
                ? null
                : limpio;
    }

    private String normalizarEstado(String estado) {

        String limpio = limpiar(estado);

        return limpio == null
                ? null
                : limpio.toUpperCase(Locale.ROOT);
    }

    private void validarLongitud(
            String valor,
            int maximo,
            String mensaje
    ) {

        if (valor != null && valor.length() > maximo) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private void validarIdPositivo(
            Long valor,
            String mensaje
    ) {

        if (valor == null || valor <= 0) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}