package co.assip.erp.seguridad.usuariosagencias;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de administrar las agencias
 * autorizadas para cada usuario del ERP.
 *
 * La relación usuario-agencia determina en qué agencias
 * puede operar un usuario, independientemente de su rol.
 */
@Service
@RequiredArgsConstructor
public class UsuarioAgenciaService {

    private final NamedParameterJdbcTemplate jdbc;
    private final UsuarioAgenciaRepository usuarioAgenciaRepository;

    /**
     * Consulta las agencias asignadas a un usuario,
     * incluyendo código y nombre de la agencia.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarAgenciasDelUsuario(
            Integer idUsuario
    ) {

        String sql = """
                SELECT ua.id_agencia,
                       a.codigo_agencia,
                       a.nombre_agencia
                FROM seguridad.usuarios_agencias ua
                JOIN general.datos_agencias a
                  ON a.id_agencia = ua.id_agencia
                WHERE ua.id_usuario = :id
                ORDER BY ua.id_agencia
                """;

        return jdbc.queryForList(
                sql,
                Map.of("id", idUsuario)
        );
    }

    /**
     * Elimina todas las asignaciones de agencias
     * correspondientes a un usuario.
     */
    @Transactional
    public void limpiarAgencias(
            Integer idUsuario
    ) {

        usuarioAgenciaRepository
                .deleteByIdUsuario(idUsuario);
    }

    /**
     * Asigna una agencia a un usuario.
     *
     * Si la relación ya existe no se genera
     * un registro duplicado.
     */
    @Transactional
    public void asignarAgencia(
            Integer idUsuario,
            Integer idAgencia,
            Integer idSeguridadCreador
    ) {

        boolean existe =
                usuarioAgenciaRepository
                        .existsByIdUsuarioAndIdAgencia(
                                idUsuario,
                                idAgencia
                        );

        if (existe) {
            return;
        }

        UsuarioAgencia nueva =
                UsuarioAgencia.builder()
                        .idUsuario(idUsuario)
                        .idAgencia(idAgencia)
                        .fkSeguridadCreacion(idSeguridadCreador)
                        .fechaCreacion(LocalDateTime.now())
                        .build();

        usuarioAgenciaRepository.save(nueva);
    }

    /**
     * Reemplaza completamente las agencias asignadas
     * a un usuario.
     *
     * Primero elimina las relaciones existentes y luego
     * registra las agencias recibidas.
     */
    @Transactional
    public void asignarAgencias(
            Integer idUsuario,
            List<Integer> agencias,
            Integer usuarioCreador
    ) {

        usuarioAgenciaRepository
                .deleteByIdUsuario(idUsuario);

        /*
         * Una lista nula o vacía representa un usuario
         * sin agencias asignadas.
         */
        if (agencias == null || agencias.isEmpty()) {
            return;
        }

        agencias.stream()
                .filter(idAgencia -> idAgencia != null)
                .distinct()
                .forEach(idAgencia ->
                        asignarAgencia(
                                idUsuario,
                                idAgencia,
                                usuarioCreador
                        )
                );
    }

    /**
     * Verifica si un usuario tiene autorización
     * para operar en una agencia determinada.
     */
    @Transactional(readOnly = true)
    public boolean usuarioTieneAcceso(
            Integer idUsuario,
            Integer idAgencia
    ) {

        return usuarioAgenciaRepository
                .existsByIdUsuarioAndIdAgencia(
                        idUsuario,
                        idAgencia
                );
    }

    /**
     * Obtiene únicamente los identificadores de las
     * agencias asignadas al usuario.
     */
    @Transactional(readOnly = true)
    public List<Integer> obtenerIdAgencias(
            Integer idUsuario
    ) {

        String sql = """
                SELECT id_agencia
                FROM seguridad.usuarios_agencias
                WHERE id_usuario = :id
                ORDER BY id_agencia
                """;

        return jdbc.queryForList(
                sql,
                Map.of("id", idUsuario),
                Integer.class
        );
    }
}