package co.assip.erp.seguridad.service;

import co.assip.erp.seguridad.domain.UsuarioAgencia;
import co.assip.erp.seguridad.repository.UsuarioAgenciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UsuarioAgenciaService {

    private final NamedParameterJdbcTemplate jdbc;
    private final UsuarioAgenciaRepository usuarioAgenciaRepository;


    // ============================================================
    // 🔹 CONSULTAR AGENCIAS DE UN USUARIO
    // ============================================================
    public List<Map<String, Object>> listarAgenciasDelUsuario(Integer idUsuario) {
        String sql = """
            SELECT ua.id_agencia,
                   a.codigo_agencia,
                   a.nombre_agencia
            FROM seguridad.usuarios_agencias ua
            JOIN general.datos_agencias a ON a.id_agencia = ua.id_agencia
            WHERE ua.id_usuario = :id
        """;

        return jdbc.queryForList(sql, Map.of("id", idUsuario));
    }


    // ============================================================
    // 🔹 ELIMINAR TODAS LAS AGENCIAS DE UN USUARIO (para reasignación)
    // ============================================================
    public void limpiarAgencias(Integer idUsuario) {
        usuarioAgenciaRepository.deleteByIdUsuario(idUsuario);
    }


    // ============================================================
    // 🔹 ASIGNAR AGENCIA A UN USUARIO
    // ============================================================
    public void asignarAgencia(Integer idUsuario, Integer idAgencia, Integer idSeguridadCreador) {

        boolean existe = usuarioAgenciaRepository.existsByIdUsuarioAndIdAgencia(idUsuario, idAgencia);

        if (existe) {
            return; // Ya existe -> no duplicamos
        }

        UsuarioAgencia nueva = UsuarioAgencia.builder()
                .idUsuario(idUsuario)
                .idAgencia(idAgencia)
                .fkSeguridadCreacion(idSeguridadCreador)
                .fechaCreacion(LocalDateTime.now())
                .build();

        usuarioAgenciaRepository.save(nueva);
    }


    // ============================================================
    // 🔹 ASIGNAR LISTA COMPLETA DE AGENCIAS
    //   * Limpia todo y vuelve a insertar
    // ============================================================
    @Transactional
    public void asignarAgencias(Integer idUsuario, List<Integer> agencias, Integer usuarioCreador) {

        usuarioAgenciaRepository.deleteByIdUsuario(idUsuario);

        for (Integer idAgencia : agencias) {
            asignarAgencia(idUsuario, idAgencia, usuarioCreador);
        }
    }


    // ============================================================
    // 🔹 VALIDAR PERMISO DE USUARIO PARA UNA AGENCIA
    // ============================================================
    public boolean usuarioTieneAcceso(Integer idUsuario, Integer idAgencia) {
        return usuarioAgenciaRepository.existsByIdUsuarioAndIdAgencia(idUsuario, idAgencia);
    }


    // ============================================================
    // 🔹 OBTENER SOLO LOS ID's DE AGENCIA
    // ============================================================
    public List<Integer> obtenerIdAgencias(Integer idUsuario) {
        String sql = """
            SELECT id_agencia
            FROM seguridad.usuarios_agencias
            WHERE id_usuario = :id
        """;

        return jdbc.queryForList(sql, Map.of("id", idUsuario), Integer.class);
    }
}
