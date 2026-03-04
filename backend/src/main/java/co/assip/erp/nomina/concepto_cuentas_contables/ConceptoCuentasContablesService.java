package co.assip.erp.nomina.concepto_cuentas_contables;

import co.assip.erp.nomina.concepto_cuentas_contables.dto.ConceptoCuentaContableFormDTO;
import co.assip.erp.nomina.concepto_cuentas_contables.dto.ConceptoCuentaContableListDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConceptoCuentasContablesService {

    private final ConceptoCuentasContablesRepository repository;

    // ============================================================
    // LISTAR (por agencia)
    // ============================================================
    public List<ConceptoCuentaContableListDTO> listar() {
        return repository.listar();
    }

    // ============================================================
    // OBTENER (por id)
    // ============================================================
    public ConceptoCuentaContableFormDTO obtener(Integer idMapeo) {

        return repository.obtener(idMapeo)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No existe mapeo de concepto contable con id: " + idMapeo
                        ));
    }

    // ============================================================
    // CREAR
    // ============================================================
    public void crear(ConceptoCuentaContableFormDTO dto, Integer idUsuario) {

        // Validaciones mínimas
        if (dto.getCodigoConcepto() == null || dto.getCodigoConcepto().isBlank()) {
            throw new RuntimeException("El código del concepto es obligatorio");
        }
        if (dto.getIdAgencia() == null) {
            throw new RuntimeException("La agencia es obligatoria");
        }

        // Validación de unicidad lógica
        if (repository.existeActivo(dto.getCodigoConcepto(), dto.getIdAgencia())) {
            throw new RuntimeException(
                    "Ya existe un mapeo activo para el concepto "
                            + dto.getCodigoConcepto()
                            + " en la agencia seleccionada"
            );
        }

        repository.crear(dto, idUsuario);
    }

    // ============================================================
    // ACTUALIZAR
    // ============================================================
    public void actualizar(Integer idMapeo,
                           ConceptoCuentaContableFormDTO dto,
                           Integer idUsuario) {

        // Validar existencia
        ConceptoCuentaContableFormDTO actual = obtener(idMapeo);

        // Regla: NO permitir cambiar concepto ni agencia
        if (!actual.getCodigoConcepto().equals(dto.getCodigoConcepto())) {
            throw new RuntimeException("No se permite cambiar el código del concepto");
        }
        if (!actual.getIdAgencia().equals(dto.getIdAgencia())) {
            throw new RuntimeException("No se permite cambiar la agencia del mapeo");
        }

        repository.actualizar(idMapeo, dto, idUsuario);
    }

    // ============================================================
    // ELIMINAR (SOFT DELETE)
    // ============================================================
    public void eliminar(Integer idMapeo, Integer idUsuario) {

        // Validar existencia
        obtener(idMapeo);

        repository.eliminar(idMapeo, idUsuario);
    }
}