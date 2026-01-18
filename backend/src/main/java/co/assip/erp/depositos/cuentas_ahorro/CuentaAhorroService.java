package co.assip.erp.depositos.cuentas_ahorro;

import co.assip.erp.depositos.cuentas_ahorro.dto.*;
import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CuentaAhorroService {

    private final CuentaAhorroRepository repository;

    // ============================================================
    // 🟦 LISTAR CUENTAS SEGÚN AGENCIAS DEL USUARIO  ⭐ CORREGIDO
    // ============================================================
    public List<CuentaAhorroDTO> listarCuentas() {

        var agencias = SecurityUtils.getAgencias();
        boolean esAdmin = SecurityUtils.tieneAccesoTotal();
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

        if (!SecurityUtils.tieneAccesoTotal() &&
                !SecurityUtils.perteneceA(idAgencia)) {

            return List.of();
        }

        return repository.listarPorAgencia(idAgencia);
    }

    // ============================================================
    // 🟦 DETALLE SEGÚN AGENCIA DEL USUARIO
    // ============================================================
    public CuentaAhorroDetalleDTO obtenerPorId(Integer id) {

        var agencias = SecurityUtils.getAgencias();

        if (!SecurityUtils.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(id, agencias)) {
            return null; // o lanzar 403
        }

        CuentaAhorroDetalleDTO dto = repository.obtenerDetalle(id);
        if (dto != null) {
            dto.setBeneficiarios(repository.listarBeneficiarios(id));
            dto.setPoderes(repository.listarPoderes(id));
        }
        return dto;
    }

    // ============================================================
    // 🟦 VALIDAR ANTES DE GUARDAR
    // ============================================================
    public CuentaAhorroGuardarRespuesta validarAntesDeGuardar(CuentaAhorroGuardarDTO dto) {

        if (!SecurityUtils.tieneAccesoTotal() &&
                !SecurityUtils.perteneceA(dto.getIdAgencia())) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para crear cuentas en esta agencia.",
                    null
            );
        }

        Integer idPer = dto.getIdDatosPersonal();
        Integer idForma = dto.getIdFormaAhorro();

        if (idForma != null) {
            CuentaAhorroDetalleDTO existente = repository.buscarCuentaAportes(idPer);

            if (existente != null && existente.getSaldoActualCuenta() > 0) {
                return new CuentaAhorroGuardarRespuesta(
                        false,
                        "El asociado ya tiene cuenta de aportes con saldo > 0.",
                        null
                );
            }
        }

        if (dto.getTasa() != null && dto.getTasa() < 0) {
            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "La tasa no puede ser negativa.",
                    null
            );
        }

        return new CuentaAhorroGuardarRespuesta(true, "Validación exitosa.", null);
    }

    // ============================================================
    // 🟦 GUARDAR
    // ============================================================
    public CuentaAhorroGuardarRespuesta guardar(CuentaAhorroGuardarDTO dto) {

        Integer idUsuario = SecurityUtils.getIdUsuario();
        if (idUsuario == null) {
            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "Usuario no autenticado.",
                    null
            );
        }

        var valid = validarAntesDeGuardar(dto);
        if (!valid.isOk()) return valid;

        Integer consecutivo = repository.obtenerConsecutivo(dto.getIdFormaAhorro());
        String codigoCuenta = consecutivo.toString();

        String fechaFinal = null;
        if (dto.getPlazoCuenta() != null && dto.getPlazoCuenta() > 0) {
            fechaFinal = LocalDate.now()
                    .plusMonths(dto.getPlazoCuenta())
                    .toString();
        }

        Integer idCuenta = repository.guardarCuenta(dto, codigoCuenta, fechaFinal);

        repository.actualizarConsecutivo(dto.getIdFormaAhorro());
        repository.guardarBeneficiarios(idCuenta, dto.getBeneficiarios(), idUsuario);
        repository.guardarPoderes(idCuenta, dto.getPoderes(), idUsuario);

        return new CuentaAhorroGuardarRespuesta(
                true,
                "Cuenta creada exitosamente.",
                idCuenta
        );
    }

    // ============================================================
    // 🟦 ELIMINAR (solo si pertenece a su agencia)
    // ============================================================
    public CuentaAhorroGuardarRespuesta eliminar(Integer idCuenta) {

        var agencias = SecurityUtils.getAgencias();

        if (!SecurityUtils.tieneAccesoTotal() &&
                !repository.cuentaPerteneceAgencias(idCuenta, agencias)) {

            return new CuentaAhorroGuardarRespuesta(
                    false,
                    "No tiene permiso para eliminar cuentas de otra agencia.",
                    null
            );
        }

        int movs = repository.contarMovimientos(idCuenta);

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
