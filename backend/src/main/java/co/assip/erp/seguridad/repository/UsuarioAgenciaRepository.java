package co.assip.erp.seguridad.repository;

import co.assip.erp.seguridad.domain.UsuarioAgencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioAgenciaRepository extends JpaRepository<UsuarioAgencia, Integer> {

    // 🔹 Listar agencias asignadas a un usuario
    List<UsuarioAgencia> findByIdUsuario(Integer idUsuario);

    // 🔹 Eliminar todas las agencias de un usuario (para reasignar)
    void deleteByIdUsuario(Integer idUsuario);

    // 🔹 Verificar si el usuario tiene una agencia específica (control de permisos)
    boolean existsByIdUsuarioAndIdAgencia(Integer idUsuario, Integer idAgencia);
}
