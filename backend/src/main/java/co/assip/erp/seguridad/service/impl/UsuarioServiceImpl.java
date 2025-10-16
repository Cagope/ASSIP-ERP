package co.assip.erp.seguridad.service.impl;

import co.assip.erp.seguridad.domain.LogEvento;
import co.assip.erp.seguridad.domain.Usuario;
import co.assip.erp.seguridad.dto.UsuarioCreateRequest;
import co.assip.erp.seguridad.dto.UsuarioResponse;
import co.assip.erp.seguridad.mapper.UsuarioMapper;
import co.assip.erp.seguridad.repository.LogEventoRepository;
import co.assip.erp.seguridad.repository.SesionRepository;
import co.assip.erp.seguridad.repository.UsuarioRepository;
import co.assip.erp.seguridad.service.UsuarioService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.Locale;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final LogEventoRepository logEventoRepository;
    private final SesionRepository sesionRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
    private static final int MIN_PASSWORD = 10;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository,
                              LogEventoRepository logEventoRepository,
                              SesionRepository sesionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.logEventoRepository = logEventoRepository;
        this.sesionRepository = sesionRepository;
    }

    // =======================
    // LECTURA
    // =======================
    @Override
    public Page<UsuarioResponse> listar(String q, Boolean activo, Boolean superuser, Pageable pageable,
                                        String actorUsername, String ip, String ua) {
        String query = (q == null) ? "" : q.trim();
        Page<Usuario> page;
        if (activo == null && superuser == null) {
            page = usuarioRepository.findByUsernameContainingIgnoreCase(query, pageable);
        } else if (activo != null && superuser == null) {
            page = usuarioRepository.findByActivoAndUsernameContainingIgnoreCase(activo, query, pageable);
        } else if (activo == null) {
            page = usuarioRepository.findBySuperuserSeguridadAndUsernameContainingIgnoreCase(superuser, query, pageable);
        } else {
            page = usuarioRepository.findByActivoAndSuperuserSeguridadAndUsernameContainingIgnoreCase(activo, superuser, query, pageable);
        }

        LogEvento ev = new LogEvento();
        ev.setAccion("SEG.USUARIOS.VIEW_LIST_OK");
        ev.setResultado("OK");
        ev.setEntidad("usuario");
        ev.setStatusCode(200);
        ev.setUsernameActor(actorUsername);
        ev.setIp(ip);
        ev.setUserAgent(ua);
        ev.setExtraJson(String.format(Locale.ROOT,
                "{\"q\":\"%s\",\"activo\":%s,\"superuser\":%s,\"page\":%d,\"size\":%d}",
                esc(query), String.valueOf(activo), String.valueOf(superuser),
                pageable.getPageNumber(), pageable.getPageSize()));
        logEventoRepository.save(ev);

        return page.map(UsuarioMapper::toResponse);
    }

    @Override
    public UsuarioResponse detalle(Long id, String actorUsername, String ip, String ua) {
        Usuario u = usuarioRepository.findById(id).orElse(null);

        LogEvento ev = new LogEvento();
        ev.setEntidad("usuario");
        ev.setUsernameActor(actorUsername);
        ev.setIp(ip);
        ev.setUserAgent(ua);

        if (u == null) {
            ev.setAccion("SEG.USUARIOS.VIEW_DETAIL_FAIL");
            ev.setResultado("FAIL");
            ev.setStatusCode(404);
            ev.setExtraJson("{\"id\":" + id + "}");
            logEventoRepository.save(ev);
            throw new RuntimeException("NOT_FOUND");
        }

        ev.setAccion("SEG.USUARIOS.VIEW_DETAIL_OK");
        ev.setResultado("OK");
        ev.setStatusCode(200);
        ev.setExtraJson("{\"id\":" + id + "}");
        logEventoRepository.save(ev);

        return UsuarioMapper.toResponse(u);
    }

    // =======================
    // CREACIÓN
    // =======================
    @Override
    public UsuarioResponse crear(UsuarioCreateRequest req, String actorUsername, String ip, String ua) {
        if (req == null || !StringUtils.hasText(req.getUsername())) {
            auditFail("SEG.USUARIOS.CREAR_FAIL", 400, actorUsername, ip, ua, "{\"error\":\"USERNAME_REQUIRED\"}");
            throw new IllegalArgumentException("USERNAME_REQUIRED");
        }
        String username = req.getUsername().trim().toLowerCase(Locale.ROOT);
        if (!username.matches("^[a-z0-9._-]{4,32}$")) {
            auditFail("SEG.USUARIOS.CREAR_FAIL", 400, actorUsername, ip, ua, "{\"error\":\"USERNAME_INVALID\"}");
            throw new IllegalArgumentException("USERNAME_INVALID");
        }
        if (usuarioRepository.existsByUsername(username)) {
            auditFail("SEG.USUARIOS.CREAR_FAIL", 409, actorUsername, ip, ua,
                    "{\"error\":\"DUPLICATE_USERNAME\",\"username\":\"" + esc(username) + "\"}");
            throw new IllegalStateException("DUPLICATE_USERNAME");
        }

        String password = req.getPassword();
        if (!StringUtils.hasText(password)) {
            password = generarPasswordTemporal(MIN_PASSWORD + 2);
        }
        if (password.length() < MIN_PASSWORD) {
            auditFail("SEG.USUARIOS.CREAR_FAIL", 400, actorUsername, ip, ua, "{\"error\":\"PASSWORD_TOO_SHORT\"}");
            throw new IllegalArgumentException("PASSWORD_TOO_SHORT");
        }

        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(password));
        u.setDebeCambiarPassword(true);
        u.setActivo(req.getActivo() == null ? true : req.getActivo());
        u.setSuperuserSeguridad(false);

        Usuario saved = usuarioRepository.save(u);

        LogEvento ev = new LogEvento();
        ev.setAccion("SEG.USUARIOS.CREAR_OK");
        ev.setResultado("OK");
        ev.setEntidad("usuario");
        ev.setStatusCode(201);
        ev.setUsernameActor(actorUsername);
        ev.setIp(ip);
        ev.setUserAgent(ua);
        ev.setExtraJson("{\"username\":\"" + esc(username) + "\",\"debe_cambiar_password\":true,\"activo\":" + saved.isActivo() + "}");
        logEventoRepository.save(ev);

        return UsuarioMapper.toResponse(saved);
    }

    // =======================
    // ESTADO / CONTRASEÑAS
    // =======================
    @Transactional
    @Override
    public UsuarioResponse cambiarEstado(Long id, boolean activo, String actorUsername, String ip, String ua) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        if (u == null) {
            auditFail("SEG.USUARIOS.ESTADO_FAIL", 404, actorUsername, ip, ua, "{\"id\":" + id + ",\"error\":\"NOT_FOUND\"}");
            throw new RuntimeException("NOT_FOUND");
        }
        u.setActivo(activo);
        Usuario saved = usuarioRepository.save(u);

        if (!activo) { // al desactivar, revoca sesiones
            sesionRepository.revocarPorUsuario(id, "USER_DISABLED");
        }

        LogEvento ev = new LogEvento();
        ev.setAccion(activo ? "SEG.USUARIOS.REACTIVAR_OK" : "SEG.USUARIOS.DESACTIVAR_OK");
        ev.setResultado("OK");
        ev.setEntidad("usuario");
        ev.setStatusCode(200);
        ev.setUsernameActor(actorUsername);
        ev.setIp(ip);
        ev.setUserAgent(ua);
        ev.setExtraJson("{\"id\":" + id + ",\"activo\":" + activo + "}");
        logEventoRepository.save(ev);

        return UsuarioMapper.toResponse(saved);
    }

    @Transactional
    @Override
    public void resetPassword(Long id, String nuevaPassword, String actorUsername, String ip, String ua) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        if (u == null) {
            auditFail("SEG.USUARIOS.RESET_PASSWORD_FAIL", 404, actorUsername, ip, ua, "{\"id\":" + id + ",\"error\":\"NOT_FOUND\"}");
            throw new RuntimeException("NOT_FOUND");
        }
        String pwd = (nuevaPassword != null && !nuevaPassword.isBlank()) ? nuevaPassword : generarPasswordTemporal(MIN_PASSWORD + 2);
        if (pwd.length() < MIN_PASSWORD) {
            auditFail("SEG.USUARIOS.RESET_PASSWORD_FAIL", 400, actorUsername, ip, ua, "{\"id\":" + id + ",\"error\":\"PASSWORD_TOO_SHORT\"}");
            throw new IllegalArgumentException("PASSWORD_TOO_SHORT");
        }
        u.setPasswordHash(passwordEncoder.encode(pwd));
        u.setDebeCambiarPassword(true);
        usuarioRepository.save(u);

        LogEvento ev = new LogEvento();
        ev.setAccion("SEG.USUARIOS.RESET_PASSWORD_OK");
        ev.setResultado("OK");
        ev.setEntidad("usuario");
        ev.setStatusCode(200);
        ev.setUsernameActor(actorUsername);
        ev.setIp(ip);
        ev.setUserAgent(ua);
        ev.setExtraJson("{\"id\":" + id + ",\"debe_cambiar_password\":true}");
        logEventoRepository.save(ev);
    }

    @Transactional
    @Override
    public void forcePasswordChange(Long id, String actorUsername, String ip, String ua) {
        Usuario u = usuarioRepository.findById(id).orElse(null);
        if (u == null) {
            auditFail("SEG.USUARIOS.FORCE_PW_CHANGE_FAIL", 404, actorUsername, ip, ua, "{\"id\":" + id + ",\"error\":\"NOT_FOUND\"}");
            throw new RuntimeException("NOT_FOUND");
        }
        u.setDebeCambiarPassword(true);
        usuarioRepository.save(u);

        LogEvento ev = new LogEvento();
        ev.setAccion("SEG.USUARIOS.FORCE_PW_CHANGE_OK");
        ev.setResultado("OK");
        ev.setEntidad("usuario");
        ev.setStatusCode(200);
        ev.setUsernameActor(actorUsername);
        ev.setIp(ip);
        ev.setUserAgent(ua);
        ev.setExtraJson("{\"id\":" + id + ",\"debe_cambiar_password\":true}");
        logEventoRepository.save(ev);
    }

    // =======================
    // Helpers
    // =======================
    private void auditFail(String accion, int code, String actor, String ip, String ua, String extraJson) {
        LogEvento ev = new LogEvento();
        ev.setAccion(accion);
        ev.setResultado("FAIL");
        ev.setEntidad("usuario");
        ev.setStatusCode(code);
        ev.setUsernameActor(actor);
        ev.setIp(ip);
        ev.setUserAgent(ua);
        ev.setExtraJson(extraJson);
        logEventoRepository.save(ev);
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    private static String generarPasswordTemporal(int length) {
        final String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*()-_=+";
        SecureRandom rng = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(alphabet.charAt(rng.nextInt(alphabet.length())));
        return sb.toString();
    }
}
