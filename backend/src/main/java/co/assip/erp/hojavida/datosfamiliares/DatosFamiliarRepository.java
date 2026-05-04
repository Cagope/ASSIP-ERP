    package co.assip.erp.hojavida.datosfamiliares;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import java.util.List;

    @Repository
    public interface DatosFamiliarRepository extends JpaRepository<DatosFamiliar, Integer> {

        /** 🔹 Retorna todos los registros ordenados por fecha de edición descendente */
        List<DatosFamiliar> findAllByOrderByFechaEdicionDesc();

        /** 🔹 Lista de familiares por persona */
        List<DatosFamiliar> findByIdDatosPersonal(Integer idDatosPersonal);

        /** 🔹 Lista de familiares por parentesco */
        List<DatosFamiliar> findByCodigoParentesco(String codigoParentesco);

        /** 🔹 Lista de familiares por ciudad */
        List<DatosFamiliar> findByIdCiudad(Integer idCiudad);

        /** 🔹 Lista de familiares por referencia */
        List<DatosFamiliar> findByReferenciaFamiliarTrue();
    }
