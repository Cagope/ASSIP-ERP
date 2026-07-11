package co.assip.erp.hojavida.bienesinmuebles;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BienInmuebleService {

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

    public List<BienInmueble> listarPorPersona(Long idDatosPersonal) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException("El asociado es obligatorio.");
        }

        return repository.listarPorPersona(idDatosPersonal);
    }

    public Optional<BienInmueble> buscarPorIdBien(Long idBien) {
        if (idBien == null || idBien <= 0) {
            throw new IllegalArgumentException("El bien es obligatorio.");
        }

        return repository.buscarPorIdBien(idBien);
    }

    // ============================================================
    // PROCESOS
    // ============================================================

    public BienInmueble registrarBienInmueble(BienInmueble dto) {
        normalizarDatos(dto);
        validarParaGuardar(dto);

        Integer idUsuario = usuarioSesionService.idUsuario();

        Long idBien = repository.insertarBien(dto, idUsuario);
        Long idBienPersona = repository.insertarBienPersona(dto, idBien, idUsuario);
        Long idBienInmueble = repository.insertarInmueble(dto, idBien, idUsuario);

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
        if (idBien == null || idBien <= 0) {
            throw new IllegalArgumentException("El bien es obligatorio para actualizar.");
        }

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

        repository.actualizarBien(idBien, dto, idUsuario);

        if (existente.getIdBienPersona() == null) {
            repository.insertarBienPersona(dto, idBien, idUsuario);
        } else {
            repository.actualizarBienPersona(
                    existente.getIdBienPersona(),
                    dto,
                    idUsuario
            );
        }

        if (existente.getIdBienInmueble() == null) {
            repository.insertarInmueble(dto, idBien, idUsuario);
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
        if (idBien == null || idBien <= 0) {
            throw new IllegalArgumentException("El bien es obligatorio para eliminar.");
        }

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
        validarPropiedad(dto);
        validarDetalleInmueble(dto);
        validarUbicacion(dto);
        validarGravamen(dto);
        validarAreas(dto);
        validarEscritura(dto);
    }

    private void validarAsociado(BienInmueble dto) {
        if (dto.getIdDatosPersonal() == null || dto.getIdDatosPersonal() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el asociado propietario del bien.");
        }
    }

    private void validarBienGeneral(BienInmueble dto) {
        if (dto.getIdTipoBien() == null || dto.getIdTipoBien() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de bien.");
        }

        if (dto.getDescripcionGeneral() == null || dto.getDescripcionGeneral().isBlank()) {
            throw new IllegalArgumentException("La descripción general del bien es obligatoria.");
        }

        if (dto.getDescripcionGeneral().length() > 250) {
            throw new IllegalArgumentException("La descripción general no puede superar 250 caracteres.");
        }

        if (dto.getValorComercial() == null) {
            throw new IllegalArgumentException("El valor comercial es obligatorio.");
        }

        if (dto.getValorComercial().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor comercial no puede ser negativo.");
        }

        if (dto.getValorGravamen() == null) {
            throw new IllegalArgumentException("El valor del gravamen es obligatorio.");
        }

        if (dto.getValorGravamen().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor del gravamen no puede ser negativo.");
        }

        if (dto.getValorGravamen().compareTo(dto.getValorComercial()) > 0) {
            throw new IllegalArgumentException("El valor del gravamen no puede superar el valor comercial del bien.");
        }
    }

    private void validarPropiedad(BienInmueble dto) {
        if (dto.getPorcentajePropiedad() == null) {
            throw new IllegalArgumentException("El porcentaje de propiedad es obligatorio.");
        }

        if (dto.getPorcentajePropiedad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El porcentaje de propiedad debe ser mayor que cero.");
        }

        if (dto.getPorcentajePropiedad().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("El porcentaje de propiedad no puede superar el 100%.");
        }
    }

    private void validarDetalleInmueble(BienInmueble dto) {
        if (dto.getIdTipoInmueble() == null || dto.getIdTipoInmueble() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de inmueble.");
        }

        if (dto.getDireccion() == null || dto.getDireccion().isBlank()) {
            throw new IllegalArgumentException("La dirección del inmueble es obligatoria.");
        }

        if (dto.getDireccion().length() > 250) {
            throw new IllegalArgumentException("La dirección del inmueble no puede superar 250 caracteres.");
        }

        validarLongitud(dto.getNumeroMatriculaInmobiliaria(), 50,
                "La matrícula inmobiliaria no puede superar 50 caracteres.");

        validarLongitud(dto.getCedulaCatastral(), 50,
                "La cédula catastral no puede superar 50 caracteres.");

        validarLongitud(dto.getBarrioVereda(), 100,
                "El barrio o vereda no puede superar 100 caracteres.");

        validarLongitud(dto.getObservaciones(), 1000,
                "Las observaciones no pueden superar 1000 caracteres.");
    }

    private void validarUbicacion(BienInmueble dto) {
        if (dto.getIdPais() == null || dto.getIdPais() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el país del inmueble.");
        }

        if (dto.getIdDepartamento() == null || dto.getIdDepartamento() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el departamento del inmueble.");
        }

        if (dto.getIdCiudad() == null || dto.getIdCiudad() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar la ciudad del inmueble.");
        }
    }

    private void validarGravamen(BienInmueble dto) {
        if (dto.getIdTipoGravamen() == null || dto.getIdTipoGravamen() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de gravamen más material.");
        }

        if (dto.getValorGravamen().compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        if (dto.getValorComercial().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Si el bien tiene gravamen, el valor comercial debe ser mayor que cero.");
        }
    }

    private void validarAreas(BienInmueble dto) {
        if (dto.getAreaTerreno() != null
                && dto.getAreaTerreno().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El área del terreno no puede ser negativa.");
        }

        if (dto.getAreaConstruida() != null
                && dto.getAreaConstruida().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El área construida no puede ser negativa.");
        }
    }

    private void validarEscritura(BienInmueble dto) {
        validarLongitud(dto.getNumeroEscritura(), 50,
                "El número de escritura no puede superar 50 caracteres.");

        validarLongitud(dto.getNotaria(), 100,
                "La notaría no puede superar 100 caracteres.");

        boolean tieneDatosNotaria =
                dto.getIdPaisNotaria() != null
                        || dto.getIdDepartamentoNotaria() != null
                        || dto.getIdCiudadNotaria() != null;

        if (tieneDatosNotaria) {
            if (dto.getIdPaisNotaria() == null || dto.getIdPaisNotaria() <= 0) {
                throw new IllegalArgumentException("Debe seleccionar el país de la notaría.");
            }

            if (dto.getIdDepartamentoNotaria() == null || dto.getIdDepartamentoNotaria() <= 0) {
                throw new IllegalArgumentException("Debe seleccionar el departamento de la notaría.");
            }

            if (dto.getIdCiudadNotaria() == null || dto.getIdCiudadNotaria() <= 0) {
                throw new IllegalArgumentException("Debe seleccionar la ciudad de la notaría.");
            }
        }
    }

    // ============================================================
    // NORMALIZACIÓN
    // ============================================================

    private void normalizarDatos(BienInmueble dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La información del bien inmueble es obligatoria.");
        }

        dto.setDescripcionGeneral(limpiar(dto.getDescripcionGeneral()));
        dto.setNumeroMatriculaInmobiliaria(limpiar(dto.getNumeroMatriculaInmobiliaria()));
        dto.setCedulaCatastral(limpiar(dto.getCedulaCatastral()));
        dto.setDireccion(limpiar(dto.getDireccion()));
        dto.setBarrioVereda(limpiar(dto.getBarrioVereda()));
        dto.setNumeroEscritura(limpiar(dto.getNumeroEscritura()));
        dto.setNotaria(limpiar(dto.getNotaria()));
        dto.setObservaciones(limpiar(dto.getObservaciones()));

        if (dto.getPorcentajePropiedad() == null) {
            dto.setPorcentajePropiedad(new BigDecimal("100"));
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
    }

    private String limpiar(String valor) {
        if (valor == null) return null;

        String limpio = valor.trim();

        if (limpio.isEmpty()) return null;

        return limpio;
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
}