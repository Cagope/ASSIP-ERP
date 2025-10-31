package co.assip.erp.hojavida.financieros;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancieroRepository extends JpaRepository<Financiero, Integer> {

    /** 🔹 Listar todos ordenados por fecha de edición descendente */
    List<Financiero> findAllByOrderByFechaEdicionDesc();

    /** 🔹 Buscar registro financiero por ID de persona */
    Optional<Financiero> findByIdDatosPersonal(Integer idDatosPersonal);
}
