package co.assip.erp.cajas.operaciones_pendientes;

import co.assip.erp.cajas.operaciones_pendientes.dto.CajaOperacionPendienteDTO;
import co.assip.erp.cajas.provisiones.CajasUsuariosService;
import co.assip.erp.seguridad.sesion.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CajasOperacionesPendientesService {

    private final CajasOperacionesPendientesRepository repository;
    private final CajasUsuariosService cajasUsuariosService;
    private final UsuarioSesionService usuarioSesionService;


    // =========================================================
    // LISTAR
    // =========================================================

    public List<CajaOperacionPendienteDTO> listarPendientes(
            Long idCaja,
            LocalDate fechaContable
    ) {

        validarIdCaja(idCaja);

        if (fechaContable == null) {
            throw new IllegalArgumentException(
                    "Debe indicar la fecha contable para consultar las operaciones pendientes."
            );
        }

        /*
         * La bandeja pertenece a la caja que está operando
         * el usuario autenticado.
         */
        cajasUsuariosService.validarCajaUsuario(
                idCaja
        );

        return repository.listarPendientes(
                idCaja,
                fechaContable
        );
    }


    // =========================================================
    // CONFIRMAR
    // =========================================================

    @Transactional
    public void confirmar(
            Long idMovimientoCaja,
            Long idCaja
    ) {

        if (
                idMovimientoCaja == null
                        || idMovimientoCaja <= 0
        ) {
            throw new IllegalArgumentException(
                    "Debe indicar la operación pendiente."
            );
        }

        validarIdCaja(idCaja);

        /*
         * El usuario solamente puede confirmar movimientos
         * direccionados a la caja que tiene vinculada.
         */
        cajasUsuariosService.validarCajaUsuario(
                idCaja
        );

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        int actualizados =
                repository.confirmar(
                        idMovimientoCaja,
                        idCaja,
                        idUsuario
                );

        if (actualizados != 1) {
            throw new RuntimeException(
                    "La operación ya fue atendida, no se encuentra pendiente o no pertenece a la caja actual."
            );
        }
    }


    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validarIdCaja(
            Long idCaja
    ) {

        if (
                idCaja == null
                        || idCaja <= 0
        ) {
            throw new IllegalArgumentException(
                    "Debe indicar una caja válida."
            );
        }
    }
}