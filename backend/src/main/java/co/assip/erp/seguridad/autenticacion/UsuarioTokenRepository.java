
package co.assip.erp.seguridad.autenticacion;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio de tokens de seguridad asociados a los usuarios.
 *
 * Administra los códigos utilizados para:
 * - activación inicial de cuentas;
 * - recuperación de contraseñas.
 *
 * Permite:
 * - consultar el token pendiente más reciente;
 * - desactivar tokens anteriores;
 * - bloquear un registro durante su actualización.
 *
 * Los tokens se conservan para mantener su trazabilidad.
 */
@Repository
public interface UsuarioTokenRepository
        extends JpaRepository<UsuarioToken, Long> {

    /**
     * Obtiene el token activo más reciente de un usuario
     * para el tipo de operación indicado.
     *
     * No aplica bloqueo de escritura.
     */
    Optional<UsuarioToken>
    findFirstByUsuario_IdUsuarioAndTipoTokenAndActivoTrueAndUtilizadoFalseOrderByFechaCreacionDesc(
            Integer idUsuario,
            String tipoToken
    );

    /**
     * Desactiva los tokens pendientes anteriores
     * del mismo usuario y tipo.
     *
     * Conserva los registros históricos.
     *
     * Debe ejecutarse dentro de una transacción activa.
     *
     * @return cantidad de registros actualizados
     */
    @Modifying
    @Query("""
            UPDATE UsuarioToken t
               SET t.activo = false,
                   t.fechaEdicion = :fechaEdicion
             WHERE t.usuario.idUsuario = :idUsuario
               AND t.tipoToken = :tipoToken
               AND t.activo = true
               AND t.utilizado = false
            """)
    int desactivarTokensPendientes(
            @Param("idUsuario") Integer idUsuario,
            @Param("tipoToken") String tipoToken,
            @Param("fechaEdicion") LocalDateTime fechaEdicion
    );

    /**
     * Obtiene un token aplicando bloqueo pesimista
     * de escritura sobre su registro.
     *
     * Evita que dos transacciones modifiquen
     * simultáneamente el contador de intentos
     * del mismo token.
     *
     * El bloqueo permanece hasta finalizar
     * la transacción que realiza la consulta.
     *
     * Debe utilizarse dentro de una transacción activa.
     *
     * @param idUsuarioToken identificador del token
     * @return token bloqueado para actualización
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
              FROM UsuarioToken t
             WHERE t.idUsuarioToken = :idUsuarioToken
            """)
    Optional<UsuarioToken> findByIdForUpdate(
            @Param("idUsuarioToken") Long idUsuarioToken
    );
}
