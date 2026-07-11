package co.assip.erp.depositos.cuentas_ahorro;

import co.assip.erp.depositos.cuentas_ahorro.dto.*;
import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CuentaAhorroService {

    private final CuentaAhorroRepository repository;
    private final UsuarioSesionService usuarioSesionService;

    // ============================================================
    // 🟦 LISTAR CUENTAS SEGÚN AGENCIAS DEL USUARIO  ⭐ CORREGIDO
    // ============================================================
    public List<CuentaAhorroDTO> listarCuentas() {

        var agencias =
                usuarioSesionService.agencias();

        boolean esAdmin =
                usuarioSesionService.tieneAccesoTotal();
        boolean tieneVarias = agencias != null && agencias.size() > 1;

        // ⭐ ADMIN o usuario con varias agencias → ver TODO
        if (esAdmin || tieneVarias) {
            return repository.listarTodas();
        }

        // ⭐ Usuario con 1 sola agencia → filtrar
        if (agencias != null && !agencias.isEmpty()) {
            return repository.listarPorAgencia(agencias.get(0));
        }

        // ⭐ Si no tiene agencias → lista vacía (no rompe)
        return List.of();
    }

    // ============================================================
    // 🟦 LISTAR POR AGENCIA ESPECÍFICA (solo si pertenece)
    // ============================================================
    public List<CuentaAhorroDTO> listarPorAgencia(Integer idAgencia) {

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !usuarioSesionService.perteneceA(idAgencia)) {

            return List.of();
        }

        return repository.listarPorAgencia(idAgencia);
    }

    // ============================================================
    // 🟦 DETALLE SEGÚN AGENCIA DEL USUARIO
    // ============================================================
    public CuentaAhorroDetalleDTO obtenerPorId(Integer id) {

        var agencias =
                usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(id, agencias)) {

            return null; // o lanzar 403
        }

        CuentaAhorroDetalleDTO dto =
                repository.obtenerDetalle(id);

        if (dto != null) {
            dto.setBeneficiarios(
                    repository.listarBeneficiarios(id)
            );

            dto.setPoderes(
                    repository.listarPoderes(id)
            );

            dto.setCuentasConjuntas(
                    repository.listarCuentasConjuntas(id)
            );
        }

        return dto;
    }

    // ============================================================
    // 🟦 VALIDAR ANTES DE GUARDAR
    // ============================================================
    public CuentaAhorroGuardarRespuesta validarAntesDeGuardar(
            CuentaAhorroGuardarDTO dto
    ) {

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !usuarioSesionService.perteneceA(dto.getIdAgencia())) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para crear cuentas en esta agencia.",
                    null
            );
        }

        Integer idPer = dto.getIdDatosPersonal();
        Integer idForma = dto.getIdFormaAhorro();

        if (idForma != null) {

            String codigoForma =
                    repository.obtenerCodigoForma(idForma);

            if ("01".equals(codigoForma)) {

                CuentaAhorroDetalleDTO existente =
                        repository.buscarCuentaAportes(idPer);

                if (existente != null) {

                    return new CuentaAhorroGuardarRespuesta(
                            false,
                            "El asociado ya tiene cuenta de aportes sociales.",
                            null
                    );
                }
            }
        }

        if (dto.getTasa() != null && dto.getTasa() < 0) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "La tasa no puede ser negativa.",
                    null
            );
        }

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Validación exitosa.",
                null
        );
    }

    // ============================================================
    // 🟦 GUARDAR
    // ============================================================
    public CuentaAhorroGuardarRespuesta guardar(
            CuentaAhorroGuardarDTO dto
    ) {

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        var valid =
                validarAntesDeGuardar(dto);

        if (!valid.isOk()) {
            return valid;
        }

        Integer consecutivo =
                repository.obtenerConsecutivo(
                        dto.getIdFormaAhorro()
                );

        if (consecutivo == null || consecutivo <= 0) {
            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No se pudo determinar el consecutivo de la forma de ahorro.",
                    null
            );
        }

        String codigoCuenta =
                String.format("%06d", consecutivo);

        String fechaFinal = null;

        if (dto.getPlazoCuenta() != null &&
                dto.getPlazoCuenta() > 0) {

            fechaFinal = LocalDate.now()
                    .plusMonths(dto.getPlazoCuenta())
                    .toString();
        }

        Integer idCuenta =
                repository.guardarCuenta(
                        dto,
                        codigoCuenta,
                        fechaFinal,
                        idUsuario
                );

        repository.actualizarConsecutivo(
                dto.getIdFormaAhorro()
        );

        repository.guardarBeneficiarios(
                idCuenta,
                dto.getBeneficiarios(),
                idUsuario
        );

        repository.guardarPoderes(
                idCuenta,
                dto.getPoderes(),
                idUsuario
        );

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Cuenta creada exitosamente.",
                idCuenta
        );
    }

    public List<CuentaConjuntaDTO> listarCuentasConjuntas(Integer idCuenta) {

        var agencias = usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return List.of();
        }

        return repository.listarCuentasConjuntas(idCuenta);
    }

    public CuentaAhorroGuardarRespuesta agregarCuentaConjunta(
            Integer idCuenta,
            CuentaConjuntaDTO dto
    ) {

        var agencias = usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para modificar esta cuenta.",
                    null
            );
        }

        if (dto.getIdDatosPersonal() == null || dto.getIdDatosPersonal() <= 0) {
            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "Seleccione el asociado conjunto.",
                    null
            );
        }

        if (dto.getCodigoAccion() == null || dto.getCodigoAccion().trim().isEmpty()) {
            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "Seleccione la acción conjunta.",
                    null
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

        repository.agregarCuentaConjunta(
                idCuenta,
                dto,
                idUsuario
        );

        repository.actualizarIndicadorCuentaConjunta(
                idCuenta,
                "S"
        );

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Cuenta conjunta agregada correctamente.",
                idCuenta
        );
    }

    public CuentaAhorroGuardarRespuesta eliminarCuentaConjunta(
            Integer idCuenta,
            Integer idCuentaConjunta
    ) {

        var agencias = usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para modificar esta cuenta.",
                    null
            );
        }

        repository.eliminarCuentaConjunta(
                idCuenta,
                idCuentaConjunta
        );

        int total = repository.contarCuentasConjuntas(idCuenta);

        repository.actualizarIndicadorCuentaConjunta(
                idCuenta,
                total > 0 ? "S" : "N"
        );

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Cuenta conjunta eliminada correctamente.",
                idCuenta
        );
    }


    // ============================================================
    // 🟦 BENEFICIARIOS
    // ============================================================
    public List<BeneficiarioDTO> listarBeneficiarios(Integer idCuenta) {

        var agencias = usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return List.of();
        }

        return repository.listarBeneficiarios(idCuenta);
    }

    public CuentaAhorroGuardarRespuesta agregarBeneficiario(
            Integer idCuenta,
            BeneficiarioDTO dto
    ) {

        var agencias = usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para modificar esta cuenta.",
                    null
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

        repository.agregarBeneficiario(
                idCuenta,
                dto,
                idUsuario
        );

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Beneficiario agregado correctamente.",
                idCuenta
        );
    }

    public CuentaAhorroGuardarRespuesta actualizarBeneficiario(
            Integer idCuenta,
            Integer idBeneficiario,
            BeneficiarioDTO dto
    ) {

        var agencias = usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para modificar esta cuenta.",
                    null
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

        repository.actualizarBeneficiario(
                idCuenta,
                idBeneficiario,
                dto,
                idUsuario
        );

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Beneficiario actualizado correctamente.",
                idCuenta
        );
    }

    public CuentaAhorroGuardarRespuesta eliminarBeneficiario(
            Integer idCuenta,
            Integer idBeneficiario
    ) {

        var agencias = usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para modificar esta cuenta.",
                    null
            );
        }

        repository.eliminarBeneficiario(
                idCuenta,
                idBeneficiario
        );

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Beneficiario eliminado correctamente.",
                idCuenta
        );
    }

    // ============================================================
    // 🟦 PODERES
    // ============================================================
    public List<PoderDTO> listarPoderes(Integer idCuenta) {

        var agencias = usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return List.of();
        }

        return repository.listarPoderes(idCuenta);
    }

    public CuentaAhorroGuardarRespuesta agregarPoder(
            Integer idCuenta,
            PoderDTO dto
    ) {

        var agencias = usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para modificar esta cuenta.",
                    null
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

        repository.agregarPoder(
                idCuenta,
                dto,
                idUsuario
        );

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Poder agregado correctamente.",
                idCuenta
        );
    }

    public CuentaAhorroGuardarRespuesta actualizarPoder(
            Integer idCuenta,
            Integer idPoder,
            PoderDTO dto
    ) {

        var agencias = usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para modificar esta cuenta.",
                    null
            );
        }

        Integer idUsuario = usuarioSesionService.idUsuario();

        repository.actualizarPoder(
                idCuenta,
                idPoder,
                dto,
                idUsuario
        );

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Poder actualizado correctamente.",
                idCuenta
        );
    }

    public CuentaAhorroGuardarRespuesta eliminarPoder(
            Integer idCuenta,
            Integer idPoder
    ) {

        var agencias = usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para modificar esta cuenta.",
                    null
            );
        }

        repository.eliminarPoder(
                idCuenta,
                idPoder
        );

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Poder eliminado correctamente.",
                idCuenta
        );
    }

    // ============================================================
    // 🟦 ELIMINAR (solo si pertenece a su agencia)
    // ============================================================
    public CuentaAhorroGuardarRespuesta eliminar(
            Integer idCuenta
    ) {

        var agencias =
                usuarioSesionService.agencias();

        if (!usuarioSesionService.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para eliminar cuentas de otra agencia.",
                    null
            );
        }

        int movs =
                repository.contarMovimientos(idCuenta);

        if (movs > 0) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "La cuenta tiene movimientos. No puede eliminarse.",
                    null
            );
        }

        repository.eliminar(idCuenta);

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Cuenta eliminada correctamente.",
                idCuenta
        );
    }
}
