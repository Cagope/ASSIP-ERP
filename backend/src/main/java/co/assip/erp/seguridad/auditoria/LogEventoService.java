package co.assip.erp.seguridad.auditoria;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de auditoría de eventos del ERP.
 *
 * Centraliza el registro de acciones relevantes realizadas
 * por los usuarios y procesos del sistema.
 */
@Service
@RequiredArgsConstructor
public class LogEventoService {

    private final LogEventoRepository logEventoRepository;

    /**
     * Registra un inicio de sesión exitoso.
     *
     * @param idUsuario usuario que inició sesión
     * @param request   petición HTTP utilizada para obtener IP y navegador
     */
    @Transactional
    public void registrarLogin(
            Integer idUsuario,
            HttpServletRequest request
    ) {

        registrarEvento(
                idUsuario,
                "SEGURIDAD",
                "LOGIN",
                "Inicio de sesión exitoso",
                request
        );
    }

    /**
     * Registra un evento de auditoría.
     *
     * @param idUsuario  usuario responsable del evento
     * @param modulo     módulo donde ocurrió
     * @param accion     acción realizada
     * @param descripcion descripción funcional del evento
     * @param request    petición HTTP de origen; puede ser null
     */
    @Transactional
    public void registrarEvento(
            Integer idUsuario,
            String modulo,
            String accion,
            String descripcion,
            HttpServletRequest request
    ) {

        String ipOrigen =
                request != null
                        ? request.getRemoteAddr()
                        : "localhost";

        String userAgent =
                request != null
                        ? request.getHeader("User-Agent")
                        : "system";

        LogEvento evento = LogEvento.builder()
                .idUsuario(idUsuario)
                .modulo(modulo)
                .accion(accion)
                .descripcion(descripcion)
                .ipOrigen(ipOrigen)
                .userAgent(userAgent)
                .build();

        logEventoRepository.save(evento);
    }
}