package co.assip.erp.depositos.apertura_cuentas;

import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentaItemDTO;
import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentasEntradaDTO;
import co.assip.erp.depositos.apertura_cuentas.dto.AperturaCuentasRespuestaDTO;
import co.assip.erp.depositos.apertura_cuentas.repository.AperturaCuentasRepository;

import co.assip.erp.seguridad.utils.SecurityUtils;   // ⭐ IMPORTANTE

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
    // 🔹 1. LISTAR FORMAS — AGENCIA SE TOMA DEL JWT
    // ============================================================
    public List<AperturaCuentaItemDTO> listarFormas(Integer idPersona) {

        // 🔥 1) Validación global — NO cambia
        int cuentasConSaldo = repo.contarCuentasConSaldo(idPersona);
        if (cuentasConSaldo > 0) {
            return Collections.emptyList();
        }

        boolean tieneAportesActivos = repo.tieneAportesActivos(idPersona);

        // 🔥 2) AGENCIAS DEL USUARIO DESDE EL TOKEN
        var agenciasUsuario = SecurityUtils.getAgencias();
        Integer agenciaFiltrar = null;

        if (agenciasUsuario != null && agenciasUsuario.size() == 1) {
            agenciaFiltrar = agenciasUsuario.get(0);
        }

        System.out.println("🔥 AGENCIA DEL USUARIO (JWT) = " + agenciaFiltrar);

        // 🔥 3) Traemos TODAS las formas internas (así funcionan las validaciones)
        var lista = repo.listarFormasDisponibles(idPersona, agenciaFiltrar);

        if (agenciaFiltrar != null) {

            // ⭐ Necesario para que funcione en el lambda
            final Integer agenciaFinal = agenciaFiltrar;

            lista.removeIf(x -> !x.getIdAgencia().equals(agenciaFinal));
        }

        // 🔥 5) Reglas de aportes iguales
        if (tieneAportesActivos) {
            lista.stream()
                    .filter(x -> "01".equals(x.getCodigoForma()))
                    .forEach(x -> x.setObservacion("⚠️ Ya posee cuenta de aportes con saldo"));
        }

        return lista;
    }

    // ============================================================
    // 🔹 2. CREAR CUENTA — AGENCIA TAMBIÉN SE OBTIENE DEL JWT
    // ============================================================
    @Transactional
    public AperturaCuentasRespuestaDTO crear(AperturaCuentasEntradaDTO dto) {

        System.out.println("➡️ ENTRÓ A CREAR CUENTA");
        System.out.println("DTO RECIBIDO = " + dto);

        AperturaCuentasRespuestaDTO res = new AperturaCuentasRespuestaDTO();

        // -------- VALIDACIONES DURAS --------
        if (dto.getIdDatosPersonal() == null || dto.getIdDatosPersonal() <= 0) {
            res.setOk(false);
            res.setMensaje("Asociado no válido.");
            return res;
        }

        // ⭐ AGENCIA TOMADA DEL TOKEN
        var agencias = SecurityUtils.getAgencias();
        Integer idAgencia = (agencias != null && agencias.size() == 1) ? agencias.get(0) : null;

        System.out.println("🔥 AGENCIA DEL JWT PARA CREAR = " + idAgencia);

        if (idAgencia == null) {
            res.setOk(false);
            res.setMensaje("No se pudo determinar la agencia del usuario.");
            return res;
        }

        // Se guarda en el DTO (evita errores en repo)
        dto.setIdAgenciaUsuario(idAgencia);

        if (dto.getUsuarioId() == null || dto.getUsuarioId() <= 0) {
            res.setOk(false);
            res.setMensaje("Usuario no detectado en sesión.");
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

        // ⭐ Consecutivo por AGENCIA + FORMA
        Integer consecutivo =
                repo.incrementarYObtenerConsecutivo(dto.getIdFormaAhorro(), idAgencia);

        String codigo = String.format("%010d", consecutivo);
        System.out.println("✔ Código generado = " + codigo);

        // Crear
        Integer idCuenta = repo.crearCuenta(
                dto.getIdFormaAhorro(),
                dto.getIdDatosPersonal(),
                idAgencia,
                codigo,
                gmf,
                retencion,
                dto.getUsuarioId()
        );

        System.out.println("✔ Cuenta creada con ID = " + idCuenta);

        // Apoderados
        repo.guardarApoderadoBasico(
                idCuenta,
                dto.getDocumentoApoderadoAportes(),
                dto.getNombreApoderadoAportes(),
                dto.getTelefonoApoderadoAportes(),
                dto.getCelularApoderadoAportes(),
                dto.getUsuarioId()
        );

        repo.guardarApoderadoBasico(
                idCuenta,
                dto.getDocumentoApoderadoAhorro(),
                dto.getNombreApoderadoAhorro(),
                dto.getTelefonoApoderadoAhorro(),
                dto.getCelularApoderadoAhorro(),
                dto.getUsuarioId()
        );

        res.setOk(true);
        res.setIdCuentaAhorro(idCuenta);
        res.setCodigoCuenta(codigo);
        res.setMensaje("✔ Cuenta creada correctamente.");

        System.out.println("🏁 FIN CREACIÓN → COMMIT");
        return res;
    }
}
