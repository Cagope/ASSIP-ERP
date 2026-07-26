package co.assip.erp.hojavida.bienesinmueblesavaluos;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BienInmuebleAvaluoService {

    private static final int VIGENCIA_MAXIMA_ANIOS = 30;

    private final BienInmuebleAvaluoRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public BienInmuebleAvaluoService(
            BienInmuebleAvaluoRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    @Transactional(readOnly = true)
    public List<BienInmuebleAvaluo> listarPorBien(Long idBien) {
        validarIdBien(idBien);

        return repository
                .findByIdBienOrderByFechaAvaluoDescFechaCreacionDesc(idBien);
    }

    @Transactional(readOnly = true)
    public Optional<BienInmuebleAvaluo> buscarPorId(Long id) {
        validarIdAvaluo(id);

        return repository.findById(id);
    }

    public BienInmuebleAvaluo crear(BienInmuebleAvaluo dto) {
        validarDtoNoNulo(dto);

        normalizar(dto);
        validar(dto);
        calcularFechaVencimiento(dto);

        Integer idUsuario = usuarioSesionService.idUsuario();
        LocalDateTime ahora = LocalDateTime.now();

        /*
         * Garantiza que siempre se cree un registro nuevo,
         * aunque el frontend envíe un identificador.
         */
        dto.setIdBienInmuebleAvaluo(null);

        dto.setFechaCreacion(ahora);
        dto.setFechaEdicion(ahora);

        dto.setFkSeguridadCreacion(idUsuario);
        dto.setFkSeguridadEdicion(idUsuario);

        return repository.save(dto);
    }

    public Optional<BienInmuebleAvaluo> actualizar(
            Long id,
            BienInmuebleAvaluo dto
    ) {
        validarIdAvaluo(id);
        validarDtoNoNulo(dto);

        Optional<BienInmuebleAvaluo> registro =
                repository.findById(id);

        if (registro.isEmpty()) {
            return Optional.empty();
        }

        BienInmuebleAvaluo existente =
                registro.get();

        /*
         * El avalúo conserva el bien al cual pertenece.
         * No se permite cambiar idBien durante la edición.
         */
        dto.setIdBien(existente.getIdBien());

        normalizar(dto);
        validar(dto);
        calcularFechaVencimiento(dto);

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        dto.setIdBienInmuebleAvaluo(id);

        dto.setFechaCreacion(
                existente.getFechaCreacion()
        );

        dto.setFkSeguridadCreacion(
                existente.getFkSeguridadCreacion()
        );

        dto.setFechaEdicion(
                LocalDateTime.now()
        );

        dto.setFkSeguridadEdicion(
                idUsuario
        );

        return Optional.of(
                repository.save(dto)
        );
    }

    public boolean eliminar(Long id) {
        validarIdAvaluo(id);

        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);

        return true;
    }

    private void validarDtoNoNulo(
            BienInmuebleAvaluo dto
    ) {
        if (dto == null) {
            throw new IllegalArgumentException(
                    "La información del avalúo es obligatoria."
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

    private void validarIdAvaluo(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El avalúo es obligatorio."
            );
        }
    }

    private void validar(BienInmuebleAvaluo dto) {
        validarDtoNoNulo(dto);
        validarIdBien(dto.getIdBien());

        if (dto.getFechaAvaluo() == null) {
            throw new IllegalArgumentException(
                    "La fecha del avalúo es obligatoria."
            );
        }

        if (dto.getFechaAvaluo().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "La fecha del avalúo no puede ser posterior a la fecha actual."
            );
        }

        if (dto.getVigenciaAnios() == null
                || dto.getVigenciaAnios() <= 0) {

            throw new IllegalArgumentException(
                    "La vigencia del avalúo debe ser mayor que cero."
            );
        }

        if (dto.getVigenciaAnios() > VIGENCIA_MAXIMA_ANIOS) {
            throw new IllegalArgumentException(
                    "La vigencia del avalúo no puede superar "
                            + VIGENCIA_MAXIMA_ANIOS
                            + " años."
            );
        }

        validarValorObligatorioNoNegativo(
                dto.getValorAvaluoComercial(),
                "El valor del avalúo comercial es obligatorio.",
                "El valor del avalúo comercial no puede ser negativo."
        );

        validarValorObligatorioNoNegativo(
                dto.getValorAvaluoCatastral(),
                "El valor del avalúo catastral es obligatorio.",
                "El valor del avalúo catastral no puede ser negativo."
        );

        validarValorNoNegativo(
                dto.getValorTerreno(),
                "El valor del terreno no puede ser negativo."
        );

        validarValorNoNegativo(
                dto.getValorConstruccion(),
                "El valor de la construcción no puede ser negativo."
        );

        validarValorNoNegativo(
                dto.getValorCultivos(),
                "El valor de los cultivos no puede ser negativo."
        );

        validarValorNoNegativo(
                dto.getValorOtros(),
                "El valor de otros componentes no puede ser negativo."
        );

        validarLongitud(
                dto.getEntidadAvaluadora(),
                150,
                "La entidad avaluadora no puede superar 150 caracteres."
        );

        validarLongitud(
                dto.getNumeroInforme(),
                50,
                "El número de informe no puede superar 50 caracteres."
        );

        validarLongitud(
                dto.getObservaciones(),
                300,
                "Las observaciones no pueden superar 300 caracteres."
        );
    }

    private void normalizar(BienInmuebleAvaluo dto) {
        if (dto == null) {
            return;
        }

        /*
         * Estos campos son obligatorios y no se completan
         * automáticamente:
         *
         * - fechaAvaluo
         * - vigenciaAnios
         * - valorAvaluoComercial
         * - valorAvaluoCatastral
         */

        dto.setValorTerreno(
                valorOZero(dto.getValorTerreno())
        );

        dto.setValorConstruccion(
                valorOZero(dto.getValorConstruccion())
        );

        dto.setValorCultivos(
                valorOZero(dto.getValorCultivos())
        );

        dto.setValorOtros(
                valorOZero(dto.getValorOtros())
        );

        dto.setEntidadAvaluadora(
                limpiar(dto.getEntidadAvaluadora())
        );

        dto.setNumeroInforme(
                limpiar(dto.getNumeroInforme())
        );

        dto.setObservaciones(
                limpiar(dto.getObservaciones())
        );
    }

    private void calcularFechaVencimiento(
            BienInmuebleAvaluo dto
    ) {
        dto.setFechaVencimientoAvaluo(
                dto.getFechaAvaluo()
                        .plusYears(dto.getVigenciaAnios())
        );
    }

    private BigDecimal valorOZero(
            BigDecimal valor
    ) {
        return valor == null
                ? BigDecimal.ZERO
                : valor;
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

    private void validarLongitud(
            String valor,
            int maximo,
            String mensaje
    ) {
        if (valor != null && valor.length() > maximo) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private void validarValorObligatorioNoNegativo(
            BigDecimal valor,
            String mensajeObligatorio,
            String mensajeNegativo
    ) {
        if (valor == null) {
            throw new IllegalArgumentException(
                    mensajeObligatorio
            );
        }

        validarValorNoNegativo(
                valor,
                mensajeNegativo
        );
    }

    private void validarValorNoNegativo(
            BigDecimal valor,
            String mensaje
    ) {
        if (valor != null
                && valor.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(mensaje);
        }
    }
}