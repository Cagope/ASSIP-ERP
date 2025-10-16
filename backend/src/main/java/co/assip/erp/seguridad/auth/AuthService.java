package co.assip.erp.seguridad.auth;

import co.assip.erp.seguridad.domain.LogEvento;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.repository.LogEventoRepository;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final LogEventoRepository logEventoRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

    public AuthService(UsuarioRepository usuarioRepository,
                       LogEventoRepository logEventoRepository,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.logEventoRepository = logEventoRepository;
        this.jwtService = jwtService;
    }

    public LoginResponse login(String username, String password, String ip, String ua) {
        Usuario u = usuarioRepository.findByUsernameIgnoreCase(username).orElse(null);
        if (u == null || !u.isActivo() || !encoder.matches(password, u.getPasswordHash())) {
            audit("SEG.AUTH.LOGIN_FAIL", 401, username, ip, ua, "{\"reason\":\"BAD_CREDENTIALS_OR_INACTIVE\"}");
            throw new RuntimeException("BAD_CREDENTIALS");
        }

        String token = jwtService.generateToken(u.getId(), u.getUsername(),
                u.isSuperuserSeguridad(), u.isDebeCambiarPassword(), u.isActivo());

        audit("SEG.AUTH.LOGIN_OK", 200, u.getUsername(), ip, ua, "{\"uid\":" + u.getId() + "}");

        return new LoginResponse(token, u.getId(), u.getUsername(),
                u.isSuperuserSeguridad(), u.isDebeCambiarPassword(), u.isActivo());
    }

    public MeResponse me(Usuario u) {
        MeResponse me = new MeResponse();
        me.setId(u.getId());
        me.setUsername(u.getUsername());
        me.setSuperuserSeguridad(u.isSuperuserSeguridad());
        me.setActivo(u.isActivo());
        me.setDebeCambiarPassword(u.isDebeCambiarPassword());
        return me;
    }

    private void audit(String accion, int code, String username, String ip, String ua, String extraJson) {
        LogEvento ev = new LogEvento();
        ev.setAccion(accion);
        ev.setResultado(accion.endsWith("_OK") ? "OK" : "FAIL");
        ev.setEntidad("auth");
        ev.setStatusCode(code);
        ev.setUsernameActor(username);
        ev.setIp(ip);
        ev.setUserAgent(ua);
        ev.setExtraJson(extraJson);
        logEventoRepository.save(ev);
    }
}
