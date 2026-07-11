package co.assip.erp.hojavida.bienesvehiculos;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BienVehiculoSeguroService {

    private final BienVehiculoSeguroRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    public BienVehiculoSeguroService(
            BienVehiculoSeguroRepository repository,
            UsuarioSesionService usuarioSesionService
    ) {
        this.repository = repository;
        this.usuarioSesionService = usuarioSesionService;
    }

    public List<BienVehiculoSeguro> listarPorBien(Long idBien) {
        validarIdBien(idBien);
        return repository.findByIdBienOrderByFechaVencimientoSeguroDescFechaCreacionDesc(idBien);
    }

    public Optional<BienVehiculoSeguro> buscarPorId(Long id) {
        validarIdSeguro(id);
        return repository.findById(id);
    }

    public BienVehiculoSeguro crear(BienVehiculoSeguro dto) {
        normalizar(dto);
        validar(dto);

        Integer idUsuario = usuarioSesionService.idUsuario();

        dto.setFechaCreacion(LocalDateTime.now());
        dto.setFechaEdicion(LocalDateTime.now());
        dto.setFkSeguridadCreacion(idUsuario);
        dto.setFkSeguridadEdicion(idUsuario);

        return repository.save(dto);
    }

    public Optional<BienVehiculoSeguro> actualizar(
            Long id,
            BienVehiculoSeguro dto
    ) {
        validarIdSeguro(id);
        normalizar(dto);
        validar(dto);

        return repository.findById(id).map(existente -> {
            Integer idUsuario = usuarioSesionService.idUsuario();

            dto.setIdBienVehiculoSeguro(id);
            dto.setFechaCreacion(existente.getFechaCreacion());
            dto.setFkSeguridadCreacion(existente.getFkSeguridadCreacion());

            dto.setFechaEdicion(LocalDateTime.now());
            dto.setFkSeguridadEdicion(idUsuario);

            return repository.save(dto);
        });
    }

    public boolean eliminar(Long id) {
        validarIdSeguro(id);

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

    private void validarIdSeguro(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El seguro del vehículo es obligatorio.");
        }
    }

    private void validar(BienVehiculoSeguro dto) {
        if (dto == null) {
            throw new IllegalArgumentException("La información del seguro del vehículo es obligatoria.");
        }

        validarIdBien(dto.getIdBien());

        if (dto.getValorAsegurado() == null) {
            throw new IllegalArgumentException("El valor asegurado es obligatorio.");
        }

        if (dto.getValorAsegurado().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor asegurado no puede ser negativo.");
        }

        if (dto.getFechaInicioSeguro() != null
                && dto.getFechaVencimientoSeguro() != null
                && dto.getFechaVencimientoSeguro().isBefore(dto.getFechaInicioSeguro())) {
            throw new IllegalArgumentException("La fecha de vencimiento no puede ser anterior a la fecha de inicio.");
        }

        if (dto.getEstadoSeguro() == null || dto.getEstadoSeguro().isBlank()) {
            throw new IllegalArgumentException("El estado del seguro es obligatorio.");
        }

        if (!dto.getEstadoSeguro().matches("A|I|V")) {
            throw new IllegalArgumentException("El estado del seguro debe ser A, I o V.");
        }

        validarLongitud(dto.getAseguradora(), 150,
                "La aseguradora no puede superar 150 caracteres.");

        validarLongitud(dto.getNumeroPoliza(), 50,
                "El número de póliza no puede superar 50 caracteres.");

        validarLongitud(dto.getObservaciones(), 1000,
                "Las observaciones no pueden superar 1000 caracteres.");
    }

    private void normalizar(BienVehiculoSeguro dto) {
        if (dto == null) return;

        if (dto.getValorAsegurado() == null) {
            dto.setValorAsegurado(BigDecimal.ZERO);
        }

        dto.setAseguradora(limpiar(dto.getAseguradora()));
        dto.setNumeroPoliza(limpiar(dto.getNumeroPoliza()));
        dto.setObservaciones(limpiar(dto.getObservaciones()));

        if (dto.getEstadoSeguro() == null || dto.getEstadoSeguro().isBlank()) {
            dto.setEstadoSeguro("A");
        } else {
            dto.setEstadoSeguro(dto.getEstadoSeguro().trim().toUpperCase());
        }
    }

    private String limpiar(String valor) {
        if (valor == null) return null;

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