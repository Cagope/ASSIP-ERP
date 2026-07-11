package co.assip.erp.hojavida.bienesvehiculos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BienVehiculoSeguroRepository
        extends JpaRepository<BienVehiculoSeguro, Long> {

    List<BienVehiculoSeguro> findByIdBienOrderByFechaVencimientoSeguroDescFechaCreacionDesc(Long idBien);

    boolean existsByIdBien(Long idBien);
}