package co.assip.erp.hojavida.bienesmaquinaria;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BienMaquinariaService {

    private final BienMaquinariaRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public BienMaquinariaService(
            BienMaquinariaRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    public List<BienMaquinaria> listarPorPersona(Long idDatosPersonal) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException("El asociado es obligatorio.");
        }

        return repository.listarPorPersona(idDatosPersonal);
    }

    public Optional<BienMaquinaria> buscarPorIdBien(Long idBien) {
        validarIdBien(idBien);
        return repository.buscarPorIdBien(idBien);
    }

    public BienMaquinaria registrarBienMaquinaria(BienMaquinaria dto) {
        normalizarDatos(dto);
        validarParaGuardar(dto);

        Integer idUsuario = usuarioSesionService.idUsuario();

        Long idBien = repository.insertarBien(dto, idUsuario);
        Long idBienPersona = repository.insertarBienPersona(dto, idBien, idUsuario);
        Long idBienMaquinaria = repository.insertarMaquinaria(dto, idBien, idUsuario);

        dto.setIdBien(idBien);
        dto.setIdBienPersona(idBienPersona);
        dto.setIdBienMaquinaria(idBienMaquinaria);

        return repository.buscarPorIdBien(idBien)
                .orElse(dto);
    }

    public Optional<BienMaquinaria> actualizarBienMaquinaria(
            Long idBien,
            BienMaquinaria dto
    ) {
        validarIdBien(idBien);

        if (!repository.existeBien(idBien)) {
            return Optional.empty();
        }

        normalizarDatos(dto);
        validarParaGuardar(dto);

        BienMaquinaria existente = repository.buscarPorIdBien(idBien)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No fue posible consultar la información actual del bien maquinaria."
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

        if (existente.getIdBienMaquinaria() == null) {
            repository.insertarMaquinaria(dto, idBien, idUsuario);
        } else {
            repository.actualizarMaquinaria(
                    existente.getIdBienMaquinaria(),
                    dto,
                    idUsuario
            );
        }

        return repository.buscarPorIdBien(idBien);
    }

    public boolean eliminarBienMaquinaria(Long idBien) {
        validarIdBien(idBien);

        if (!repository.existeBien(idBien)) {
            return false;
        }

        repository.eliminarPorBien(idBien);
        return true;
    }

    private void validarParaGuardar(BienMaquinaria dto) {
        validarAsociado(dto);
        validarBienGeneral(dto);
        validarPropiedad(dto);
        validarDetalleMaquinaria(dto);
        validarGravamen(dto);
    }

    private void validarAsociado(BienMaquinaria dto) {
        if (dto.getIdDatosPersonal() == null || dto.getIdDatosPersonal() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el asociado propietario del bien.");
        }
    }

    private void validarBienGeneral(BienMaquinaria dto) {
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

    private void validarPropiedad(BienMaquinaria dto) {
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

    private void validarDetalleMaquinaria(BienMaquinaria dto) {
        if (dto.getIdTipoMaquinaria() == null || dto.getIdTipoMaquinaria() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de maquinaria.");
        }

        validarLongitud(dto.getMarca(), 100,
                "La marca no puede superar 100 caracteres.");

        validarLongitud(dto.getModelo(), 100,
                "El modelo no puede superar 100 caracteres.");

        validarLongitud(dto.getSerial(), 100,
                "El serial no puede superar 100 caracteres.");

        validarLongitud(dto.getReferencia(), 100,
                "La referencia no puede superar 100 caracteres.");

        validarLongitud(dto.getDescripcionTecnica(), 500,
                "La descripción técnica no puede superar 500 caracteres.");

        validarLongitud(dto.getUbicacion(), 250,
                "La ubicación no puede superar 250 caracteres.");

        validarLongitud(dto.getEstadoOperativo(), 50,
                "El estado operativo no puede superar 50 caracteres.");

        validarLongitud(dto.getObservaciones(), 1000,
                "Las observaciones no pueden superar 1000 caracteres.");
    }

    private void validarGravamen(BienMaquinaria dto) {
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

    private void normalizarDatos(BienMaquinaria dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La información del bien maquinaria es obligatoria.");
        }

        dto.setDescripcionGeneral(limpiar(dto.getDescripcionGeneral()));
        dto.setMarca(limpiarMayuscula(dto.getMarca()));
        dto.setModelo(limpiarMayuscula(dto.getModelo()));
        dto.setSerial(limpiarMayuscula(dto.getSerial()));
        dto.setReferencia(limpiarMayuscula(dto.getReferencia()));
        dto.setDescripcionTecnica(limpiar(dto.getDescripcionTecnica()));
        dto.setUbicacion(limpiarMayuscula(dto.getUbicacion()));
        dto.setEstadoOperativo(limpiarMayuscula(dto.getEstadoOperativo()));
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
    }

    private void validarIdBien(Long idBien) {
        if (idBien == null || idBien <= 0) {
            throw new IllegalArgumentException("El bien es obligatorio.");
        }
    }

    private String limpiar(String valor) {
        if (valor == null) return null;

        String limpio = valor.trim();
        return limpio.isEmpty() ? null : limpio;
    }

    private String limpiarMayuscula(String valor) {
        String limpio = limpiar(valor);
        return limpio == null ? null : limpio.toUpperCase();
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