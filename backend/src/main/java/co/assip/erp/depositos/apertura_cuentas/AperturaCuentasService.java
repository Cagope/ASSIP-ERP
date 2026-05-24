package co.assip.erp.depositos.apertura_cuentas;

import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentaItemDTO;
import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentasEntradaDTO;
import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentasRespuestaDTO;
import co.assip.erp.depositos.apertura_cuentas.repository.AperturaCuentasRepository;

import co.assip.erp.seguridad.service.UsuarioSesionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AperturaCuentasService {

    private final AperturaCuentasRepository repo;
    private final UsuarioSesionService usuarioSesionService;

    // ============================================================
    // 🔹 0. VALIDAR (para el botón Gestionar)
    // ============================================================
    public AperturaCuentasRespuestaDTO validarApertura(Integer idPersona, Integer idAgencia) {

        AperturaCuentasRespuestaDTO res = new AperturaCuentasRespuestaDTO();

        if (idPersona == null || idPersona <= 0) {
            res.setOk(false);
            res.setMensaje("Asociado no válido.");
            return res;
        }

        // 1) Tiene cuentas con saldo
        int cuentasConSaldo = repo.contarCuentasConSaldo(idPersona);
        if (cuentasConSaldo > 0) {
            res.setOk(false);
            res.setMensaje("El asociado tiene cuentas con saldo. No es posible abrir nuevas cuentas.");
            return res;
        }

        // 2) Ya tiene aportes activos
        boolean tieneAportesActivos = repo.tieneAportesActivos(idPersona);
        if (tieneAportesActivos) {
            res.setOk(false);
            res.setMensaje("El asociado ya tiene una cuenta de APORTES activa.");
            return res;
        }

        // 3) NUEVO: validar cuentas ACTIVAS en la misma agencia, aunque saldo = 0
        boolean tieneCuentaActivaAgencia = repo.existeCuentaActivaEnAgencia(idPersona, idAgencia);
        if (tieneCuentaActivaAgencia) {
            res.setOk(false);
            res.setMensaje("El asociado ya tiene una cuenta activa en esta agencia.");
            return res;
        }

        // ✔ Pasa todas las validaciones
        res.setOk(true);
        res.setMensaje("OK");
        return res;
    }

    // ============================================================
    // 🔹 1. LISTAR FORMAS (solo para llenar combo, no valida)
    // ============================================================
    public List<AperturaCuentaItemDTO> listarFormas(Integer idPersona) {

        boolean tieneAportesActivos = repo.tieneAportesActivos(idPersona);

        var agenciasUsuario =
                usuarioSesionService.agencias();
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
                    .forEach(x -> x.setObservacion("⚠️ Ya posee cuenta de aportes con saldo/activa"));
        }

        return lista;
    }

    // ============================================================
    // 🔹 2. CREAR DOS CUENTAS EN TRANSACCIÓN
    // ============================================================
    @Transactional
    public AperturaCuentasRespuestaDTO crear(AperturaCuentasEntradaDTO dto) {

        AperturaCuentasRespuestaDTO res = new AperturaCuentasRespuestaDTO(); // ✅ PRIMERO

        Integer idUsuario =
                usuarioSesionService.idUsuario();

        System.out.println("➡️ INICIANDO PROCESO DE CREACIÓN DE 2 CUENTAS");

        if (dto.getIdDatosPersonal() == null || dto.getIdDatosPersonal() <= 0) {
            res.setOk(false);
            res.setMensaje("Asociado no válido.");
            return res;
        }

        var agencias =
                usuarioSesionService.agencias();

        Integer idAgencia = (agencias != null && agencias.size() == 1)
                ? agencias.get(0)
                : null;

        if (idAgencia == null) {
            res.setOk(false);
            res.setMensaje("No se pudo determinar la agencia del usuario.");
            return res;
        }

        dto.setIdAgenciaUsuario(idAgencia);

        Integer idPersona = dto.getIdDatosPersonal();
        Integer idFormaAhorroSeleccionada = dto.getIdFormaAhorro();

        // 🔥 CUENTA 1 — APORTES
        Integer consecutivoAportes =
                repo.incrementarYObtenerConsecutivo(1, idAgencia);

        String codigoAportes = String.format("%06d", consecutivoAportes);

        Integer idCuentaAportes = repo.crearCuenta(
                1,
                idPersona,
                idAgencia,
                codigoAportes,
                "N",
                false,
                idUsuario
        );

        repo.guardarApoderadoBasico(
                idCuentaAportes,
                dto.getDocumentoApoderadoAportes(),
                dto.getNombreApoderadoAportes(),
                dto.getTelefonoApoderadoAportes(),
                dto.getCelularApoderadoAportes(),
                idUsuario
        );

        // 🔥 CUENTA 2 — FORMA SELECCIONADA
        boolean esAportes = idFormaAhorroSeleccionada != null && idFormaAhorroSeleccionada == 1;

        String gmf = dto.getGmf();
        Boolean retencion = dto.getRetencion();

        if (esAportes) {
            gmf = "N";
            retencion = false;
        }

        Integer consecutivoAhorro =
                repo.incrementarYObtenerConsecutivo(idFormaAhorroSeleccionada, idAgencia);

        String codigoAhorro = String.format("%06d", consecutivoAhorro);

        Integer idCuentaAhorro = repo.crearCuenta(
                idFormaAhorroSeleccionada,
                idPersona,
                idAgencia,
                codigoAhorro,
                gmf,
                retencion,
                idUsuario
        );

        repo.guardarApoderadoBasico(
                idCuentaAhorro,
                dto.getDocumentoApoderadoAhorro(),
                dto.getNombreApoderadoAhorro(),
                dto.getTelefonoApoderadoAhorro(),
                dto.getCelularApoderadoAhorro(),
                idUsuario
        );

        res.setOk(true);
        res.setIdCuentaAhorro(idCuentaAhorro);
        res.setCodigoCuenta(codigoAhorro);
        res.setMensaje("✔ Se crearon las dos cuentas correctamente.");

        return res;
    }

}
