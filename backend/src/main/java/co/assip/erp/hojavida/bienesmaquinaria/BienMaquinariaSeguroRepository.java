package co.assip.erp.hojavida.bienesmaquinaria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BienMaquinariaSeguroRepository
        extends JpaRepository<BienMaquinariaSeguro, Long> {

    List<BienMaquinariaSeguro> findByIdBienOrderByFechaVencimientoSeguroDescFechaCreacionDesc(
            Long idBien
    );

    boolean existsByIdBien(Long idBien);
}