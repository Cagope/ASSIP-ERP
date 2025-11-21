package co.assip.erp.contabilidad.plan_cuentas;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlanCuentaService {

    private final PlanCuentaRepository repository;

    public PlanCuentaService(PlanCuentaRepository repository) {
        this.repository = repository;
    }

    // ==========================================================
    // OBTENER DETALLE
    // ==========================================================
    public PlanCuenta obtener(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada."));
    }

    // ==========================================================
    // LISTAR TODAS POR AGENCIA
    // ==========================================================
    public List<PlanCuenta> listarPorAgencia(Integer idAgencia) {
        return repository.findByIdAgenciaOrderByCodigoCuentaAsc(idAgencia);
    }

    // ==========================================================
    // GUARDAR
    // ==========================================================
    public PlanCuenta guardar(PlanCuenta dto) {

        // 1) VALIDAR JERARQUÍA
        validarJerarquia(dto);

        // 2) VALIDAR OPERABLE
        validarOperable(dto);

        // 3) Guardar
        return repository.save(dto);
    }

    // ==========================================================
    // VALIDAR JERARQUÍA DE BLOQUES
    // ==========================================================
    private void validarJerarquia(PlanCuenta dto) {

        String codigo = dto.getCodigoCuenta().trim();
        int len = codigo.length();

        // Nivel raíz
        if (len == 1) {
            dto.setNivel(1);
            return;
        }

        // Validar longitudes permitidas
        if (!(len == 1 || len == 2 || len == 4 || len == 6 || len == 8 || len == 11)) {
            throw new IllegalArgumentException(
                    "Longitud del código inválida. Debe ser 1, 2, 4, 6, 8 o 11 dígitos."
            );
        }

        // Calcular nivel
        int nivel = switch (len) {
            case 2 -> 2;
            case 4 -> 3;
            case 6 -> 4;
            case 8 -> 5;
            case 11 -> 6;
            default -> 1;
        };
        dto.setNivel(nivel);

        // Obtener código padre
        String padre = switch (len) {
            case 2 -> codigo.substring(0, 1);
            case 4 -> codigo.substring(0, 2);
            case 6 -> codigo.substring(0, 4);
            case 8 -> codigo.substring(0, 6);
            case 11 -> codigo.substring(0, 8);
            default -> null;
        };

        if (padre == null) return;

        // Buscar cuenta padre
        PlanCuenta cuentaPadre =
                repository.findByIdAgenciaAndCodigoCuenta(dto.getIdAgencia(), padre)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El padre (" + padre + ") no existe para la agencia " + dto.getIdAgencia()
                                )
                        );


        // Padre no puede ser operable
        if (Boolean.TRUE.equals(cuentaPadre.getOperable())) {
            throw new IllegalArgumentException(
                    "El padre (" + padre + ") NO puede ser operable."
            );
        }
    }

    // ==========================================================
    // VALIDAR OPERABILIDAD
    // ==========================================================
    private void validarOperable(PlanCuenta dto) {

        boolean esOperable = Boolean.TRUE.equals(dto.getOperable());
        int nivel = dto.getNivel();

        // Solo niveles 5 y 6 pueden ser operables
        if (esOperable && !(nivel == 5 || nivel == 6)) {
            throw new IllegalArgumentException(
                    "Solo las cuentas de nivel 5 o 6 pueden ser operables."
            );
        }

        // Si es operable, no puede tener hijas
        if (esOperable) {
            List<PlanCuenta> hijas = repository.buscarHijas(
                    dto.getIdAgencia(),
                    dto.getCodigoCuenta().trim()
            );

            if (!hijas.isEmpty()) {
                throw new IllegalArgumentException(
                        "Esta cuenta no puede ser operable porque tiene subcuentas registradas."
                );
            }
        }
    }

    // ==========================================================
    // ELIMINAR
    // ==========================================================
    public void eliminar(Integer id) {

        PlanCuenta cuenta = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cuenta no encontrada."));

        // Validar: NO eliminar si tiene hijas
        List<PlanCuenta> hijas =
                repository.buscarHijas(cuenta.getIdAgencia(), cuenta.getCodigoCuenta().trim());

        if (!hijas.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se puede eliminar: la cuenta tiene subcuentas asociadas."
            );
        }

        repository.delete(cuenta);
    }
}
