package co.assip.erp.depositos.apertura_cuentas;

import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentaItemDTO;
import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentasEntradaDTO;
import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentasRespuestaDTO;
import co.assip.erp.depositos.apertura_cuentas.repository.AperturaCuentasRepository;

import co.assip.erp.seguridad.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AperturaCuentasService {

    private final AperturaCuentasRepository repo;

    // ============================================================
    // 🔹 1. LISTAR FORMAS
    // ============================================================
    public List<AperturaCuentaItemDTO> listarFormas(Integer idPersona) {

        int cuentasConSaldo = repo.contarCuentasConSaldo(idPersona);
        if (cuentasConSaldo > 0) {
            return Collections.emptyList();
        }

        boolean tieneAportesActivos = repo.tieneAportesActivos(idPersona);

        var agenciasUsuario = SecurityUtils.getAgencias();
        Integer agenciaFiltrar = null;

        if (agenciasUsuario != null && agenciasUsuario.size() == 1) {
            agenciaFiltrar = agenciasUsuario.get(0);
        }

        var lista = repo.listarFormasDisponibles(idPersona, agenciaFiltrar);

        if (agenciaFiltrar != null) {
            final Integer agenciaFinal = agenciaFiltrar;
            lista.removeIf(x -> !x.getIdAgencia().equals(agenciaFinal));
        }

        if (tieneAportesActivos) {
            lista.stream()
                    .filter(x -> "01".equals(x.getCodigoForma()))
                    .forEach(x -> x.setObservacion("⚠️ Ya posee cuenta de aportes con saldo"));
        }

        return lista;
    }

    // ============================================================
    // 🔹 2. CREAR DOS CUENTAS EN UNA TRANSACCIÓN
    // ============================================================
    @Transactional
    public AperturaCuentasRespuestaDTO crear(AperturaCuentasEntradaDTO dto) {

        System.out.println("➡️ INICIANDO PROCESO DE CREACIÓN DE 2 CUENTAS");

        AperturaCuentasRespuestaDTO res = new AperturaCuentasRespuestaDTO();

        if (dto.getIdDatosPersonal() == null || dto.getIdDatosPersonal() <= 0) {
            res.setOk(false);
            res.setMensaje("Asociado no válido.");
            return res;
        }

        var agencias = SecurityUtils.getAgencias();
        Integer idAgencia = (agencias != null && agencias.size() == 1) ? agencias.get(0) : null;

        if (idAgencia == null) {
            res.setOk(false);
            res.setMensaje("No se pudo determinar la agencia del usuario.");
            return res;
        }

        dto.setIdAgenciaUsuario(idAgencia);

        if (dto.getUsuarioId() == null || dto.getUsuarioId() <= 0) {
            res.setOk(false);
            res.setMensaje("Usuario no detectado en sesión.");
            return res;
        }

        Integer idPersona = dto.getIdDatosPersonal();
        Integer idFormaAhorroSeleccionada = dto.getIdFormaAhorro();

        // ============================================================
        // 🚫 VALIDACIÓN NUEVA — evitar duplicados aunque saldo = 0
        // ============================================================
        if (repo.existeCuentaActivaEnForma(idPersona, 1, idAgencia)) {
            res.setOk(false);
            res.setMensaje("El asociado ya tiene cuenta de APORTES activa en esta agencia.");
            return res;
        }

        if (repo.existeCuentaActivaEnForma(idPersona, idFormaAhorroSeleccionada, idAgencia)) {
            res.setOk(false);
            res.setMensaje("El asociado ya tiene una cuenta activa en esta forma de ahorro dentro de la misma agencia.");
            return res;
        }

        // ============================================================
        // 🔥 CUENTA 1 — APORTES
        // ============================================================
        System.out.println("🔵 creando cuenta de APORTES (01)");

        Integer consecutivoAportes =
                repo.incrementarYObtenerConsecutivo(1, idAgencia);

        String codigoAportes = String.format("%010d", consecutivoAportes);

        Integer idCuentaAportes = repo.crearCuenta(
                1,
                idPersona,
                idAgencia,
                codigoAportes,
                "N",
                false,
                dto.getUsuarioId()
        );

        repo.guardarApoderadoBasico(
                idCuentaAportes,
                dto.getDocumentoApoderadoAportes(),
                dto.getNombreApoderadoAportes(),
                dto.getTelefonoApoderadoAportes(),
                dto.getCelularApoderadoAportes(),
                dto.getUsuarioId()
        );

        System.out.println("✔ Cuenta de aportes creada: " + idCuentaAportes);


        // ============================================================
        // 🔥 CUENTA 2 — FORMA SELECCIONADA
        // ============================================================
        System.out.println("🟢 creando segunda cuenta (forma seleccionada): " + idFormaAhorroSeleccionada);

        boolean esAportes = idFormaAhorroSeleccionada != null && idFormaAhorroSeleccionada == 1;

        String gmf = dto.getGmf();
        Boolean retencion = dto.getRetencion();

        if (esAportes) {
            gmf = "N";
            retencion = false;
        }

        Integer consecutivoAhorro =
                repo.incrementarYObtenerConsecutivo(idFormaAhorroSeleccionada, idAgencia);

        String codigoAhorro = String.format("%010d", consecutivoAhorro);

        Integer idCuentaAhorro = repo.crearCuenta(
                idFormaAhorroSeleccionada,
                idPersona,
                idAgencia,
                codigoAhorro,
                gmf,
                retencion,
                dto.getUsuarioId()
        );

        repo.guardarApoderadoBasico(
                idCuentaAhorro,
                dto.getDocumentoApoderadoAhorro(),
                dto.getNombreApoderadoAhorro(),
                dto.getTelefonoApoderadoAhorro(),
                dto.getCelularApoderadoAhorro(),
                dto.getUsuarioId()
        );

        System.out.println("✔ Cuenta seleccionada creada: " + idCuentaAhorro);

        // ============================================================
        // 🔹 RESPUESTA FINAL
        // ============================================================
        res.setOk(true);
        res.setIdCuentaAhorro(idCuentaAhorro);
        res.setCodigoCuenta(codigoAhorro);
        res.setMensaje("✔ Se crearon las dos cuentas correctamente.");

        System.out.println("🏁 FIN — TRANSACCIÓN OK");
        return res;
    }
}
