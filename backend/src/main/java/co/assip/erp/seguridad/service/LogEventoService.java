package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.LogEvento;
import co.assip.erp.seguridad.repository.LogEventoRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogEventoService {

    private final LogEventoRepository logEventoRepository;

    /**
     * 🟢 Registrar inicio de sesión exitoso
     */
    @Transactional
    public void registrarLogin(Integer idUsuario, HttpServletRequest request) {
        registrarEvento(
                idUsuario,
                "SEGURIDAD",
                "LOGIN",
                "Inicio de sesión exitoso",
                request
        );
    }

    /**
     * 🟡 Método genérico para registrar cualquier tipo de evento (crear, actualizar, eliminar, etc.)
     */
    @Transactional
    public void registrarEvento(
            Integer idUsuario,
            String modulo,
            String accion,
            String descripcion,
            HttpServletRequest request
    ) {
        String ip = (request != null) ? request.getRemoteAddr() : "localhost";
        String userAgent = (request != null) ? request.getHeader("User-Agent") : "system";

        LogEvento evento = LogEvento.builder()
                .idUsuario(idUsuario)
                .modulo(modulo)
                .accion(accion)
                .descripcion(descripcion)
                .fechaEvento(LocalDateTime.now())
                .ipOrigen(ip)
                .userAgent(userAgent)
                .build();

        logEventoRepository.save(evento);
    }
}
