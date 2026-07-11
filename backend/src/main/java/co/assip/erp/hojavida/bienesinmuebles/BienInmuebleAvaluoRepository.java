package co.assip.erp.hojavida.bienesinmueblesavaluos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BienInmuebleAvaluoRepository
        extends JpaRepository<BienInmuebleAvaluo, Long> {

    List<BienInmuebleAvaluo> findByIdBienOrderByFechaAvaluoDescFechaCreacionDesc(Long idBien);

    boolean existsByIdBien(Long idBien);
}