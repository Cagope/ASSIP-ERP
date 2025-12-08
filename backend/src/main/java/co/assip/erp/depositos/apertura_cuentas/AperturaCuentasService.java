package co.assip.erp.depositos.apertura_cuentas;

import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentaItemDTO;
import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentasEntradaDTO;
import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentasRespuestaDTO;
import co.assip.erp.depositos.apertura_cuentas.repository.AperturaCuentasRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AperturaCuentasService {

    private final AperturaCuentasRepository repo;

    // ============================================================
    // 🔹 1. Listar formas aplicando reglas REALES
    // ============================================================
    public List<AperturaCuentaItemDTO> listarFormas(Integer idDatosPersonal, List<Integer> agenciasUsuario) {

        System.out.println("🚨 ID persona: " + idDatosPersonal);

        // ✔ Validación única válida:
        int cuentasConSaldo = repo.contarCuentasConSaldo(idDatosPersonal);

        System.out.println("🔎 Total cuentas con saldo: " + cuentasConSaldo);

        if (cuentasConSaldo > 0) {
            System.out.println("🚫 Bloqueado: tiene cuentas activas con saldo");
            return Collections.emptyList();
        }

        Integer agenciaUsuario = null;
        if (agenciasUsuario != null && !agenciasUsuario.isEmpty()) {
            agenciaUsuario = agenciasUsuario.get(0);
            System.out.println("✔ Agencia operativa: " + agenciaUsuario);
        }

        boolean tieneAportesActivos = repo.tieneAportesActivos(idDatosPersonal);

        var lista = repo.listarFormasDisponibles(idDatosPersonal, agenciaUsuario);

        if (tieneAportesActivos) {
            lista.stream()
                    .filter(x -> "01".equals(x.getCodigoForma()))
                    .forEach(x -> x.setObservacion("⚠️ Ya posee cuenta de aportes con saldo"));
        }

        return lista;
    }

    // ============================================================
    // 🔹 2. Crear cuenta
    // ============================================================
    public AperturaCuentasRespuestaDTO crear(AperturaCuentasEntradaDTO dto) {

        AperturaCuentasRespuestaDTO res = new AperturaCuentasRespuestaDTO();

        if (dto.getIdDatosPersonal() == null || dto.getIdDatosPersonal() <= 0) {
            res.setOk(false);
            res.setMensaje("Asociado no válido.");
            return res;
        }

        if (dto.getIdAgenciaUsuario() == null) {
            res.setOk(false);
            res.setMensaje("Agencia no válida en la sesión.");
            return res;
        }

        boolean esAportes = dto.getIdFormaAhorro() != null && dto.getIdFormaAhorro() == 1;
        boolean tieneAportesActivos = repo.tieneAportesActivos(dto.getIdDatosPersonal());

        if (esAportes && tieneAportesActivos) {
            res.setOk(false);
            res.setMensaje("El asociado ya tiene una cuenta de aportes activa.");
            return res;
        }

        String gmf = dto.getGmf();
        Boolean retencion = dto.getRetencion();

        if (esAportes) {
            gmf = "N";
            retencion = false;
        }

        // Código con 10 dígitos
        Integer consecutivo = repo.obtenerConsecutivo(dto.getIdFormaAhorro());
        String codigoForma = repo.obtenerCodigoForma(dto.getIdFormaAhorro());
        String codigo = codigoForma + "-" + String.format("%010d", consecutivo);

        // 🔹 Crear cuenta
        Integer idCuenta = repo.crearCuenta(
                dto.getIdFormaAhorro(),
                dto.getIdDatosPersonal(),
                dto.getIdAgenciaUsuario(),
                codigo,
                gmf,
                retencion,
                dto.getUsuarioId()
        );

        // =============================
        // 🔹 Guardar apoderado aportes
        // =============================
        repo.guardarApoderadoBasico(
                idCuenta,
                dto.getDocumentoApoderadoAportes(),
                dto.getNombreApoderadoAportes(),
                dto.getTelefonoApoderadoAportes(),
                dto.getCelularApoderadoAportes(),
                dto.getUsuarioId()
        );

        // =============================
        // 🔹 Guardar apoderado ahorro opcional
        // =============================
        repo.guardarApoderadoBasico(
                idCuenta,
                dto.getDocumentoApoderadoAhorro(),
                dto.getNombreApoderadoAhorro(),
                dto.getTelefonoApoderadoAhorro(),
                dto.getCelularApoderadoAhorro(),
                dto.getUsuarioId()
        );

        // 🔹 Actualizar consecutivo
        repo.actualizarConsecutivo(dto.getIdFormaAhorro());

        res.setOk(true);
        res.setIdCuentaAhorro(idCuenta);
        res.setCodigoCuenta(codigo);
        res.setMensaje("Cuenta creada exitosamente.");

        return res;
    }
}
