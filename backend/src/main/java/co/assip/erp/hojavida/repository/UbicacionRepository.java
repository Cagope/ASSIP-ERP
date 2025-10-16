package co.assip.erp.hojavida.repository;

import co.assip.erp.hojavida.domain.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Integer> {

    // 1:1 por persona
    Optional<Ubicacion> findByIdDatosPersonal(Integer idDatosPersonal);
    boolean existsByIdDatosPersonal(Integer idDatosPersonal);
    Optional<Ubicacion> findByIdUbicacionAndIdDatosPersonal(Integer idUbicacion, Integer idDatosPersonal);
    long deleteByIdUbicacionAndIdDatosPersonal(Integer idUbicacion, Integer idDatosPersonal);

    // ===== Lectura: proyección con nombres ya decodificados (JOINs) =====
    interface UbicacionView {
        Integer getIdUbicacion();
        Integer getIdDatosPersonal();

        String  getDireccion();
        String  getBarrio();
        String  getTelefono();
        String  getCelularUno();
        String  getCelularDos();
        String  getCorreo();

        Integer getIdPais();
        Integer getIdDepartamento();
        Integer getIdCiudad();
        Integer getIdSubZona();

        String  getNombrePais();
        String  getNombreDepartamento();
        String  getNombreCiudad();
        String  getNombreZona();
        String  getNombreSubZona();
    }

    @Query(value = """
        SELECT
          u.id_ubicacion         AS idUbicacion,
          u.id_datos_personal    AS idDatosPersonal,
          u.direccion            AS direccion,
          u.barrio               AS barrio,
          u.telefono             AS telefono,
          u.celular_uno          AS celularUno,
          u.celular_dos          AS celularDos,
          u.correo               AS correo,
          u.id_pais              AS idPais,
          u.id_departamento      AS idDepartamento,
          u.id_ciudad            AS idCiudad,
          u.id_sub_zona          AS idSubZona,
          p.nombre_pais          AS nombrePais,
          d.nombre_departamento  AS nombreDepartamento,
          c.nombre_ciudad        AS nombreCiudad,
          z.nombre_zona          AS nombreZona,
          sz.nombre_sub_zona     AS nombreSubZona
        FROM hoja_vida.ubicaciones u
        LEFT JOIN catalogos.paises        p  ON p.id_pais = u.id_pais
        LEFT JOIN catalogos.departamentos d  ON d.id_departamento = u.id_departamento
        LEFT JOIN catalogos.ciudades      c  ON c.id_ciudad = u.id_ciudad
        LEFT JOIN general.sub_zonas       sz ON sz.id_sub_zona = u.id_sub_zona
        LEFT JOIN general.zonas           z  ON z.id_zona = sz.id_zona
        WHERE u.id_datos_personal = :idDatosPersonal
        LIMIT 1
    """, nativeQuery = true)
    Optional<UbicacionView> findViewByIdDatosPersonal(@Param("idDatosPersonal") Integer idDatosPersonal);

    // ===== Aux: validaciones coherencia (simple y eficientes) =====
    @Query(value = "SELECT c.id_departamento FROM catalogos.ciudades c WHERE c.id_ciudad = :idCiudad", nativeQuery = true)
    Integer findDepartamentoByCiudad(@Param("idCiudad") Integer idCiudad);

    @Query(value = "SELECT z.id_zona FROM general.sub_zonas sz JOIN general.zonas z ON z.id_zona = sz.id_zona WHERE sz.id_sub_zona = :idSubZona", nativeQuery = true)
    Integer findZonaBySubZona(@Param("idSubZona") Integer idSubZona);

    @Query(value = "SELECT COUNT(1) FROM general.sub_zonas sz WHERE sz.id_sub_zona = :idSubZona", nativeQuery = true)
    int existsSubZona(@Param("idSubZona") Integer idSubZona);
}
