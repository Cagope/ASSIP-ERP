package co.assip.erp.hojavida.bienesvehiculos;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BienVehiculoService {

    private final BienVehiculoRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public BienVehiculoService(
            BienVehiculoRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    // ============================================================
    // CONSULTAS
    // ============================================================

    public List<BienVehiculo> listarPorPersona(Long idDatosPersonal) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException("El asociado es obligatorio.");
        }

        return repository.listarPorPersona(idDatosPersonal);
    }

    public Optional<BienVehiculo> buscarPorIdBien(Long idBien) {
        if (idBien == null || idBien <= 0) {
            throw new IllegalArgumentException("El bien es obligatorio.");
        }

        return repository.buscarPorIdBien(idBien);
    }

    // ============================================================
    // PROCESOS
    // ============================================================

    public BienVehiculo registrarBienVehiculo(BienVehiculo dto) {
        normalizarDatos(dto);
        validarParaGuardar(dto);

        Integer idUsuario = usuarioSesionService.idUsuario();

        Long idBien = repository.insertarBien(dto, idUsuario);
        Long idBienPersona = repository.insertarBienPersona(dto, idBien, idUsuario);
        Long idBienVehiculo = repository.insertarVehiculo(dto, idBien, idUsuario);

        dto.setIdBien(idBien);
        dto.setIdBienPersona(idBienPersona);
        dto.setIdBienVehiculo(idBienVehiculo);

        return repository.buscarPorIdBien(idBien)
                .orElse(dto);
    }

    public Optional<BienVehiculo> actualizarBienVehiculo(
            Long idBien,
            BienVehiculo dto
    ) {
        if (idBien == null || idBien <= 0) {
            throw new IllegalArgumentException("El bien es obligatorio para actualizar.");
        }

        if (!repository.existeBien(idBien)) {
            return Optional.empty();
        }

        normalizarDatos(dto);
        validarParaGuardar(dto);

        BienVehiculo existente = repository.buscarPorIdBien(idBien)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No fue posible consultar la información actual del bien vehículo."
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

        if (existente.getIdBienVehiculo() == null) {
            repository.insertarVehiculo(dto, idBien, idUsuario);
        } else {
            repository.actualizarVehiculo(
                    existente.getIdBienVehiculo(),
                    dto,
                    idUsuario
            );
        }

        return repository.buscarPorIdBien(idBien);
    }

    public boolean eliminarBienVehiculo(Long idBien) {
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

    private void validarParaGuardar(BienVehiculo dto) {
        validarAsociado(dto);
        validarBienGeneral(dto);
        validarPropiedad(dto);
        validarDetalleVehiculo(dto);
        validarGravamen(dto);
    }

    private void validarAsociado(BienVehiculo dto) {
        if (dto.getIdDatosPersonal() == null || dto.getIdDatosPersonal() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el asociado propietario del bien.");
        }
    }

    private void validarBienGeneral(BienVehiculo dto) {
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

    private void validarPropiedad(BienVehiculo dto) {
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

    private void validarDetalleVehiculo(BienVehiculo dto) {
        if (dto.getIdTipoVehiculo() == null || dto.getIdTipoVehiculo() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de vehículo.");
        }

        if (dto.getPlaca() == null || dto.getPlaca().isBlank()) {
            throw new IllegalArgumentException("La placa del vehículo es obligatoria.");
        }

        validarLongitud(dto.getPlaca(), 20,
                "La placa del vehículo no puede superar 20 caracteres.");

        validarLongitud(dto.getMarca(), 100,
                "La marca del vehículo no puede superar 100 caracteres.");

        validarLongitud(dto.getLinea(), 100,
                "La línea del vehículo no puede superar 100 caracteres.");

        validarLongitud(dto.getColor(), 50,
                "El color del vehículo no puede superar 50 caracteres.");

        validarLongitud(dto.getNumeroMotor(), 100,
                "El número de motor no puede superar 100 caracteres.");

        validarLongitud(dto.getNumeroChasis(), 100,
                "El número de chasis no puede superar 100 caracteres.");

        validarLongitud(dto.getNumeroSerie(), 100,
                "El número de serie no puede superar 100 caracteres.");

        validarLongitud(dto.getObservaciones(), 1000,
                "Las observaciones no pueden superar 1000 caracteres.");

        if (dto.getModelo() != null) {
            int anioActual = Year.now().getValue();

            if (dto.getModelo() < 1900) {
                throw new IllegalArgumentException("El modelo del vehículo no es válido.");
            }

            if (dto.getModelo() > anioActual + 1) {
                throw new IllegalArgumentException("El modelo del vehículo no puede superar el año siguiente al actual.");
            }
        }
    }

    private void validarGravamen(BienVehiculo dto) {
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

    // ============================================================
    // NORMALIZACIÓN
    // ============================================================

    private void normalizarDatos(BienVehiculo dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La información del bien vehículo es obligatoria.");
        }

        dto.setDescripcionGeneral(limpiar(dto.getDescripcionGeneral()));
        dto.setPlaca(limpiarMayuscula(dto.getPlaca()));
        dto.setMarca(limpiarMayuscula(dto.getMarca()));
        dto.setLinea(limpiarMayuscula(dto.getLinea()));
        dto.setColor(limpiarMayuscula(dto.getColor()));
        dto.setNumeroMotor(limpiarMayuscula(dto.getNumeroMotor()));
        dto.setNumeroChasis(limpiarMayuscula(dto.getNumeroChasis()));
        dto.setNumeroSerie(limpiarMayuscula(dto.getNumeroSerie()));
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

    private String limpiar(String valor) {
        if (valor == null) return null;

        String limpio = valor.trim();

        if (limpio.isEmpty()) return null;

        return limpio;
    }

    private String limpiarMayuscula(String valor) {
        String limpio = limpiar(valor);

        if (limpio == null) return null;

        return limpio.toUpperCase();
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