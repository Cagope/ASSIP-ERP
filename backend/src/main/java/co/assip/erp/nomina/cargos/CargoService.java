package co.assip.erp.nomina.cargos;

import co.assip.erp.nomina.cargos.dto.CargoDTO;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CargoService {

    private final CargoRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    // ============================================================
    // ✅ LISTAR
    // ============================================================
    public List<CargoDTO> listar() {
        return repository.listar();
    }

    // ============================================================
    // ✅ OBTENER
    // ============================================================
    public CargoDTO obtener(Integer idCargo) {
        return repository.obtener(idCargo);
    }

    // ============================================================
    // ✅ CREAR
    // ============================================================
    public Integer crear(CargoDTO dto) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        return repository.crear(dto, idUsuario);
    }

    // ============================================================
    // ✅ ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idCargo, CargoDTO dto) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        repository.actualizar(idCargo, dto, idUsuario);
    }

    // ============================================================
    // ✅ DESACTIVAR
    // ============================================================
    public void desactivar(Integer idCargo) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        repository.desactivar(idCargo, idUsuario);
    }
}
