package co.assip.erp.hojavida.bienesinversiones;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BienInversionService {

    private final BienInversionRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public BienInversionService(
            BienInversionRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    public List<BienInversion> listarPorPersona(Long idDatosPersonal) {
        if (idDatosPersonal == null || idDatosPersonal <= 0) {
            throw new IllegalArgumentException(
                    "El asociado es obligatorio."
            );
        }

        return repository.listarPorPersona(idDatosPersonal);
    }

    public Optional<BienInversion> buscarPorIdBien(Long idBien) {
        validarIdBien(idBien);
        return repository.buscarPorIdBien(idBien);
    }

    public BienInversion registrarBienInversion(
            BienInversion dto
    ) {
        normalizarDatos(dto);
        validarParaGuardar(dto);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        Long idBien =
                repository.insertarBien(
                        dto,
                        idUsuario
                );

        Long idBienPersona =
                repository.insertarBienPersona(
                        dto,
                        idBien,
                        idUsuario
                );

        Long idBienInversion =
                repository.insertarInversion(
                        dto,
                        idBien,
                        idUsuario
                );

        dto.setIdBien(idBien);
        dto.setIdBienPersona(idBienPersona);
        dto.setIdBienInversion(idBienInversion);

        return repository.buscarPorIdBien(idBien)
                .orElse(dto);
    }

    public Optional<BienInversion> actualizarBienInversion(
            Long idBien,
            BienInversion dto
    ) {
        validarIdBien(idBien);

        if (!repository.existeBien(idBien)) {
            return Optional.empty();
        }

        normalizarDatos(dto);
        validarParaGuardar(dto);

        BienInversion existente =
                repository.buscarPorIdBien(idBien)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "No fue posible consultar la información actual del bien inversión."
                                )
                        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

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

        if (existente.getIdBienInversion() == null) {
            repository.insertarInversion(
                    dto,
                    idBien,
                    idUsuario
            );
        } else {
            repository.actualizarInversion(
                    existente.getIdBienInversion(),
                    dto,
                    idUsuario
            );
        }

        return repository.buscarPorIdBien(idBien);
    }

    public boolean eliminarBienInversion(Long idBien) {
        validarIdBien(idBien);

        if (!repository.existeBien(idBien)) {
            return false;
        }

        repository.eliminarPorBien(idBien);
        return true;
    }

    private void validarParaGuardar(
            BienInversion dto
    ) {
        validarAsociado(dto);
        validarBienGeneral(dto);
        validarPropiedad(dto);
        validarDetalleInversion(dto);
        validarGravamen(dto);
    }

    private void validarAsociado(
            BienInversion dto
    ) {
        if (dto.getIdDatosPersonal() == null
                || dto.getIdDatosPersonal() <= 0) {

            throw new IllegalArgumentException(
                    "Debe seleccionar el asociado propietario del bien."
            );
        }
    }

    private void validarBienGeneral(
            BienInversion dto
    ) {
        if (dto.getIdTipoBien() == null
                || dto.getIdTipoBien() <= 0) {

            throw new IllegalArgumentException(
                    "Debe seleccionar el tipo de bien."
            );
        }

        if (dto.getDescripcionGeneral() == null
                || dto.getDescripcionGeneral().isBlank()) {

            throw new IllegalArgumentException(
                    "La descripción general del bien es obligatoria."
            );
        }

        if (dto.getDescripcionGeneral().length() > 250) {
            throw new IllegalArgumentException(
                    "La descripción general no puede superar 250 caracteres."
            );
        }

        if (dto.getValorComercial() == null) {
            throw new IllegalArgumentException(
                    "El valor comercial es obligatorio."
            );
        }

        if (dto.getValorComercial()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El valor comercial no puede ser negativo."
            );
        }

        if (dto.getValorGravamen() == null) {
            throw new IllegalArgumentException(
                    "El valor del gravamen es obligatorio."
            );
        }

        if (dto.getValorGravamen()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El valor del gravamen no puede ser negativo."
            );
        }

        if (dto.getValorGravamen()
                .compareTo(dto.getValorComercial()) > 0) {

            throw new IllegalArgumentException(
                    "El valor del gravamen no puede superar el valor comercial del bien."
            );
        }
    }

    private void validarPropiedad(
            BienInversion dto
    ) {
        if (dto.getPorcentajePropiedad() == null) {
            throw new IllegalArgumentException(
                    "El porcentaje de propiedad es obligatorio."
            );
        }

        if (dto.getPorcentajePropiedad()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El porcentaje de propiedad debe ser mayor que cero."
            );
        }

        if (dto.getPorcentajePropiedad()
                .compareTo(new BigDecimal("100")) > 0) {

            throw new IllegalArgumentException(
                    "El porcentaje de propiedad no puede superar el 100%."
            );
        }
    }

    private void validarDetalleInversion(
            BienInversion dto
    ) {
        if (dto.getIdTipoInversion() == null
                || dto.getIdTipoInversion() <= 0) {

            throw new IllegalArgumentException(
                    "Debe seleccionar el tipo de inversión."
            );
        }

        validarLongitud(
                dto.getEntidad(),
                150,
                "La entidad no puede superar 150 caracteres."
        );

        validarLongitud(
                dto.getNumeroTitulo(),
                100,
                "El número de título no puede superar 100 caracteres."
        );

        validarLongitud(
                dto.getObservaciones(),
                1000,
                "Las observaciones no pueden superar 1000 caracteres."
        );

        if (dto.getFechaInversion() != null
                && dto.getFechaVencimiento() != null
                && dto.getFechaVencimiento()
                .isBefore(dto.getFechaInversion())) {

            throw new IllegalArgumentException(
                    "La fecha de vencimiento no puede ser anterior a la fecha de inversión."
            );
        }

        if (dto.getValorNominal() == null) {
            throw new IllegalArgumentException(
                    "El valor nominal es obligatorio."
            );
        }

        if (dto.getValorNominal()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El valor nominal no puede ser negativo."
            );
        }

        if (dto.getValorActual() == null) {
            throw new IllegalArgumentException(
                    "El valor actual es obligatorio."
            );
        }

        if (dto.getValorActual()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "El valor actual no puede ser negativo."
            );
        }

        if (dto.getTasaRendimiento() != null
                && dto.getTasaRendimiento()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "La tasa de rendimiento no puede ser negativa."
            );
        }
    }

    private void validarGravamen(
            BienInversion dto
    ) {
        if (dto.getIdTipoGravamen() == null
                || dto.getIdTipoGravamen() <= 0) {

            throw new IllegalArgumentException(
                    "Debe seleccionar el tipo de gravamen más material."
            );
        }

        if (dto.getValorGravamen()
                .compareTo(BigDecimal.ZERO) == 0) {

            return;
        }

        if (dto.getValorComercial()
                .compareTo(BigDecimal.ZERO) == 0) {

            throw new IllegalArgumentException(
                    "Si el bien tiene gravamen, el valor comercial debe ser mayor que cero."
            );
        }
    }

    private void normalizarDatos(
            BienInversion dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "La información del bien inversión es obligatoria."
            );
        }

        dto.setDescripcionGeneral(
                limpiar(dto.getDescripcionGeneral())
        );

        dto.setEntidad(
                limpiarMayuscula(dto.getEntidad())
        );

        dto.setNumeroTitulo(
                limpiarMayuscula(dto.getNumeroTitulo())
        );

        dto.setObservaciones(
                limpiar(dto.getObservaciones())
        );

        if (dto.getPorcentajePropiedad() == null) {
            dto.setPorcentajePropiedad(
                    new BigDecimal("100")
            );
        }

        if (dto.getValorComercial() == null) {
            dto.setValorComercial(
                    BigDecimal.ZERO
            );
        }

        if (dto.getValorGravamen() == null) {
            dto.setValorGravamen(
                    BigDecimal.ZERO
            );
        }

        if (dto.getValorNominal() == null) {
            dto.setValorNominal(
                    BigDecimal.ZERO
            );
        }

        if (dto.getValorActual() == null) {
            dto.setValorActual(
                    BigDecimal.ZERO
            );
        }
    }

    private void validarIdBien(Long idBien) {
        if (idBien == null || idBien <= 0) {
            throw new IllegalArgumentException(
                    "El bien es obligatorio."
            );
        }
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }

        String limpio = valor.trim();

        return limpio.isEmpty()
                ? null
                : limpio;
    }

    private String limpiarMayuscula(
            String valor
    ) {
        String limpio = limpiar(valor);

        return limpio == null
                ? null
                : limpio.toUpperCase();
    }

    private void validarLongitud(
            String valor,
            int maximo,
            String mensaje
    ) {
        if (valor != null
                && valor.length() > maximo) {

            throw new IllegalArgumentException(
                    mensaje
            );
        }
    }
}