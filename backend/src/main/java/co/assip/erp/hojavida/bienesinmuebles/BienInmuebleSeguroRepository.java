package co.assip.erp.hojavida.bienesinmueblesseguros;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BienInmuebleSeguroRepository
        extends JpaRepository<BienInmuebleSeguro, Long> {

    List<BienInmuebleSeguro>
    findByIdBienOrderByFechaVencimientoSeguroDescFechaCreacionDesc(
            Long idBien
    );

    boolean existsByIdBien(Long idBien);
}