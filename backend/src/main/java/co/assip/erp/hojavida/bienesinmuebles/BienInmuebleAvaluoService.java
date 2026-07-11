package co.assip.erp.hojavida.bienesinmueblesavaluos;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BienInmuebleAvaluoService {

    private final BienInmuebleAvaluoRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public BienInmuebleAvaluoService(
            BienInmuebleAvaluoRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    public List<BienInmuebleAvaluo> listarPorBien(Long idBien) {
        validarIdBien(idBien);
        return repository.findByIdBienOrderByFechaAvaluoDescFechaCreacionDesc(idBien);
    }

    public Optional<BienInmuebleAvaluo> buscarPorId(Long id) {
        validarIdAvaluo(id);
        return repository.findById(id);
    }

    public BienInmuebleAvaluo crear(BienInmuebleAvaluo dto) {
        normalizar(dto);
        validar(dto);
        calcularFechaVencimiento(dto);

        Integer idUsuario = usuarioSesionService.idUsuario();

        dto.setFechaCreacion(LocalDateTime.now());
        dto.setFechaEdicion(LocalDateTime.now());
        dto.setFkSeguridadCreacion(idUsuario);
        dto.setFkSeguridadEdicion(idUsuario);

        return repository.save(dto);
    }

    public Optional<BienInmuebleAvaluo> actualizar(
            Long id,
            BienInmuebleAvaluo dto
    ) {
        validarIdAvaluo(id);
        normalizar(dto);
        validar(dto);
        calcularFechaVencimiento(dto);

        return repository.findById(id).map(existente -> {
            Integer idUsuario = usuarioSesionService.idUsuario();

            dto.setIdBienInmuebleAvaluo(id);
            dto.setFechaCreacion(existente.getFechaCreacion());
            dto.setFkSeguridadCreacion(existente.getFkSeguridadCreacion());

            dto.setFechaEdicion(LocalDateTime.now());
            dto.setFkSeguridadEdicion(idUsuario);

            return repository.save(dto);
        });
    }

    public boolean eliminar(Long id) {
        validarIdAvaluo(id);

        if (!repository.existsById(id)) {
            return false;
        }

        repository.deleteById(id);
        return true;
    }

    private void validarIdBien(Long idBien) {
        if (idBien == null || idBien <= 0) {
            throw new IllegalArgumentException("El bien es obligatorio.");
        }
    }

    private void validarIdAvaluo(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El avalúo es obligatorio.");
        }
    }

    private void validar(BienInmuebleAvaluo dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La información del avalúo es obligatoria.");
        }

        validarIdBien(dto.getIdBien());

        if (dto.getFechaAvaluo() == null) {
            throw new IllegalArgumentException("La fecha del avalúo es obligatoria.");
        }

        if (dto.getVigenciaAnios() == null || dto.getVigenciaAnios() <= 0) {
            throw new IllegalArgumentException("La vigencia del avalúo debe ser mayor que cero.");
        }

        if (dto.getVigenciaAnios() > 30) {
            throw new IllegalArgumentException("La vigencia del avalúo no puede superar 30 años.");
        }

        if (dto.getValorAvaluoComercial() == null) {
            throw new IllegalArgumentException("El valor del avalúo comercial es obligatorio.");
        }

        if (dto.getValorAvaluoComercial().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor del avalúo comercial no puede ser negativo.");
        }

        if (dto.getValorAvaluoCatastral() == null) {
            throw new IllegalArgumentException("El valor del avalúo catastral es obligatorio.");
        }

        if (dto.getValorAvaluoCatastral().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor del avalúo catastral no puede ser negativo.");
        }

        validarLongitud(dto.getEntidadAvaluadora(), 150,
                "La entidad avaluadora no puede superar 150 caracteres.");

        validarLongitud(dto.getNumeroInforme(), 50,
                "El número de informe no puede superar 50 caracteres.");

        validarLongitud(dto.getObservaciones(), 1000,
                "Las observaciones no pueden superar 1000 caracteres.");
    }

    private void normalizar(BienInmuebleAvaluo dto) {
        if (dto == null) {
            return;
        }

        if (dto.getVigenciaAnios() == null || dto.getVigenciaAnios() <= 0) {
            dto.setVigenciaAnios(3);
        }

        if (dto.getValorAvaluoComercial() == null) {
            dto.setValorAvaluoComercial(BigDecimal.ZERO);
        }

        if (dto.getValorAvaluoCatastral() == null) {
            dto.setValorAvaluoCatastral(BigDecimal.ZERO);
        }

        dto.setEntidadAvaluadora(limpiar(dto.getEntidadAvaluadora()));
        dto.setNumeroInforme(limpiar(dto.getNumeroInforme()));
        dto.setObservaciones(limpiar(dto.getObservaciones()));
    }

    private void calcularFechaVencimiento(BienInmuebleAvaluo dto) {
        dto.setFechaVencimientoAvaluo(
                dto.getFechaAvaluo().plusYears(dto.getVigenciaAnios())
        );
    }

    private String limpiar(String valor) {
        if (valor == null) {
            return null;
        }

        String limpio = valor.trim();

        return limpio.isEmpty() ? null : limpio;
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